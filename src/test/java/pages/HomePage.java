package pages;

import java.time.Duration;

import testbase.TestBase;
import testbase.TheInternetWrapper;
import utilities.Configuration;

/**
 * Page object for the application's home page, which lists links to the
 * example pages. Locators are in object-repository/HomePage.properties.
 */
public class HomePage extends TheInternetWrapper {

    /**
     * Clicks the Form Authentication link and navigates to the Login Page.
     *
     * @return LoginPage object representing the Login Page
     */
    public LoginPage clickFormAuthenticationButton() {
        click(locator("formAuthenticationButton"));
        return new LoginPage();
    }

    /**
     * Clicks the Two-Factor Login link and navigates to the Two-Factor Login
     * Page. This click needs a longer wait than the default, so it uses a
     * temporary Configuration with a 30 second explicit wait; the thread's
     * Configuration is left unchanged. The locator is read on this page object,
     * not on the temporary TestBase, so it comes from HomePage.properties.
     *
     * @return TwoFactorLoginPage instance representing the next page
     */
    public TwoFactorLoginPage clickTwoFactorLoginButton() {
        Configuration configuration = new Configuration();
        configuration.timeoutsDurationsExplicitWait = Duration.ofSeconds(30);
        new TestBase(configuration).click(locator("twoFactorLoginButton"));
        return new TwoFactorLoginPage();
    }

    /**
     * Clicks the Radio Buttons link and navigates to the Radio Buttons Page.
     *
     * @return RadioButtonsPage object representing the Radio Buttons Page
     */
    public RadioButtonsPage clickRadioButtonsButton() {
        click(locator("radioButtonsButton"));
        return new RadioButtonsPage();
    }
}
