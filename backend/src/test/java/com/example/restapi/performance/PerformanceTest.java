package com.example.restapi.performance;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.service.AppUserService;
import com.example.restapi.service.ItemService;
import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class);

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule(new HtmlReportGenerator("perf-reports/report.html"));

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ItemService itemService;
    private AppUserService appUserService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Plant");
        item.setAmount(9.99);
        item.setQuantity(50);
        item.setStatus(true);

        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemRepository.findByStatusTrue()).thenReturn(List.of(item));

        Profile mockProfile = mock(Profile.class);
        when(mockProfile.getUsername()).thenReturn("testuser");
        when(profileRepository.findAll()).thenReturn(List.of(mockProfile));

        itemService = new ItemService(itemRepository, categoryRepository, profileRepository);
        appUserService = new AppUserService(profileRepository);
    }

    // SUCCESS: invocations&Threads - how each individual call behaves under high concurrency?
    // 10 threads hammering the method at the same time, and you check that no single call takes more than 500ms and the average stays under 100ms
    @Test
    @JUnitPerfTest(durationMs = 3000, threads = 10)
    @JUnitPerfTestRequirement(maxLatency = 500, meanLatency = 100)
    public void testGetAllItems_invocationsAndThreads() {
        itemService.getAllItems();
        log.info("testGetAllItems_invocationsAndThreads completed");
    }

    // SUCCESS: throughput - how many operations the system can complete per second?
    // ensures that we execute at least 10 operations per second
    // taking into acount that we have 5 threads running for 2000ms.
    @Test
    @JUnitPerfTest(durationMs = 2000, threads = 5)
    @JUnitPerfTestRequirement(executionsPerSec = 10)
    public void testGetAllItems_throughput() {
        itemService.getAllItems();
    }

    // SUCCESS: duration - measure esatability over time
    // In our case duration runs continuously for 2000ms across 3 threads

    @Test
    @JUnitPerfTest(durationMs = 2000, threads = 3)
    @JUnitPerfTestRequirement(maxLatency = 500)
    public void testGetAllItems_duration() {
        itemService.getAllItems();
    }

    // SUCCESS: threads + avg + max + throughput on AppUserService

    @Test
    @JUnitPerfTest(durationMs = 2000, threads = 5)
    @JUnitPerfTestRequirement(maxLatency = 500, meanLatency = 100, executionsPerSec = 10)
    public void testGetAllUsers_invocationsAndThreads() {
        appUserService.getAllUsers();
        log.info("testGetAllUsers_invocationsAndThreads completed");
    }

    // SUCCESS: duration test on AppUserService
    // In our case duration runs continuously for 2000ms across 3 threads
    @Test
    @JUnitPerfTest(durationMs = 2000, threads = 3)
    @JUnitPerfTestRequirement(executionsPerSec = 5)
    public void testGetAllUsers_duration() {
        appUserService.getAllUsers();
    }

    // DEMO: intentionally fails (maxLatency=1ms with a 5ms sleep) to illustrate how a
    // JUnitPerf assertion violation looks in the report. Kept @Ignored so `mvn test`
    // stays green; un-ignore locally if you want to see a failing JUnitPerf run.
    @Ignore("Intentionally violates maxLatency — left as a JUnitPerf failure example")
    @Test
    @JUnitPerfTest(durationMs = 500, threads = 2)
    @JUnitPerfTestRequirement(maxLatency = 1)
    public void testGetAllItems_strictRequirements_fails() throws InterruptedException {
        Thread.sleep(5);
        itemService.getAllItems();
    }
}
