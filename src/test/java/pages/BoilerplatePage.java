package pages;

import testbase.TheInternetWrapper;

// TODO: Rename the class and file to match the page (e.g. CheckboxesPage), and
// rename src/test/resources/object-repository/BoilerplatePage.properties to match.
/**
 * Page object for the TODO: page name. Locators are in
 * object-repository/BoilerplatePage.properties.
 */
public class BoilerplatePage extends TheInternetWrapper {

    /**
     * Types a value into the example field.
     *
     * @param value the text to be entered in the field.
     * @return the current page, for method chaining.
     */
    // TODO: Rename the method, update the Javadoc, and use the right repository key.
    public BoilerplatePage enterExampleValue(String value) {
        sendKeys(locator("exampleField"), value);
        return this;
    }

    /**
     * Clicks the example button.
     *
     * @return the page shown after the click.
     */
    // TODO: Rename the method, update the Javadoc, and return the correct next
    // page object (or `this` if the click stays on the same page).
    public BoilerplatePage clickExampleButton() {
        click(locator("exampleButton"));
        // Placeholder entries take their values as extra arguments:
        // click(locator("itemCheckbox", "item-1"));
        // An element shared with another page can be read from that page's file:
        // click(locator(LoginPage.class, "loginButton"));
        return new BoilerplatePage();
    }
}
