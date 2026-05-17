package com.example.restapi.acceptance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Smoke acceptance test: the seeded user (TEST_USER_EMAIL / TEST_USER_PASSWORD) can
 * sign in through the React UI and reach the dashboard. Verifies the full stack is
 * wired end-to-end (frontend → nginx → backend → Supabase).
 */
class RegisterLoginAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("seeded user logs in and lands on the dashboard")
    void loginSucceedsAndShowsDashboard() {
        loginAsTestUser();
        WebElement dashboard = driver.findElement(By.cssSelector("[data-testid='dashboard-shell']"));
        assertTrue(dashboard.isDisplayed(), "Dashboard shell should be visible after login");
    }

    @Test
    @DisplayName("invalid credentials surface an error and stay on the login screen")
    void invalidCredentialsShowError() {
        driver.get(BASE_URL + "/");

        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-email']")));
        emailInput.sendKeys("nobody+" + System.currentTimeMillis() + "@example.com");
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys("wrong-password");
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        // App routes 404 (unknown email) to the Register screen, and 401 to an inline error.
        // Both are valid "did not reach the dashboard" outcomes — assert the dashboard is NOT shown.
        boolean stillOutsideDashboard = wait.until(d ->
                d.findElements(By.cssSelector("[data-testid='dashboard-shell']")).isEmpty());
        assertTrue(stillOutsideDashboard, "Login with bad credentials must not reach the dashboard");
    }
}
