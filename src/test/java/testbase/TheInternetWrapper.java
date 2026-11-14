package testbase;

import org.testng.annotations.AfterMethod;

/**
 * Base class for every page object and test class of the application under
 * test. Adds the browser clean-up after each test and shortcuts for reading
 * CSV test data.
 */
public class TheInternetWrapper extends TestBase {

    /**
     * Quits the browser after every test method, pass or fail, so the next
     * test (or retry) starts with a fresh browser.
     */
    @AfterMethod
    public void tearDown() {
        quitBrowser();
    }

    /**
     * Reads a CSV file from test-data. See
     * {@link utilities.DataProvider#csvReader(String, String)}.
     *
     * @param folderPath the subfolder within test-data
     * @param fileName   the CSV file name
     * @return the CSV rows, without the header, as TestNG data provider data
     */
    public Object[][] csvReader(String folderPath, String fileName) {
        return new utilities.DataProvider().csvReader(folderPath, fileName);
    }

    /**
     * Reads a CSV file from test-data and repeats every row for each enabled
     * browser. See
     * {@link utilities.DataProvider#csvReaderWithBrowser(TestBase, String, String)}.
     *
     * @param folderPath the subfolder within test-data
     * @param fileName   the CSV file name
     * @return the CSV rows with the browser name added as the first column
     */
    public Object[][] csvReaderWithBrowser(String folderPath, String fileName) {
        return new utilities.DataProvider().csvReaderWithBrowser(this, folderPath, fileName);
    }
}
