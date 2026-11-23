package testbase;

import org.testng.annotations.AfterMethod;

/**
 * Base class for every page object and test class of the application under
 * test. Adds the browser clean-up after each test and shortcuts for reading
 * CSV, XLSX and JSON test data.
 */
public class TheInternetWrapper extends TestBase {

    /**
     * Quits the browser after every test method, pass or fail, so the next
     * test (or retry) starts with a fresh browser. {@code alwaysRun} makes it run
     * when a suite selects tests by group: otherwise TestNG skips configuration
     * methods that are not in an included group and the browsers stay open.
     */
    @AfterMethod(alwaysRun = true)
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

    /**
     * Reads the first sheet of an XLSX file from test-data. See
     * {@link utilities.DataProvider#xlsxReader(String, String)}.
     *
     * @param folderPath the subfolder within test-data
     * @param fileName   the XLSX file name
     * @return the XLSX rows, without the header, as TestNG data provider data
     */
    public Object[][] xlsxReader(String folderPath, String fileName) {
        return new utilities.DataProvider().xlsxReader(folderPath, fileName);
    }

    /**
     * Reads an XLSX file from test-data and repeats every row for each enabled
     * browser. See
     * {@link utilities.DataProvider#xlsxReaderWithBrowser(TestBase, String, String)}.
     *
     * @param folderPath the subfolder within test-data
     * @param fileName   the XLSX file name
     * @return the XLSX rows with the browser name added as the first column
     */
    public Object[][] xlsxReaderWithBrowser(String folderPath, String fileName) {
        return new utilities.DataProvider().xlsxReaderWithBrowser(this, folderPath, fileName);
    }

    /**
     * Reads a JSON file from test-data. See
     * {@link utilities.DataProvider#jsonReader(String, String)}.
     *
     * @param folderPath the subfolder within test-data
     * @param fileName   the JSON file name
     * @return the JSON objects as TestNG data provider data; nested values are
     *         returned as Jackson JsonNodes
     */
    public Object[][] jsonReader(String folderPath, String fileName) {
        return new utilities.DataProvider().jsonReader(folderPath, fileName);
    }

    /**
     * Reads a JSON file from test-data and repeats every object for each enabled
     * browser. See
     * {@link utilities.DataProvider#jsonReaderWithBrowser(TestBase, String, String)}.
     *
     * @param folderPath the subfolder within test-data
     * @param fileName   the JSON file name
     * @return the JSON objects with the browser name added as the first column
     */
    public Object[][] jsonReaderWithBrowser(String folderPath, String fileName) {
        return new utilities.DataProvider().jsonReaderWithBrowser(this, folderPath, fileName);
    }
}
