package com.example.restapi.acceptance;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Base for Selenium acceptance tests. Mirrors dipina/book-api-acceptance-selenium with
 * one key difference: the stack (Spring Boot + React via nginx) must already be running
 * — these tests do NOT boot the app via @SpringBootTest because the frontend is a
 * separate container. Run the stack with `docker compose up -d` before `mvn test -Pacceptance`.
 */
public abstract class BaseAcceptanceTest {

    protected static final String BASE_URL =
            System.getProperty("acceptance.baseUrl", "http://localhost");

    protected static final String TEST_USER_EMAIL =
            envOrProperty("TEST_USER_EMAIL", "test.user@example.com");

    protected static final String TEST_USER_PASSWORD =
            envOrProperty("TEST_USER_PASSWORD", "ChangeMe!123");

    protected static final Duration DEFAULT_WAIT = Duration.ofSeconds(15);

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeAll
    static void waitForStack() throws Exception {
        // Health-check via Spring Boot Actuator. The backend container exposes 8080
        // directly; nginx (port 80) does NOT proxy /actuator, so we hit 8080.
        String healthUrl = backendHealthUrl();
        long deadline = System.currentTimeMillis() + 60_000L;
        Exception last = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(healthUrl).toURL().openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                conn.disconnect();
                if (code == 200) {
                    return;
                }
            } catch (Exception e) {
                last = e;
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException(
                "Backend not reachable at " + healthUrl
                        + " — start the stack with `docker compose up -d` before running -Pacceptance",
                last);
    }

    @BeforeEach
    void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1366,900");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, DEFAULT_WAIT);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Drives the React login form with the seeded test user and waits for the dashboard. */
    protected void loginAsTestUser() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-email']")))
                .sendKeys(TEST_USER_EMAIL);
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys(TEST_USER_PASSWORD);
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='dashboard-shell']")));
    }

    /** Convenience: first element matching the selector, or null if none. */
    protected WebElement firstOrNull(By by) {
        var elements = driver.findElements(by);
        return elements.isEmpty() ? null : elements.get(0);
    }

    private static String backendHealthUrl() {
        // BASE_URL is the URL Selenium navigates to (frontend, typically http://localhost).
        // Health-check always goes to the backend on 8080.
        return "http://localhost:8080/actuator/health";
    }

    private static String envOrProperty(String name, String fallback) {
        String fromEnv = System.getenv(name);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProp = System.getProperty(name);
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp;
        }
        return fallback;
    }
}
