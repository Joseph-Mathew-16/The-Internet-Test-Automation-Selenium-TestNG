package pages;

import testbase.TheInternetWrapper;

/**
 * Page object for the Radio Buttons page, which has a "Favorite color" group
 * and a "Favorite sport" group and shows the current selection in a result line.
 * Locators are in object-repository/RadioButtonsPage.properties; the radio
 * button entries take the colour or sport value as their {0} placeholder.
 */
public class RadioButtonsPage extends TheInternetWrapper {

    /**
     * Selects a colour radio button.
     *
     * @param color the colour value, e.g. "green".
     * @return the current page, for method chaining.
     */
    public RadioButtonsPage selectColor(String color) {
        click(locator("colorRadio", color));
        return this;
    }

    /**
     * Selects a sport radio button.
     *
     * @param sport the sport value, e.g. "tennis".
     * @return the current page, for method chaining.
     */
    public RadioButtonsPage selectSport(String sport) {
        click(locator("sportRadio", sport));
        return this;
    }

    /**
     * Checks whether a colour radio button is selected.
     *
     * @param color the colour value, e.g. "green".
     * @return true if the radio button is selected.
     */
    public boolean isColorSelected(String color) {
        return isSelected(locator("colorRadio", color));
    }

    /**
     * Checks whether a sport radio button is selected.
     *
     * @param sport the sport value, e.g. "tennis".
     * @return true if the radio button is selected.
     */
    public boolean isSportSelected(String sport) {
        return isSelected(locator("sportRadio", sport));
    }

    /**
     * Reads the result line that shows the current selection.
     *
     * @return the result text, e.g. "Color: green, Sport: tennis".
     */
    public String getResultText() {
        return getText(locator("resultText"));
    }
}
