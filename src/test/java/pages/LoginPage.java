package pages;

import org.openqa.selenium.By;

import testbase.TheInternetWrapper;

/**
 * Page object for the Form Authentication login page.
 */
public class LoginPage extends TheInternetWrapper {

    /**
     * Locators for the login page elements.
     */
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.xpath("//button[@type=\"submit\"]");

    /**
     * Types the username into the username field.
     *
     * @param username the username to be entered in the username field.
     * @return the current LoginPage, for method chaining.
     */
    public LoginPage enterUsername(String username) {
        sendKeys(usernameField, username);
        return this;
    }

    /**
     * Types the password into the password field.
     *
     * @param password the password to be entered in the password field.
     * @return the current LoginPage, for method chaining.
     */
    public LoginPage enterPassword(String password) {
        sendKeys(passwordField, password);
        return this;
    }

    /**
     * Clicks the login button.
     *
     * @return the SecurePage shown after a successful login.
     */
    public SecurePage clickLoginButton() {
        click(loginButton);
        return new SecurePage();
    }
}
