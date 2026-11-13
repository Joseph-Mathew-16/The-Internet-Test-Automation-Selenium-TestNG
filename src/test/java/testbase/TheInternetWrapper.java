package testbase;

import org.testng.annotations.AfterMethod;

public class TheInternetWrapper extends TestBase {

    @AfterMethod
    public void tearDown() {
        quitBrowser();
    }

    public Object[][] csvReader(String folderPath, String fileName) {
        return new utilities.DataProvider().csvReader(folderPath, fileName);
    }

    public Object[][] csvReaderWithBrowser(String folderPath, String fileName) {
        return new utilities.DataProvider().csvReaderWithBrowser(this, folderPath, fileName);
    }
}
