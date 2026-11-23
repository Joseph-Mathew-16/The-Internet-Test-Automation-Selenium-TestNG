package pages;

import testbase.TheInternetWrapper;

/**
 * Page object for the Two-Factor Login pages. The flow has two steps on the
 * same page object: username and password first, then the one-time password
 * (OTP) that the application displays on screen. Locators are in
 * object-repository/TwoFactorLoginPage.properties.
 */
public class TwoFactorLoginPage extends TheInternetWrapper {

    /**
     * Types the username into the username field.
     *
     * @param username the username to be entered in the username field.
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage enterUsername(String username) {
        sendKeys(locator("usernameField"), username);
        return this;
    }

    /**
     * Types the password into the password field.
     *
     * @param password the password to be entered in the password field.
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage enterPassword(String password) {
        sendKeys(locator("passwordField"), password);
        return this;
    }

    /**
     * Clicks the next button to submit the credentials and move to the OTP step.
     *
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage clickNextButton() {
        click(locator("nextButton"));
        return this;
    }

    /**
     * Reads the one-time password (OTP) shown on the page.
     *
     * @return the 6-digit OTP code.
     */
    public String getOtpCodeText() {
        return getText(locator("otpCode"));
    }

    /**
     * Types the one-time password into the OTP input field.
     *
     * @param otp the 6-digit code to enter.
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage enterOtp(String otp) {
        sendKeys(locator("otpField"), otp);
        return this;
    }

    /**
     * Clicks the verify button to submit the OTP. It is the same submit button
     * as the next button, stored under its own key so either can change alone.
     *
     * @return the SecurePage shown after successful verification.
     */
    public SecurePage clickVerifyButton() {
        click(locator("verifyButton"));
        return new SecurePage();
    }
}
