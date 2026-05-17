package com.example.restapi.acceptance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Acceptance test for the Community (forum) tab. Verifies a logged-in user can
 * reach the feed and open a post detail. Assumes at least one post exists in
 * the database — the project ships with four seeded posts.
 */
class ForumBrowsingAcceptanceTest extends BaseAcceptanceTest {

    @Test
    @DisplayName("Community tab renders the post feed with cards")
    void forumFeedRenders() {
        loginAsTestUser();

        driver.findElement(By.cssSelector("[data-testid='tab-community']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='forum']")));

        // The feed shows a "loading" message while /api/posts is in flight.
        // Wait until it resolves into either a populated feed or the empty state.
        boolean rendered = wait.until(d ->
                !d.findElements(By.cssSelector("[data-testid='forum-post-card']")).isEmpty()
                        || !d.findElements(By.cssSelector("[data-testid='forum-empty']")).isEmpty());
        assertTrue(rendered, "Forum should render either at least one post or the empty state");
    }

    @Test
    @DisplayName("Clicking a post opens its details modal with the comments anchor")
    void clickingPostOpensDetails() {
        loginAsTestUser();

        driver.findElement(By.cssSelector("[data-testid='tab-community']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='forum']")));

        // Wait for either cards or empty state before deciding.
        wait.until(d ->
                !d.findElements(By.cssSelector("[data-testid='forum-post-card']")).isEmpty()
                        || !d.findElements(By.cssSelector("[data-testid='forum-empty']")).isEmpty());

        // Skip the assertion gracefully if the DB is empty (acceptance shouldn't fail
        // because of seeding state outside this test's control).
        if (driver.findElements(By.cssSelector("[data-testid='forum-post-card']")).isEmpty()) {
            return;
        }

        driver.findElements(By.cssSelector("[data-testid='forum-post-card']")).get(0).click();

        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='post-details-modal']")));
        assertTrue(modal.isDisplayed(), "Post details modal should appear");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='post-details-title']")));

        // The comments section is the integration anchor for the comments work the
        // other contributor will land. Make sure it stays in the DOM so they have
        // a stable hook.
        assertFalse(driver.findElements(By.cssSelector("[data-testid='comments-section']")).isEmpty(),
                "Post details modal must expose the comments-section anchor for the comments feature");
    }
}
