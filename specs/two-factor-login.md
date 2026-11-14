# Two-Factor Login Test Spec

## Application overview
The Two-Factor Login pages on https://the-internet.herokuapp.com/ (`/two_factor`, reached from the landing page via the "Two-Factor Login" link). The login is a two-step flow on the same page:
1. The user enters a username and password and presses Next.
2. The application shows a one-time password (OTP) on screen (it does not send it anywhere). The user enters the OTP and presses Verify.

On success the secure area is shown with a flash message confirming two-factor login.

This spec describes the behaviour automated by `testcases/TwoFactorLoginTest.java`. It was written from the test and page object code. All locators below were later verified live (each resolves to exactly one element, and the flow ends on `/two_factor` with the success flash message).

## Page elements
| Element | Locator (By) | Page object | Notes |
|---------|--------------|-------------|-------|
| Two-Factor Login link (landing page) | Used by `HomePage.clickTwoFactorLoginButton()` | `HomePage` | Uses a longer one-off wait, see `HomePage` |
| Username field | `By.id("username")` | `TwoFactorLoginPage` | Step 1 |
| Password field | `By.id("password")` | `TwoFactorLoginPage` | Step 1 |
| Next / Verify button | `By.cssSelector("button[type='submit']")` | `TwoFactorLoginPage` | Same button for both steps. Step 1 is "Next", step 2 is "Verify" |
| Displayed OTP | `By.id("otp-code")` | `TwoFactorLoginPage` | Read with `getOtpCodeText()`. The application shows the code, so the test reads it from the page |
| OTP input field | `By.id("code")` | `TwoFactorLoginPage` | Step 2 |
| Flash message (secure area) | `By.id("flash")` | `SecurePage` | Holds the success text |

## Existing coverage
- `testcases.TwoFactorLoginTest.successfulTwoFactorLogin(String browser, String username, String password)`, data-driven from `src/test/resources/test-data/Login Cases/TwoFactorLoginTest.csv`.

## Scenarios

### 1. Two-Factor Login
**Suggested names:** test class `TwoFactorLoginTest` (existing), page object `TwoFactorLoginPage` (existing), data folder `Login Cases`
**Severity:** NORMAL (the existing class has no `@Severity`, `@Feature` or `@Owner` annotations; add them if the Allure report should group it with the other login tests)
**Data columns:** `Username`, `Password` (the framework adds `browser` as the first parameter)

#### 1.1 Successful two-factor login (automated)
**Steps:**
1. Launch the browser on the base URL.
2. Click the "Two-Factor Login" link on the landing page.
3. Enter the username and password from the data row.
4. Click Next.
5. Read the OTP shown on the page.
6. Enter the OTP in the code field.
7. Click Verify.

**Expected results:**
- The OTP shown on the page is read and entered without error.
- The secure area is shown.
- The flash message contains `You logged in with two-factor authentication!`.

**Data rows:**
| Username | Password |
|----------|----------|
| Row 1 of `TwoFactorLoginTest.csv` (currently a valid demo account) | Row 1 of `TwoFactorLoginTest.csv` |

Data-driven notes: the test runs once per CSV row per enabled browser.

## Not yet automated (candidate scenarios)
These are not covered by `TwoFactorLoginTest` today. Confirm the expected results on the live page before automating them:
- Wrong OTP: the secure area should not be shown, and an error should appear. Needs an OTP data column or a fixed wrong value.
- Invalid username or password at step 1: the flow should not reach the OTP step.
- Empty OTP at step 2: the Verify action should not log in.
