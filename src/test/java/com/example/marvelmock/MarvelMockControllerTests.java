package com.example.marvelmock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(MarvelMockController.class)
class MarvelMockControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCharacters_withoutNameParameter_returnsDefault3DMan() throws Exception {
        mockMvc.perform(get("/v1/public/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("Ok"))
                .andExpect(jsonPath("$.data.results").isArray())
                .andExpect(jsonPath("$.data.results", hasSize(1)))
                .andExpect(jsonPath("$.data.results[0].id").value(1011334))
                .andExpect(jsonPath("$.data.results[0].name").value("3-D Man"))
                .andExpect(jsonPath("$.data.results[0].description").value("A mock character from Marvel API"))
                .andExpect(jsonPath("$.data.results[0].modified").value("2014-04-29T14:18:17-0400"));
    }

    @Test
    void getCharacters_withNameParameter_returnsCharacterWithSpecifiedName() throws Exception {
        String characterName = "Spider-Man";

        mockMvc.perform(get("/v1/public/characters")
                .param("name", characterName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("Ok"))
                .andExpect(jsonPath("$.data.results").isArray())
                .andExpect(jsonPath("$.data.results", hasSize(1)))
                .andExpect(jsonPath("$.data.results[0].id").value(1011334))
                .andExpect(jsonPath("$.data.results[0].name").value(characterName))
                .andExpect(jsonPath("$.data.results[0].description").value("A mock character from Marvel API"))
                .andExpect(jsonPath("$.data.results[0].modified").value("2014-04-29T14:18:17-0400"));
    }

    @Test
    void getCharacters_withEmptyNameParameter_returnsEmptyName() throws Exception {
        mockMvc.perform(get("/v1/public/characters")
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("Ok"))
                .andExpect(jsonPath("$.data.results[0].name").value(""));
    }

    @Test
    void getCharacters_responseStructure_containsAllRequiredFields() throws Exception {
        mockMvc.perform(get("/v1/public/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.results").exists())
                .andExpect(jsonPath("$.data.results[0].id").exists())
                .andExpect(jsonPath("$.data.results[0].name").exists())
                .andExpect(jsonPath("$.data.results[0].description").exists())
                .andExpect(jsonPath("$.data.results[0].modified").exists());
    }

}
