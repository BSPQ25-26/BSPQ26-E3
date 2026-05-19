package com.example.restapi.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.restapi.model.Category;
import com.example.restapi.repository.CategoryRepository;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    private static final Logger log = LoggerFactory.getLogger(CategoryControllerTest.class);

    @Autowired MockMvc mockMvc;
    @MockitoBean CategoryRepository categoryRepository;

    @Test
    @DisplayName("GET /api/categories returns 200 with all categories")
    void returnsAllCategories() throws Exception {
        Category c1 = new Category("Indoor", "Indoor plants");
        Category c2 = new Category("Outdoor", "Outdoor plants");
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Indoor"))
                .andExpect(jsonPath("$[1].name").value("Outdoor"));

        log.info("returnsAllCategories passed");
    }

    @Test
    @DisplayName("GET /api/categories returns 200 with empty list when no categories")
    void returnsEmptyListWhenNoCategories() throws Exception {
        when(categoryRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        log.info("returnsEmptyListWhenNoCategories passed");
    }
}
