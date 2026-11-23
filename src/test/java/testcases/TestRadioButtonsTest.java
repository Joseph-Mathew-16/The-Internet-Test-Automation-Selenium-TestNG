package testcases;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import pages.HomePage;
import pages.RadioButtonsPage;
import testbase.TheInternetWrapper;

/**
 * Tests for the Radio Buttons page. Test data comes from
 * test-data/Radio Button Cases/TestRadioButtonsTest.csv.
 */
public class TestRadioButtonsTest extends TheInternetWrapper {

    /**
     * Supplies one iteration per CSV row per enabled browser. The CSV file is
     * named after this class, so renaming the class means renaming the file.
     */
    @DataProvider(name = "CSV Data Reader With Browsers", parallel = true)
    public Object[][] dataProvider() {
        String folderPath = "Radio Button Cases";
        // Simple class name, e.g. "TestRadioButtonsTest", used as the CSV file name.
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    /**
     * Selects a colour and a sport and verifies the selection and the result text.
     *
     * @param browser        the browser to run on (first column added by the data provider)
     * @param color          the colour to select, e.g. "green"
     * @param sport          the sport to select, e.g. "tennis"
     * @param expectedResult the result text the page should display
     */
    @Test(dataProvider = "CSV Data Reader With Browsers", groups = { "regression", "radio-buttons" })
    @Owner("Joseph Mathew")
    @Severity(SeverityLevel.NORMAL)
    @Feature("Radio Buttons")
    @Description("Selects a colour and a sport on the Radio Buttons page and verifies the selection and the displayed result text.")
    public void selectColorAndSport(String browser, String color, String sport, String expectedResult) {
        launchBrowser(browser);

        // Click the "Radio Buttons" link on the landing page.
        new HomePage().clickRadioButtonsButton();

        // Select the colour radio button, then the sport radio button.
        RadioButtonsPage page = new RadioButtonsPage().selectColor(color).selectSport(sport);

        // Soft assertions: failures are recorded but verified only when the test ends.
        softly().assertThat(page.isColorSelected(color)).isTrue();
        softly().assertThat(page.isSportSelected(sport)).isTrue();
        softly().assertThat(page.isColorSelected("blue")).isFalse();
        softly().assertThat(page.getResultText()).isEqualTo(expectedResult);
    }
}
