package testcases;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pages.HomePage;
import pages.SecurePage;
import pages.TwoFactorLoginPage;
import testbase.TheInternetWrapper;

public class TwoFactorLoginTest extends TheInternetWrapper {

    @DataProvider(name = "CSV Data Reader With Browsers")
    public Object[][] dataProvider() {
        String folderPath = "Login Cases";
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    @Test(dataProvider = "CSV Data Reader With Browsers")
    public void successfulTwoFactorLogin(String browser, String username, String password) {
        launchBrowser(browser);

        new HomePage().clickTwoFactorLoginButton().enterUsername(username).enterPassword(password).clickNextButton();

        String otpCode = new TwoFactorLoginPage().getOtpCodeText();

        new TwoFactorLoginPage().enterOtp(otpCode).clickVerifyButton();

        assertThatStringContains(new SecurePage().getSuccessMessage(), "You logged in with two-factor authentication!");
    }
}