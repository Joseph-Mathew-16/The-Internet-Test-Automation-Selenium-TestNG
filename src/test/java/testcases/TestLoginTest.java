package testcases;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import pages.HomePage;
import pages.SecurePage;
import testbase.TheInternetWrapper;

/**
 * Tests for the Form Authentication login. Test data comes from
 * test-data/Login Cases/TestLoginTest.csv.
 */
public class TestLoginTest extends TheInternetWrapper {

    /**
     * Supplies one iteration per CSV row per enabled browser. The CSV file is
     * named after this class, so renaming the class means renaming the file.
     */
    @DataProvider(name = "CSV Data Reader With Browsers", parallel = true)
    public Object[][] dataProvider() {
        String folderPath = "Login Cases";
        // Simple class name, e.g. "TestLoginTest", used as the CSV file name.
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    /**
     * Logs in with the given credentials and checks the success message.
     *
     * @param browser  the browser to run on (first column added by the data provider)
     * @param username the username from the CSV file
     * @param password the password from the CSV file
     */
    @Test(dataProvider = "CSV Data Reader With Browsers")
    @Owner("Joseph Mathew")
    @Severity(SeverityLevel.CRITICAL)
    @Feature("Login")
    @Description("Logs in through the Form Authentication page and verifies the success message on the secure page.")
    public void login(String browser, String username, String password) {
        launchBrowser(browser);

        new HomePage().clickFormAuthenticationButton().enterUsername(username).enterPassword(password)
                .clickLoginButton();

        // Soft assertion: a failure is recorded but verified only when the test ends.
        softly().assertThat(new SecurePage().getSuccessMessage()).contains("You logged into a secure area!");
    }

}
