package pages;

import java.time.Duration;

import org.openqa.selenium.By;

import testbase.TestBase;
import testbase.TheInternetWrapper;
import utilities.Configuration;

/**
 * Page object for the application's home page, which lists links to the
 * example pages.
 */
public class HomePage extends TheInternetWrapper {

    /**
     * Locators for the elements on the Home Page.
     */
    private final By formAuthenticationButton = By.linkText("Form Authentication");
    private final By twoFactorLoginButton = By.linkText("Two-Factor Login");

    /**
     * Clicks the Form Authentication link and navigates to the Login Page.
     *
     * @return LoginPage object representing the Login Page
     */
    public LoginPage clickFormAuthenticationButton() {
        click(formAuthenticationButton);
        return new LoginPage();
    }

    /**
     * Clicks the Two-Factor Login link and navigates to the Two-Factor Login
     * Page. This click needs a longer wait than the default, so it uses a
     * temporary Configuration with a 30 second explicit wait; the thread's
     * Configuration is left unchanged.
     *
     * @return TwoFactorLoginPage instance representing the next page
     */
    public TwoFactorLoginPage clickTwoFactorLoginButton() {
        Configuration configuration = new Configuration();
        configuration.timeoutsDurationsExplicitWait = Duration.ofSeconds(30);
        new TestBase(configuration).click(twoFactorLoginButton);
        return new TwoFactorLoginPage();
    }
}
