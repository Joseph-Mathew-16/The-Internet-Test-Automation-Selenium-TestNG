package pages;

import org.openqa.selenium.By;

import testbase.TheInternetWrapper;

/**
 * Page object for the Radio Buttons page, which has a "Favorite color" group
 * and a "Favorite sport" group and shows the current selection in a result line.
 */
public class RadioButtonsPage extends TheInternetWrapper {

    /**
     * Locators for the elements on this page.
     */
    private final By resultText = By.id("result");

    /**
     * Builds the locator of a colour radio button; the radio ids equal the colour values.
     *
     * @param color the colour value, e.g. "green".
     * @return the locator of that colour radio button.
     */
    private By colorRadio(String color) {
        return By.id(color);
    }

    /**
     * Builds the locator of a sport radio button; sport radios have no id, so the
     * value attribute is used.
     *
     * @param sport the sport value, e.g. "tennis".
     * @return the locator of that sport radio button.
     */
    private By sportRadio(String sport) {
        return By.cssSelector("input[name='sport'][value='" + sport + "']");
    }

    /**
     * Selects a colour radio button.
     *
     * @param color the colour value, e.g. "green".
     * @return the current page, for method chaining.
     */
    public RadioButtonsPage selectColor(String color) {
        click(colorRadio(color));
        return this;
    }

    /**
     * Selects a sport radio button.
     *
     * @param sport the sport value, e.g. "tennis".
     * @return the current page, for method chaining.
     */
    public RadioButtonsPage selectSport(String sport) {
        click(sportRadio(sport));
        return this;
    }

    /**
     * Checks whether a colour radio button is selected.
     *
     * @param color the colour value, e.g. "green".
     * @return true if the radio button is selected.
     */
    public boolean isColorSelected(String color) {
        return isSelected(colorRadio(color));
    }

    /**
     * Checks whether a sport radio button is selected.
     *
     * @param sport the sport value, e.g. "tennis".
     * @return true if the radio button is selected.
     */
    public boolean isSportSelected(String sport) {
        return isSelected(sportRadio(sport));
    }

    /**
     * Reads the result line that shows the current selection.
     *
     * @return the result text, e.g. "Color: green, Sport: tennis".
     */
    public String getResultText() {
        return getText(resultText);
    }
}
