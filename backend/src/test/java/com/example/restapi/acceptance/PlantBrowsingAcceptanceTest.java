package com.example.restapi.acceptance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Acceptance test for the plant catalogue: a logged-in user sees the plants grid and
 * can open the details modal of any plant. Assumes at least one item exists in the DB
 * (created via the app or fixtures).
 */
class PlantBrowsingAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("logged-in user sees plant cards on the shop tab")
    void plantsGridShowsCards() {
        loginAsTestUser();

        // Shop tab is the default. Wait for the grid to render and then for at least one card.
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='plants-grid']")));
        wait.until(d -> !d.findElements(By.cssSelector("[data-testid='plant-card']")).isEmpty());

        List<WebElement> cards = driver.findElements(By.cssSelector("[data-testid='plant-card']"));
        assertFalse(cards.isEmpty(),
                "Expected at least one plant in the catalogue. "
                        + "Seed one via the UI ('+ Create post') or directly in the DB before running this test.");
    }

    @Test
    @DisplayName("clicking 'View details' opens the plant details modal")
    void clickingViewDetailsOpensModal() {
        loginAsTestUser();

        wait.until(d -> !d.findElements(By.cssSelector("[data-testid='plant-view-details']")).isEmpty());
        driver.findElements(By.cssSelector("[data-testid='plant-view-details']")).get(0).click();

        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='plant-details-modal']")));
        assertTrue(modal.isDisplayed(), "Plant details modal should appear after clicking 'View details'");
        // The add-to-cart button is what makes this modal actionable.
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='plant-add-to-cart']")));
    }
}
