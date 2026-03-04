package pages;

import org.openqa.selenium.By;
import testbase.TheInternetWrapper;

/**
 * This class represents the Home Page of the application.
 */
public class HomePage extends TheInternetWrapper {

    /**
     * Locators for the elements on the Home Page
     */
    private final By formAuthenticationButton = By.linkText("Form Authentication");

    /**
     * Method to click on the Form Authentication button and navigate to the Login
     * Page
     *
     * @return LoginPage object representing the Login Page
     */
    public LoginPage clickFormAuthenticationButton() {
        click(formAuthenticationButton);
        return new LoginPage();
    }
}
