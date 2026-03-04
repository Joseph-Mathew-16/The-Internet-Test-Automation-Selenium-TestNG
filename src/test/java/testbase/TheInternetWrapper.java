package testbase;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import utilities.ChainTestReporter;

@Listeners(ChainTestReporter.class)

public class TheInternetWrapper extends TestBase {

    @AfterMethod
    public void tearDown() {
        quitBrowser();
    }

    public Object[][] csvReader(String folderPath, String fileName) {
        return new utilities.DataProvider().csvReader(this, folderPath, fileName);
    }

    public Object[][] csvReaderWithBrowser(String folderPath, String fileName) {
        return new utilities.DataProvider().csvReaderWithBrowser(this, folderPath, fileName);
    }
}
