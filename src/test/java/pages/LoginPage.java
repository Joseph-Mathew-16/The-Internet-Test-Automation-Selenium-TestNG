package pages;

import testbase.TheInternetWrapper;

/**
 * Page object for the Form Authentication login page. Locators are in
 * object-repository/LoginPage.properties.
 */
public class LoginPage extends TheInternetWrapper {

    /**
     * Types the username into the username field.
     *
     * @param username the username to be entered in the username field.
     * @return the current LoginPage, for method chaining.
     */
    public LoginPage enterUsername(String username) {
        sendKeys(locator("usernameField"), username);
        return this;
    }

    /**
     * Types the password into the password field.
     *
     * @param password the password to be entered in the password field.
     * @return the current LoginPage, for method chaining.
     */
    public LoginPage enterPassword(String password) {
        sendKeys(locator("passwordField"), password);
        return this;
    }

    /**
     * Clicks the login button.
     *
     * @return the SecurePage shown after a successful login.
     */
    public SecurePage clickLoginButton() {
        click(locator("loginButton"));
        return new SecurePage();
    }
}
