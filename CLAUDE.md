# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Selenium + TestNG UI test framework for https://the-internet.herokuapp.com/. Java 25 (`maven.compiler.release`, so JDK 25 or newer), Maven, AssertJ soft assertions, Allure reporting. All code lives under `src/test` (there is no `src/main`); packages are `pages`, `testcases`, `testbase`, `utilities`. There is no README, linter or CI workflow.

## Commands

Run from the project root: configuration, test data and upload files are read through paths relative to it (`./src/test/resources/...`). In PowerShell, quote every `-D` argument (`"-Dtest=..."`).

- Full suite: `mvn test`. Surefire is pinned to `src/test/resources/regression-test-suite.xml`.
- Single class or method: `mvn test "-Dtest=TestLoginTest"` or `mvn test "-Dtest=TestLoginTest#login"`. `-Dtest` takes precedence over the pinned suite, so no suite override is needed. A data-driven method still runs once per CSV row per enabled browser (about 30 s for one row on three browsers).
- **`-Dtest` runs skip the suite XML, and with it the listeners declared there.** Allure results are still written (`AllureTestNg` also registers itself through the service loader), but there is no retry and, more importantly, no `SoftAssertListener`: a failed `softly()` assertion is logged as a failed step and the test still passes. To judge pass/fail of a soft-asserting test, run it through a suite XML that declares the listeners (the full suite, or a small copy of it).
- Compile only: `mvn test-compile` (or `mvn test -DskipTests`).
- Surefire runs with the AspectJ weaver as a `-javaagent` loaded from `~/.m2` (needed for the Allure step and AssertJ aspects), so dependencies must have been resolved once.
- Reports: results go to `target/allure-results` (`src/test/resources/allure.properties`). The report is built with the Allure 3 CLI (`allure generate target/allure-results`), configured by `allurerc.json`: output `./allure-report`, history in `history.jsonl` (both git-ignored), a quality gate, and the `awesome` plugin with `publish: true`.

## Architecture

**Class hierarchy.** `TestBase` → `TheInternetWrapper` → every test class *and* every page object. `TheInternetWrapper` adds the `@AfterMethod` that quits the browser (only fires on test classes) and the data-reader shortcuts.

**Browser/driver lifecycle (`testbase/TestBase`).** `TestBase` holds `ThreadLocal<RemoteWebDriver>` and `ThreadLocal<Configuration>`. `new SomePage()` picks up the driver already registered for the current thread; no driver is passed around. A test therefore calls `launchBrowser(browser)` first (which opens the base URL and then applies the timeouts) and only then instantiates pages; an object created before the launch has a null `driver` field. Every test invocation and every retry gets a fresh browser.

**Parallelism.** The suite declares `parallel="tests"` with `thread-count="5"` and `data-provider-thread-count="5"`, but it contains a single `<test>`, so nothing runs in parallel across tests or methods. Parallelism comes from the data providers: each test class's provider is declared `@DataProvider(..., parallel = true)`, so the rows of one method (including its browsers) run concurrently on up to 5 threads. Keep that flag on new providers, and keep anything shared between tests thread-local.

**Element actions.** `TestBase` provides the wrapped primitives (`click`, `sendKeys`, `getText`, `clear`, `getAttribute`, `uploadFile`, alert helpers, `isDisplayed`/`isEnabled`/`isSelected`). Each does an explicit wait, then the action, inside an Allure step. Page objects build on these rather than calling Selenium directly: private `By` locators, fluent methods returning `this` or the next page object.
- **Locator strategy.** Prefer, in this order: ID → Name → CSS selector → short relative XPath, and fall back to the rest only when needed:
  1. **ID**: fastest and most reliable; use it whenever it is present and stable (beware of dynamically generated IDs).
  2. **Name**: good when unique, which is common on form fields.
  3. **CSS selector**: fast, readable and powerful (classes, attributes, parent/child); the best choice when there is no good ID.
  4. **XPath**: the most flexible (can go up to parents, match on text); keep it short and relative, e.g. `//button[text()='Submit']`.
  5. **Class name**: single class only and rarely unique, so fragile unless the class is distinctive.
  6. **Tag name**: mainly for collections (all `<a>` or `<tr>`), not for one specific element.
  7. **Link text / partial link text**: anchors only; breaks when the text changes or the site is localized.

  The order is about uniqueness, stability and readability, not speed (the performance difference between ID, CSS and XPath is negligible). Avoid absolute XPath (`/html/body/div[2]/...`) and index-based locators, which break on layout changes. If a dedicated test hook such as `data-testid` or `data-test` exists, target it with CSS (`[data-testid='login-btn']`). Selenium 4 relative locators (`above()`, `below()`, `toRightOf()`) are a fallback, not a first choice.
- The `is*` checks do not wait explicitly and return false for a missing element, but the implicit wait (10 s) still applies while the element is looked up.
- For a one-off longer wait, use `new TestBase(configuration)` with a modified copy of the configuration; it leaves the thread's configuration untouched (see `HomePage.clickTwoFactorLoginButton`).
- `uploadFile` resolves file names against `src/test/resources/test-data/Upload Files`.

**Allure steps (`utilities/AllureWrapper`).** All steps go through `AllureWrapper.step(...)`, which also writes to the TestNG reporter/console. Steps that wrap a block always attach a screenshot when the block ends, pass or fail; plain log steps attach one unless `ScreenshotOption.SKIP` is passed, which is what inner log lines should use. The driver is found through a thread-local `TestBase` set in `launchBrowser`; a screenshot that cannot be taken becomes a BROKEN step instead of an error.

**Failure convention.** Framework code reports a problem by writing an Allure step with the message and calling `Assert.fail(message, cause)`, rather than letting the raw exception escape.

**Configuration (`utilities/Configuration` + `src/test/resources/configuration.properties`).** The properties file is loaded once per JVM; each `new Configuration()` copies the values into public instance fields, so one instance can be changed without affecting others. A missing or non-numeric number fails via `Assert.fail`, but booleans use `Boolean.parseBoolean`, so a missing or misspelled flag silently becomes `false`. The file holds the base URL, timeouts (seconds), browser enablement flags (`browser.browser-execution.*`), headless mode, window size and `retry-logic.max-retries`.

**Data-driven tests (`utilities/DataProvider`).** Despite the name this is not the TestNG annotation; it reads CSV / XLSX / JSON from `src/test/resources/test-data/<folder>/<TestClassName>.<ext>` (folder names contain spaces, e.g. `Login Cases`). The first row is a header and is skipped; values arrive as trimmed Strings, except nested JSON objects and arrays, which arrive as Jackson `JsonNode`s. The `*WithBrowser` variants repeat every row once per enabled browser and prepend the browser name, so test methods take `String browser` as their first parameter. Test classes expose these through a `@DataProvider` method that calls the `TheInternetWrapper` shortcuts and derives the file name from the class name, so renaming a test class means renaming its data file.

**Listeners (registered only in the suite XML).**
- `AnnotationTransformer` attaches `RetryAnalyzer` to every test. With `retry-logic.max-retries=3`, a failing invocation runs up to four times before it is reported, each with its own browser.
- `SoftAssertListener` + `SoftAssert` give each test invocation its own AssertJ `SoftAssertions` (use `softly()` in tests; never call `assertAll()` yourself) and verify them after the method, turning collected failures into a test failure.
- `AllureTestNg` writes the results.

**Adding a test.** `testcases/BoilerplateTest` and `pages/BoilerplatePage` are the templates (full of `TODO`s, including the commented-out JSON/XLSX provider variants). Copy and rename them and add the data file named after the new test class. The suite uses `<package name="testcases"/>`, so any new class in `testcases` is picked up automatically; `BoilerplateTest` itself is excluded explicitly in the suite XML.

**Agents and specs.** `.claude/agents` (mirrored for Copilot in `.github/agents`) holds three agents: `selenium-test-planner` explores a page in a browser and saves a plan to `specs/`, `selenium-test-generator` turns a plan scenario into a page object, test class and CSV file, and `selenium-test-healer` runs tests and fixes failures. `specs/` currently holds only a placeholder `README.md`.
