package pages;

import org.openqa.selenium.By;

import testbase.TheInternetWrapper;

/**
 * Page object for the Two-Factor Login pages. The flow has two steps on the
 * same page object: username and password first, then the one-time password
 * (OTP) that the application displays on screen.
 */
public class TwoFactorLoginPage extends TheInternetWrapper {

    /**
     * Locators for the two-factor login page elements.
     */
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    /**
     * Shared submit button: it is the "Next" button on the credentials step and
     * the "Verify" button on the OTP step, so both actions use this locator.
     */
    private final By nextButton = By.cssSelector("button[type='submit']");
    private final By otpCode = By.id("otp-code");
    private final By otpField = By.id("code");

    /**
     * Types the username into the username field.
     *
     * @param username the username to be entered in the username field.
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage enterUsername(String username) {
        sendKeys(usernameField, username);
        return this;
    }

    /**
     * Types the password into the password field.
     *
     * @param password the password to be entered in the password field.
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage enterPassword(String password) {
        sendKeys(passwordField, password);
        return this;
    }

    /**
     * Clicks the next button to submit the credentials and move to the OTP step.
     *
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage clickNextButton() {
        click(nextButton);
        return this;
    }

    /**
     * Reads the one-time password (OTP) shown on the page.
     *
     * @return the 6-digit OTP code.
     */
    public String getOtpCodeText() {
        return getText(otpCode);
    }

    /**
     * Types the one-time password into the OTP input field.
     *
     * @param otp the 6-digit code to enter.
     * @return the current TwoFactorLoginPage, for method chaining.
     */
    public TwoFactorLoginPage enterOtp(String otp) {
        sendKeys(otpField, otp);
        return this;
    }

    /**
     * Clicks the verify button to submit the OTP.
     *
     * @return the SecurePage shown after successful verification.
     */
    public SecurePage clickVerifyButton() {
        click(nextButton);
        return new SecurePage();
    }
}
