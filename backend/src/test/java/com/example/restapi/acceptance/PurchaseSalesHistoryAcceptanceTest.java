package com.example.restapi.acceptance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Acceptance test for the Purchases and Sales tabs of the dashboard. Verifies that
 * each tab renders its container without errors — either showing real records or the
 * empty-state placeholder, both are valid outcomes.
 */
class PurchaseSalesHistoryAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("Purchases tab renders the purchase history view")
    void purchaseTabRenders() {
        loginAsTestUser();

        driver.findElement(By.cssSelector("[data-testid='tab-purchases']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='purchase-history']")));

        boolean rendered =
                !driver.findElements(By.cssSelector("[data-testid='purchase-card']")).isEmpty()
                        || !driver.findElements(By.cssSelector("[data-testid='purchase-history-empty']")).isEmpty();
        assertTrue(rendered, "Purchase history should render either cards or empty-state placeholder");
    }

    @Test
    @DisplayName("Sales tab renders the sales history view")
    void salesTabRenders() {
        loginAsTestUser();

        driver.findElement(By.cssSelector("[data-testid='tab-sales']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='sales-history']")));

        boolean rendered =
                !driver.findElements(By.cssSelector("[data-testid='sale-card']")).isEmpty()
                        || !driver.findElements(By.cssSelector("[data-testid='sales-history-empty']")).isEmpty();
        assertTrue(rendered, "Sales history should render either cards or empty-state placeholder");
    }
}
