package pages;

import org.openqa.selenium.By;

import testbase.TheInternetWrapper;

/**
 * Page object for the secure area shown after a successful login.
 */
public class SecurePage extends TheInternetWrapper {

    /**
     * Locators for the elements on the Secure Page.
     */
    private final By successMessage = By.id("flash");

    /**
     * Gets the flash message displayed on the Secure Page after login.
     *
     * @return the text of the flash message.
     */
    public String getSuccessMessage() {
        return getText(successMessage);
    }
}
