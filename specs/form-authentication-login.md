# Form Authentication Login Test Spec

## Application overview
The Form Authentication page on https://the-internet.herokuapp.com/ (reached from the landing page via the "Form Authentication" link). The user enters a username and password and presses Login. On success the application opens the secure area, which shows a flash message confirming the login.

This spec describes the behaviour automated by `testcases/TestLoginTest.java`. It was written from the test and page object code. All locators below were later verified live (each resolves to exactly one element, and a valid login ends on `/secure` with the success flash message).

## Page elements
| Element | Locator (By) | Page object | Notes |
|---------|--------------|-------------|-------|
| Form Authentication link (landing page) | Used by `HomePage.clickFormAuthenticationButton()` | `HomePage` | Opens the login page |
| Username field | `By.id("username")` | `LoginPage` | |
| Password field | `By.id("password")` | `LoginPage` | |
| Login button | `By.cssSelector("button[type='submit']")` | `LoginPage` | Returns `SecurePage` |
| Flash message (secure area) | `By.id("flash")` | `SecurePage` | Holds the success text |

## Existing coverage
- `testcases.TestLoginTest.login(String browser, String username, String password)`, data-driven from `src/test/resources/test-data/Login Cases/TestLoginTest.csv`.

## Scenarios

### 1. Login
**Suggested names:** test class `TestLoginTest` (existing), page objects `HomePage` and `LoginPage` (existing), data folder `Login Cases`
**Severity:** CRITICAL
**Data columns:** `Username`, `Password` (the framework adds `browser` as the first parameter)

#### 1.1 Successful login with valid credentials (automated)
**Steps:**
1. Launch the browser on the base URL.
2. Click the "Form Authentication" link on the landing page.
3. Enter the username from the data row in the username field.
4. Enter the password from the data row in the password field.
5. Click Login.

**Expected results:**
- The secure area is shown.
- The flash message contains `You logged into a secure area!`.

**Data rows:**
| Username | Password |
|----------|----------|
| Row 1 of `TestLoginTest.csv` (currently a valid demo account) | Row 1 of `TestLoginTest.csv` |

Data-driven notes: the test runs once per CSV row per enabled browser. Adding a row to the CSV adds a run for every browser without any code change.

## Not yet automated (candidate scenarios)
These are not covered by `TestLoginTest` today. They are listed as possible additions, and their expected results must be confirmed on the live page before they are automated:
- Invalid password for a valid username: the page should stay on the login form and show an error flash message.
- Unknown username: same as above.
- Empty username or password: the form should not log in.
- Logout after a successful login returns to the login page.
