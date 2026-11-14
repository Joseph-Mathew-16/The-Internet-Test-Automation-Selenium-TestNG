package pages;

import org.openqa.selenium.By;

import testbase.TheInternetWrapper;

// TODO: Rename the class and file to match the page (e.g. CheckboxesPage).
/**
 * Page object for the TODO: page name.
 */
public class BoilerplatePage extends TheInternetWrapper {

    /**
     * Locators for the elements on this page.
     */
    // TODO: Replace with the real locators for this page.
    private final By exampleField = By.id("TODO");
    private final By exampleButton = By.id("TODO");

    /**
     * Types a value into the example field.
     *
     * @param value the text to be entered in the field.
     * @return the current page, for method chaining.
     */
    // TODO: Rename the method, update the Javadoc, and use the right locator.
    public BoilerplatePage enterExampleValue(String value) {
        sendKeys(exampleField, value);
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
        click(exampleButton);
        return new BoilerplatePage();
    }
}
