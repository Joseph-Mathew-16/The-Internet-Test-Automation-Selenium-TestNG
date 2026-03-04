package pages;

import org.openqa.selenium.By;
import testbase.TheInternetWrapper;

/**
 * This class represents the Login Page of the application.
 */
public class LoginPage extends TheInternetWrapper {

    /**
     * Locators for the login page elements
     */
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.xpath("//button[@type=\"submit\"]");

    /**
     * Method to enter username in username field.
     *
     * @param username - the username to be entered in the username field.
     * @return LoginPage - returns the current instance of LoginPage for method
     * chaining.
     */
    public LoginPage enterUsername(String username) {
        enterString(usernameField, username);
        return this;
    }

    /**
     * Method to enter password in password field.
     *
     * @param password - the password to be entered in the password field.
     * @return LoginPage - returns the current instance of LoginPage for method
     * chaining.
     */
    public LoginPage enterPassword(String password) {
        enterString(passwordField, password);
        return this;
    }

    /**
     * Method to click login button.
     *
     * @return SecurePage - returns a SecurePage object for method chaining.
     */
    public SecurePage clickLoginButton() {
        click(loginButton);
        return new SecurePage();
    }
}
