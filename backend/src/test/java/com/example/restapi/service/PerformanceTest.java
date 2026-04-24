package com.example.restapi.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
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
    public ContiPerfRule rule = new ContiPerfRule();

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

    // SUCCESS: 1000 invocations across 10 threads, max 500ms per call, avg under 100ms
    @Test
    @PerfTest(invocations = 1000, threads = 10)
    @Required(max = 500, average = 100)
    public void testGetAllItems_invocationsAndThreads() {
        itemService.getAllItems();
    }

    // SUCCESS: throughput - at least 10 operations per second
    @Test
    @PerfTest(invocations = 500, threads = 5)
    @Required(throughput = 10)
    public void testGetAllItems_throughput() {
        itemService.getAllItems();
    }

    // SUCCESS: duration - runs continuously for 2000ms across 3 threads
    @Test
    @PerfTest(duration = 2000, threads = 3)
    @Required(max = 500)
    public void testGetAllItems_duration() {
        itemService.getAllItems();
    }

    // SUCCESS: invocations + threads + avg + max + throughput on AppUserService
    @Test
    @PerfTest(invocations = 500, threads = 5)
    @Required(max = 500, average = 100, throughput = 10)
    public void testGetAllUsers_invocationsAndThreads() {
        appUserService.getAllUsers();
    }

    // SUCCESS: duration test on AppUserService
    @Test
    @PerfTest(duration = 2000, threads = 3)
    @Required(throughput = 10)
    public void testGetAllUsers_duration() {
        appUserService.getAllUsers();
    }

    // FAIL: requirements set stricter than achievable (max=1ms, but each call sleeps 5ms
    //       to simulate realistic DB/network latency)
    @Test
    @PerfTest(invocations = 10, threads = 2)
    @Required(max = 1)
    public void testGetAllItems_strictRequirements_fails() throws InterruptedException {
        Thread.sleep(5);
        itemService.getAllItems();
    }
}
