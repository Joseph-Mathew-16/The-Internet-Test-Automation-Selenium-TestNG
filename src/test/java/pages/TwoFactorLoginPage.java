package pages;

import org.openqa.selenium.By;

import testbase.TheInternetWrapper;

/**
 * This class represents the Two-Factor Login Page of the application.
 */
public class TwoFactorLoginPage extends TheInternetWrapper {

    /**
     * Locators for the two-factor login page elements
     */
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By nextButton = By.cssSelector("button[type='submit']");
    private final By otpCode = By.id("otp-code");
    private final By otpField = By.id("code");
    private final By verifyButton = By.cssSelector("button[type='submit']");

    /**
     * Method to enter username in username field.
     *
     * @param username - the username to be entered in the username field.
     * @return TwoFactorLoginPage - returns the current instance of
     *         TwoFactorLoginPage for method
     *         chaining.
     */
    public TwoFactorLoginPage enterUsername(String username) {
        sendKeys(usernameField, username);
        return this;
    }

    /**
     * Method to enter password in password field.
     *
     * @param password - the password to be entered in the password field.
     * @return TwoFactorLoginPage - returns the current instance of
     *         TwoFactorLoginPage for method
     *         chaining.
     */
    public TwoFactorLoginPage enterPassword(String password) {
        sendKeys(passwordField, password);
        return this;
    }

    /**
     * Method to click next button.
     *
     * @return TwoFactorLoginPage - returns the current instance of
     *         TwoFactorLoginPage for method
     *         chaining.
     */
    public TwoFactorLoginPage clickNextButton() {
        click(nextButton);
        return this;
    }

    /**
     * Method to retrieve the one‑time password (OTP) shown on the page.
     *
     * @return String containing the 6‑digit OTP code.
     */
    public String getOtpCodeText() {
        return getText(otpCode);
    }

    /**
     * Enter the one‑time password into the OTP input field.
     *
     * @param otp 6‑digit code to enter.
     * @return TwoFactorLoginPage - returns the current instance of
     *         TwoFactorLoginPage for method
     *         chaining.
     */
    public TwoFactorLoginPage enterOtp(String otp) {
        sendKeys(otpField, otp);
        return this;
    }

    /**
     * Method to click verify button.
     *
     * @return SecurePage - returns a SecurePage object for method chaining.
     */
    public SecurePage clickVerifyButton() {
        click(verifyButton);
        return new SecurePage();
    }
}
