package pages;

import org.openqa.selenium.By;
import testbase.TheInternetWrapper;

/**
 * This class represents the Secure Page of the application.
 */
public class SecurePage extends TheInternetWrapper {

    /**
     * Locators for the elements on the Home Page
     */
    private final By successMessage = By.id("flash");

    /**
     * Method to get the success message displayed on the Secure Page after
     * successful login.
     *
     * @return String - the success message text displayed on the Secure Page.
     */
    public String getSuccessMessage() {
        return getText(successMessage);
    }
}
