# Spec: Replace ChainTest Reporting with Allure Reporting

Written for: the repository owner (Joseph Mathew), who maintains this Selenium + TestNG framework and will carry out the migration.

## 1. Goal

Replace the ChainTest report (`com.aventstack:chaintest-testng`) with Allure reporting in `src/`. After the change:

- No code, dependency, listener, or config references ChainTest.
- Every test run writes Allure results to `target/allure-results`, and an Allure HTML report can be generated from them with the Allure 3 CLI.
- Each logged step is recorded as an Allure step with a status. Failure screenshots are recorded as Allure attachments.
- A screenshot failure never fails a test. Only the test's own assertions and actions do.
- The active test class (`TestLoginTest`) still runs through `regression-test-suite.xml`.

## 2. Current state

### 2.1 Reference project (`reference/`) – pattern source only

| Concern | How the reference does it |
|---|---|
| Dependency | `allure-testng` 2.35.2 (test scope). No ChainTest. |
| Listener | `io.qameta.allure.testng.AllureTestNg` declared in `regression-test-suite.xml`. |
| Results dir | `allure.properties` → `allure.results.directory=target/allure-results`. |
| Step helper | `utilities/Step.java` wraps `Allure.step(...)` and also calls `Reporter.log(name, true)`. |
| Report build | `allure-maven` 3.1.0 with `reportVersion=${allure.version}` (2.35.2). This generates Allure 2 reports. |
| Annotations | `@Step` on TestBase methods, `@Owner`, `@Severity`, `@Name` on tests. |
| Retry | `AnnotationTransformer` + `RetryAnalyzer` (same as src). |

The reference has no ChainTest, no `@Listeners` on a base class, and no screenshot logic.

### 2.2 Source project (`src/`) – what must change

**Dependencies (`pom.xml`)**
- `chaintest-testng` 1.0.12 – remove.
- `allure-testng` `${allure.version}` = `3.1.1` – keep. The artifact resolves on Maven Central.
- `allure-maven` – not present in `src/pom.xml`. Do not add it (see §6).
- `aspectjweaver` 1.9.27 – keep.

**ChainTest coupling in Java (by file)**

| File | ChainTest coupling | Count |
|---|---|---|
| `src/test/java/utilities/ChainTestReporter.java` | `extends ChainTestListener`; screenshot capture and `Assert.fail` on screenshot error live in its `reportLog` method | whole class |
| `src/test/java/testbase/TheInternetWrapper.java` | `@Listeners(ChainTestReporter.class)` | 1 |
| `src/test/java/testbase/TestBase.java` | `reportLog(...)` calls `new ChainTestReporter().reportLog(...)`; `click`, `sendKeys`, `getText` pass `AttachScreenshot.YES` | 32 references in file |
| `src/test/java/utilities/Configuration.java` | `testBase.reportLog(...)` in 4 catch blocks; imports the enums | 8 |
| `src/test/java/utilities/DataProvider.java` | `testBase.reportLog(...)` and enum imports | 4 |
| `src/test/java/utilities/TimeUtilities.java` | Timestamp formatter used only for ChainTest log text | 1 |

Total: 38 `reportLog(` call sites and 7 `AttachScreenshot.YES` sites.

**Screenshot location (corrected):** The screenshot is taken inside `ChainTestReporter.reportLog`, not in `click`, `sendKeys` or `getText`. Those methods only pass `AttachScreenshot.YES`. When the screenshot throws `WebDriverException`, `reportLog` calls `Assert.fail("Failure when taking screenshot.")`, which fails the test. This is the behavior to remove.

**Config and resources**
- `src/test/resources/chaintest.properties` – delete.
- `src/test/resources/configuration.properties` – `report.log-level` is used only by the ChainTest level filter. It is removed with that filter (see §3.1). Remove the key and its comment block.
- `src/test/resources/allure.properties` – keep (`allure.results.directory=target/allure-results`).
- `src/test/resources/regression-test-suite.xml` – keep `AllureTestNg` and `AnnotationTransformer` listeners.
- `.gitignore` – keep `test-output/` (TestNG default output). ChainTest's `test-output/ChainTest Report/` is no longer produced.
- `allurec.json` (root) – keep. It is the config for the Allure 3 CLI report (see §6).
- `.allure/` (root) – created by the earlier `mvn allure:report` attempt. It contains no report. Delete it.

**Pre-existing issues (not caused by this migration)**
- `RetryAnalyzer` calls `new Configuration(null)` when no thread config exists. If `configuration.properties` is missing, `Configuration` throws a NullPointerException in its catch blocks because `testBase` is null. Fix while touching `Configuration`.
- `TestLoginTest` is modified in the working tree. Confirm those changes before committing.

## 3. Target design

### 3.1 `utilities/AllureWrapper.java` (replaces `ChainTestReporter`)

Name: `AllureWrapper`. It is a thin wrapper over the Allure API, named after what it wraps. It also avoids a clash with the `io.qameta.allure.Step` annotation, which `TestBase` already imports. Alternatives considered: `AllureSteps`, `TestReporter`.

Methods mirror the Allure API:

| Method | Behavior |
|---|---|
| `static void step(String name, Level level)` | If `level.number <= report.log-level`, calls `Reporter.log(name, true)` and `Allure.step(name, level.status)`. |
| `static void attachScreenshot(WebDriver driver)` | Captures a PNG and calls `Allure.addAttachment("Screenshot", "image/png", ...)`. Errors are caught here (see below). |

Log levels (kept, configured by `report.log-level`):

`AllureWrapper` has a `Level` enum. Each level has a number and an Allure status. A message is written only when `level.number <= report.log-level`. Level 0 in the config turns all logging off.

| Config value | Level | Meaning | Allure status | Existing call sites (before migration) |
|---|---|---|---|---|
| 0 | OFF | Threshold only; no message uses it | none | none |
| 1 | ERROR | Framework and test-data errors | BROKEN | levels 0 (4 sites) and 1 (5 sites) |
| 2 | FAIL | Failed test steps | FAILED | level 2 (4 sites) |
| 3 | WARN | Warnings that may indicate a failure | PASSED | level 3 (4 sites) |
| 4 | INFO | Informational steps that do not decide pass/fail | PASSED | level 4 sites that are not passed actions (to review) |
| 5 | PASS | Completed test actions (click, enter text, launch browser) | PASSED | level 4 sites that are passed actions (to review) |
| 6 | DEBUG | Framework internals (configuration, setup) | PASSED | levels 5 (0 sites) and 6 (8 sites) |

The 10 existing level-4 call sites were documented as PASS in `configuration.properties`, but the user has asked for INFO and PASS to be separate. Each one needs to be assigned to INFO or PASS during implementation.

Screenshot rule:
- Only the capture and attach are inside `try/catch`.
- On `WebDriverException` or any other `Exception`, log a warning with `Reporter.log` and return.
- Do not call `Assert.fail`. The action that triggered the screenshot has already succeeded or failed on its own terms.

Console output (`Reporter.log`) is written for every step, as it is now. Filtering console output to failures only is deferred.

Allure steps outside a running test (for example in the `Configuration` constructor or `RetryAnalyzer`) are a no-op in Allure. `Reporter.log` still records them. Confirm this in the first run.

### 3.2 `TestBase`

- Replace `reportLog(String, int, LogToStandardOut, AttachScreenshot)` with `step(String name, Level level)`. Update all 38 call sites.
- Each call site passes a `Level` name (`ERROR`, `FAIL`, `WARN`, `INFO`, `PASS`, `DEBUG`) instead of a number. Allure status comes from the level (§3.1).
- Remove the `Reporter.getCurrentTestResult()` block that built the `[methodName]` prefix. Remove the `[timestamp]` prefix. Allure records both.
- `click`, `sendKeys`, `getText`: after the action, call `AllureWrapper.attachScreenshot(driver)` when they previously passed `AttachScreenshot.YES`. Their own `try/catch` still handles action errors.
- Remove the `LogToStandardOut` and `AttachScreenshot` enums and all imports of them.
- Add Allure's `@Step` to `click`, `sendKeys`, `getText`, and `assertThatStringContains`, as the reference does, so Allure nests them.

### 3.3 Listeners

- Remove `@Listeners(ChainTestReporter.class)` from `TheInternetWrapper`.
- Keep `AllureTestNg` and `AnnotationTransformer` in `regression-test-suite.xml`.
- Do not add `@Listeners(AllureTestNg.class)` to any class.

### 3.4 `Configuration` and `DataProvider`

- `Configuration.java`: replace each `testBase.reportLog(..., 0, ...)` with `AllureWrapper.step(message, Level.ERROR)`. Remove the enum imports. Keep the `report.log-level` parsing line, which now feeds `AllureWrapper`. Drop the `testBase` dependency from the catch blocks, which fixes the null guard.
- `DataProvider.java`: replace its 4 log calls with `AllureWrapper.step(message, Level.ERROR)`. Remove the enum imports.

### 3.5 Test classes

- `TestLoginTest`: add `@Owner`, `@Severity`, `@Feature("Login")`, and `@Description`, matching the reference style.
- `TwoFactorLoginTest`: stays commented out in the suite. It must still compile.

## 4. POM changes

```xml
<!-- REMOVE -->
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>chaintest-testng</artifactId>
    <version>1.0.12</version>
    <scope>test</scope>
</dependency>
```

- Keep `<allure.version>3.1.1</allure.version>`. `allure-testng` 3.1.1 is on Maven Central.
- Do not add `allure-maven`.
- Leave the surefire `argLine` (AspectJ agent) unchanged.

## 5. Config file changes

| File | Change |
|---|---|
| `src/test/resources/chaintest.properties` | Delete. |
| `src/test/resources/configuration.properties` | Keep `report.log-level`. Rewrite its comment block to list the levels in §3.1 (0 OFF, 1 ERROR, 2 FAIL, 3 WARN, 4 INFO, 5 PASS, 6 DEBUG). |
| `src/test/resources/allure.properties` | Keep. |
| `src/test/resources/regression-test-suite.xml` | Keep. |
| `allurec.json` | Keep. Verify it with the CLI before relying on it (see §6). |
| `.allure/` | Delete (leftover from `mvn allure:report`). |

## 6. Report generation

Use the Allure 3 CLI already installed (`allure --version` → 3.17.0).

- Generate: `allure generate target/allure-results -o allure-report --clean`
- Serve locally: `allure serve target/allure-results`

`allurec.json` is read by the CLI from the project root. Its plugins (`awesome`, `dashboard`, `classic`, `csv`, `log`) and `qualityGate` must be confirmed to work with 3.17.0 by running the generate command. If the file causes errors, remove the unsupported keys or delete the file and run the CLI with defaults.

`allure-maven` is dropped. The reference's `allure-maven` 3.1.0 is set to `reportVersion` 2.35.2, so it generates Allure 2 reports, and it produced no report in this project. Using it would mean a second Allure version in the build.

## 7. Decisions

| # | Decision |
|---|---|
| D1 | Keep Allure 3.1.1 (`allure-testng`). |
| D2 | No `allure-maven`. Report generation uses the Allure 3.17.0 CLI. |
| D3 | Rename `ChainTestReporter` to `AllureWrapper`. Replace `reportLog` with `step(name, Level)`. Keep the level filter driven by `report.log-level` (0–6). Levels: OFF 0, ERROR 1, FAIL 2, WARN 3, INFO 4, PASS 5, DEBUG 6 (§3.1). |
| D4 | Console output: `Reporter.log` for every step, unchanged. Failure-only console filtering is deferred. |
| D5 | Screenshot failures are logged as a warning and never fail a test. The capture is wrapped in its own `try/catch` inside `AllureWrapper`. |
| D6 | Drop the `[timestamp]` and `[methodName]` prefixes. |

## 8. Implementation order

1. `pom.xml`: remove `chaintest-testng`. Keep `allure.version` at 3.1.1.
2. Create `utilities/AllureWrapper.java` with `step(name, Status)`, `step(name)`, and `attachScreenshot(WebDriver)`. Screenshot errors are caught and logged.
3. `TestBase`: replace `reportLog` with `step`. Update all 38 call sites. Replace `AttachScreenshot.YES` with `attachScreenshot`. Remove the enums and the `ChainTestReporter` instantiation. Add `@Step` annotations.
4. `Configuration` and `DataProvider`: update the log calls, remove enum imports and the `report.log-level` read. Remove the `testBase` dependency from the catch blocks.
5. `TheInternetWrapper`: remove `@Listeners(ChainTestReporter.class)`.
6. Delete `ChainTestReporter.java`, `TimeUtilities.java`, `chaintest.properties`, and `.allure/`.
7. Rewrite the level comments in `configuration.properties`. Assign each existing level-4 call site to INFO or PASS.
8. Add Allure annotations to `TestLoginTest`.
9. Verify:
   - `grep -ri chaintest src pom.xml` returns nothing.
   - `mvn -q test-compile` succeeds (Java 25).
   - `mvn test` runs `TestLoginTest` (chrome, headless) and writes `target/allure-results/*.json`.
   - `allure generate target/allure-results -o allure-report --clean` produces a report with steps. Check that it opens.
   - Force a screenshot failure (for example, close the browser before `attachScreenshot`) and confirm the test result is unchanged and a warning appears in the console.
   - `test-output/ChainTest Report/` is no longer created.
10. Commit in logical steps if wanted: dependencies, wrapper, tests, config.

## 9. Out of scope

- Converting `TwoFactorLoginTest` into the active suite.
- Changing page objects or locators.
- Failure-only console filtering (deferred, see D4).
- Moving to the `reference/` REST Assured framework. `reference/` is a pattern source, not a dependency.
- CI pipeline setup for publishing reports.

## 10. Risks

| Risk | Mitigation |
|---|---|
| `Allure.step` outside a test is dropped | `Reporter.log` still records those messages. Verify in the first run. |
| `allurec.json` keys not supported by CLI 3.17.0 | Run `allure generate` and fix or remove unsupported keys. |
| Screenshot attachments on every `click`, `sendKeys`, `getText` inflate the report | Keep the current `attachScreenshot` choices. Review after the first run. |
| Level 4 call sites are mixed INFO and PASS, and mis-assignment hides or shows the wrong steps | Review each call site during step 7 of §8. |
| `reference/` shows as untracked in git | Decide whether to commit it or add it to `.gitignore`. Not part of this migration. |
