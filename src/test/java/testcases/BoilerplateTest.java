package testcases;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import pages.BoilerplatePage; // TODO: Import the page objects this test uses.
import pages.HomePage;
import testbase.TheInternetWrapper;

// TODO: Rename the class and file to match the feature under test (e.g. TestCheckboxesTest).
/**
 * Tests for TODO: feature name. Test data comes from
 * test-data/TODO folder/BoilerplateTest.csv.
 */
public class BoilerplateTest extends TheInternetWrapper {

    /**
     * Supplies one iteration per CSV row per enabled browser. The CSV file is
     * named after this class, so renaming the class means renaming the file.
     */
    @DataProvider(name = "CSV Data Reader With Browsers", parallel = true)
    public Object[][] dataProvider() {
        // TODO: Set the folder under test-data that holds this test's CSV file.
        String folderPath = "TODO folder";
        // Simple class name, e.g. "BoilerplateTest", used as the CSV file name.
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    // To use JSON or XLSX test data instead of CSV: uncomment one of the data
    // providers below and point the @Test's dataProvider at its name. The file
    // is named after this class (BoilerplateTest.json / BoilerplateTest.xlsx).
    // TODO: Delete the data providers that are not used.

    // /**
    //  * Supplies one iteration per JSON object per enabled browser. The file must
    //  * be a non-empty array of objects; the keys of the first object decide the
    //  * column order. Nested objects and arrays arrive as JsonNode parameters.
    //  */
    // @DataProvider(name = "JSON Data Reader With Browsers", parallel = true)
    // public Object[][] jsonDataProvider() {
    //     // TODO: Set the folder under test-data that holds this test's JSON file.
    //     String folderPath = "TODO folder";
    //     String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".json";
    //     return jsonReaderWithBrowser(folderPath, fileName);
    // }

    // /**
    //  * Supplies one iteration per XLSX row per enabled browser.
    //  */
    // @DataProvider(name = "XLSX Data Reader With Browsers", parallel = true)
    // public Object[][] xlsxDataProvider() {
    //     // TODO: Set the folder under test-data that holds this test's XLSX file.
    //     String folderPath = "TODO folder";
    //     String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".xlsx";
    //     return xlsxReaderWithBrowser(folderPath, fileName);
    // }

    /**
     * TODO: Describe what this test does.
     *
     * @param browser the browser to run on (first column added by the data provider)
     * @param value   TODO: describe this CSV column
     */
    // TODO: Rename the method and match the parameters to the CSV columns
    // (after the leading browser column).
    // TODO: Change the dataProvider name if using the JSON or XLSX provider.
    // TODO: Add the feature group after "regression" (lowercase and hyphenated, e.g. "check-boxes",
    // reusing an existing one where it fits), and "smoke" if the test is a critical, fast check.
    @Test(dataProvider = "CSV Data Reader With Browsers", groups = { "regression" })
    @Owner("TODO: Owner name")
    @Severity(SeverityLevel.NORMAL) // TODO: Pick the right severity.
    @Feature("TODO: Feature name")
    @Description("TODO: Describe the test for the Allure report.")
    public void exampleTest(String browser, String value) {
        launchBrowser(browser);

        // TODO: Replace with the real navigation and page actions.
        new HomePage();
        BoilerplatePage page = new BoilerplatePage();
        page.enterExampleValue(value);

        // Soft assertion: a failure is recorded but verified only when the test ends.
        // TODO: Replace with the real expected result.
        softly().assertThat(value).isNotNull();
    }

}
