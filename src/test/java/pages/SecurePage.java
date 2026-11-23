package pages;

import testbase.TheInternetWrapper;

/**
 * Page object for the secure area shown after a successful login. Locators are
 * in object-repository/SecurePage.properties.
 */
public class SecurePage extends TheInternetWrapper {

    /**
     * Gets the flash message displayed on the Secure Page after login.
     *
     * @return the text of the flash message.
     */
    public String getSuccessMessage() {
        return getText(locator("successMessage"));
    }
}
