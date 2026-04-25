package com.example.restapi.service;

import static org.mockito.Mockito.*;

import java.util.List;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;

public class PerformanceTest {

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule(new HtmlReportGenerator("perf-reports/report.html"));

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProfileRepository profileRepository;

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

        itemService = new ItemService(itemRepository);
        appUserService = new AppUserService(profileRepository);
    }

    // SUCCESS: 10 threads for 3000ms, max 500ms per call, avg under 100ms
    @Test
    @JUnitPerfTest(durationMs = 3000, threads = 10)
    @JUnitPerfTestRequirement(maxLatency = 500, meanLatency = 100)
    public void testGetAllItems_invocationsAndThreads() {
        itemService.getAllItems();
    }

    // SUCCESS: throughput - at least 10 operations per second
    @Test
    @JUnitPerfTest(durationMs = 2000, threads = 5)
    @JUnitPerfTestRequirement(executionsPerSec = 10)
    public void testGetAllItems_throughput() {
        itemService.getAllItems();
    }

    // SUCCESS: duration - runs continuously for 2000ms across 3 threads
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
    }

    // SUCCESS: duration test on AppUserService
    @Test
    @JUnitPerfTest(durationMs = 2000, threads = 3)
    @JUnitPerfTestRequirement(executionsPerSec = 10)
    public void testGetAllUsers_duration() {
        appUserService.getAllUsers();
    }

    // FAIL: max=1ms but each call sleeps 5ms — intentionally fails to demonstrate a failing test
    @Test
    @JUnitPerfTest(durationMs = 500, threads = 2)
    @JUnitPerfTestRequirement(maxLatency = 1)
    public void testGetAllItems_strictRequirements_fails() throws InterruptedException {
        Thread.sleep(5);
        itemService.getAllItems();
    }
}
