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

public class TestLoginTest extends TheInternetWrapper {

    @DataProvider(name = "CSV Data Reader With Browsers")
    public Object[][] dataProvider() {
        String folderPath = "Login Cases";
        String fileName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1) + ".csv";
        return csvReaderWithBrowser(folderPath, fileName);
    }

    @Test(dataProvider = "CSV Data Reader With Browsers")
    @Owner("Joseph Mathew")
    @Severity(SeverityLevel.CRITICAL)
    @Feature("Login")
    @Description("Logs in through the Form Authentication page and verifies the success message on the secure page.")
    public void login(String browser, String username, String password) {
        launchBrowser(browser);

        new HomePage().clickFormAuthenticationButton().enterUsername(username).enterPassword(password)
                .clickLoginButton();

        assertThatStringContains(new SecurePage().getSuccessMessage(), "You logged into a secure area!");
    }

}
