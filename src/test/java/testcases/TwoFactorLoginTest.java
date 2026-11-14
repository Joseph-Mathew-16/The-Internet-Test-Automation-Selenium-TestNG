package testcases;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pages.HomePage;
import pages.SecurePage;
import pages.TwoFactorLoginPage;
import testbase.TheInternetWrapper;

/**
 * Tests for the Two-Factor Login. Test data comes from
 * test-data/Login Cases/TwoFactorLoginTest.csv.
 */
public class TwoFactorLoginTest extends TheInternetWrapper {

    /**
     * Supplies one iteration per CSV row per enabled browser. The CSV file is
     * named after this class, so renaming the class means renaming the file.
     */
    @DataProvider(name = "CSV Data Reader With Browsers")
    public Object[][] dataProvider() {
        String folderPath = "Login Cases";
        // Simple class name, e.g. "TwoFactorLoginTest", used as the CSV file name.
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    /**
     * Logs in with username and password, reads the one-time password the page
     * displays, submits it and checks the success message.
     *
     * @param browser  the browser to run on (first column added by the data provider)
     * @param username the username from the CSV file
     * @param password the password from the CSV file
     */
    @Test(dataProvider = "CSV Data Reader With Browsers")
    public void successfulTwoFactorLogin(String browser, String username, String password) {
        launchBrowser(browser);

        new HomePage().clickTwoFactorLoginButton().enterUsername(username).enterPassword(password).clickNextButton();

        // The application shows the OTP on screen instead of sending it, so the test reads it from the page.
        String otpCode = new TwoFactorLoginPage().getOtpCodeText();

        new TwoFactorLoginPage().enterOtp(otpCode).clickVerifyButton();

        // Soft assertion: a failure is recorded but verified only when the test ends.
        softly().assertThat(new SecurePage().getSuccessMessage()).contains("You logged in with two-factor authentication!");
    }
}
