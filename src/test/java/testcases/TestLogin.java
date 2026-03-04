package testcases;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SecurePage;
import testbase.TheInternetWrapper;

public class TestLogin extends TheInternetWrapper {

    @DataProvider(name = "CSV Data Reader With Browsers")
    public Object[][] dataProvider() {
        String folderPath = "Login Cases";
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    @Test(dataProvider = "CSV Data Reader With Browsers")
    public void login(String browser, String username, String password) {
        launchBrowser(browser);

        new HomePage().clickFormAuthenticationButton().enterUsername(username).enterPassword(password)
                .clickLoginButton();

        assertThatStringContains(new SecurePage().getSuccessMessage(), "You logged into a secure area!");
    }

}
