package com.example.restapi.acceptance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Acceptance test for the cart + checkout flow. Adds the first available plant to the
 * cart, opens the cart sidebar, runs the simulated checkout and asserts the receipt.
 *
 * PaymentService rejects ~10% of payments at random, so checkout is retried up to
 * {@link #CHECKOUT_RETRIES} times — leaving the cumulative failure rate negligible.
 */
class CartCheckoutAcceptanceTest extends BaseAcceptanceTest {

    private static final int CHECKOUT_RETRIES = 4;

    @Test
    @DisplayName("user adds an item, checks out and sees the receipt")
    void addItemAndCheckoutSucceeds() {
        loginAsTestUser();

        // 1. Open the first plant and add 1 unit to the cart.
        wait.until(d -> !d.findElements(By.cssSelector("[data-testid='plant-view-details']")).isEmpty());
        driver.findElements(By.cssSelector("[data-testid='plant-view-details']")).get(0).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='plant-details-modal']")));
        // The modal mounts immediately but renders a spinner while plant details load —
        // wait for the quantity input itself, which only appears once data has arrived.
        // The input is pre-filled with 1 via React state, so we leave it untouched
        // (typing into it cross-platform is unreliable: Ctrl+A doesn't select on macOS).
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='plant-quantity']")));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='plant-add-to-cart']")))
                .click();

        // The modal closes itself after a 1.5s success message. Wait for it to be gone.
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("[data-testid='plant-details-modal']")));

        // 2. Open the cart, expect at least one item.
        driver.findElement(By.cssSelector("[data-testid='open-cart']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='cart-sidebar']")));
        wait.until(d -> !d.findElements(By.cssSelector("[data-testid='cart-item']")).isEmpty());

        assertFalse(driver.findElements(By.cssSelector("[data-testid='cart-item']")).isEmpty(),
                "Cart should contain the item we just added");

        // 3. Run checkout. Retry on the random 10% payment failure.
        driver.findElement(By.cssSelector("[data-testid='cart-checkout']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='checkout-modal']")));

        boolean receiptShown = false;
        for (int attempt = 0; attempt < CHECKOUT_RETRIES && !receiptShown; attempt++) {
            fillCheckoutForm();
            driver.findElement(By.cssSelector("[data-testid='checkout-submit']")).click();

            WebDriverWait outcome = new WebDriverWait(driver, Duration.ofSeconds(20));
            receiptShown = outcome.until(d -> {
                if (!d.findElements(By.cssSelector("[data-testid='receipt-modal']")).isEmpty()) {
                    return true;
                }
                // Payment failed (simulated). The form stays open with a .checkout-error message.
                return !d.findElements(By.className("checkout-error")).isEmpty();
            });

            if (!driver.findElements(By.cssSelector("[data-testid='receipt-modal']")).isEmpty()) {
                receiptShown = true;
            } else {
                // Clear the error and retry — the form is still on screen and re-fillable.
                receiptShown = false;
            }
        }

        assertTrue(receiptShown,
                "Checkout did not produce a receipt after " + CHECKOUT_RETRIES + " attempts");

        WebElement receiptNumber = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='receipt-number']")));
        assertFalse(receiptNumber.getText().isBlank(), "Receipt number should be present");

        driver.findElement(By.cssSelector("[data-testid='receipt-continue']")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("[data-testid='receipt-modal']")));
    }

    private void fillCheckoutForm() {
        setText(By.cssSelector("[data-testid='checkout-card-number']"), "4111111111111111");
        setText(By.cssSelector("[data-testid='checkout-card-holder']"), "Acceptance Test");
        setText(By.cssSelector("[data-testid='checkout-expiry']"), "1230");
        setText(By.cssSelector("[data-testid='checkout-cvv']"), "123");
    }

    private void setText(By by, String text) {
        WebElement el = driver.findElement(by);
        el.clear();
        el.sendKeys(text);
    }
}
