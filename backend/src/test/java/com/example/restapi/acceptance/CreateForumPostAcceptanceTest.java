package com.example.restapi.acceptance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Acceptance test for creating a forum post end-to-end through the React UI.
 */
class CreateForumPostAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("logged-in user can create a forum post and see it in the feed")
    void createPostAppearsInFeed() {
        String uniqueTitle = "acceptance-post-" + UUID.randomUUID();

        loginAsTestUser();

        driver.findElement(By.cssSelector("[data-testid='tab-community']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='forum']")));

        // Open the create-post modal.
        driver.findElement(By.cssSelector("[data-testid='forum-create-post']")).click();
        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='create-forum-post-modal']")));
        assertTrue(modal.isDisplayed(), "Create-post modal should appear");

        // Wait until the category dropdown has loaded from /api/categories before submitting.
        wait.until(d -> {
            WebElement select = d.findElement(By.cssSelector("[data-testid='forum-post-category']"));
            return !select.getAttribute("value").isBlank();
        });

        driver.findElement(By.cssSelector("[data-testid='forum-post-title']")).sendKeys(uniqueTitle);
        driver.findElement(By.cssSelector("[data-testid='forum-post-content']"))
                .sendKeys("Created from an acceptance test.");
        driver.findElement(By.cssSelector("[data-testid='forum-post-submit']")).click();

        // Modal closes on success.
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("[data-testid='create-forum-post-modal']")));

        // The new post should now be in the feed.
        wait.until(d -> d.findElements(By.cssSelector("[data-testid='forum-post-card-title']")).stream()
                .anyMatch(e -> uniqueTitle.equals(e.getText())));
    }
}
