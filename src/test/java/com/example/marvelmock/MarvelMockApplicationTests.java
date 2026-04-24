package com.example.marvelmock;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class MarvelMockApplicationTests {

    @Test
    void testQueryParamParsing() {
        MarvelMockController controller = new MarvelMockController();

        // Test URI parsing
        URI uri1 = URI.create("http://localhost:8080/v1/public/characters?name=Spider-Man");
        String name1 = controller.getQueryParam(uri1, "name");
        assertEquals("Spider-Man", name1);

        // Test URI without params
        URI uri2 = URI.create("http://localhost:8080/v1/public/characters");
        String name2 = controller.getQueryParam(uri2, "name");
        assertNull(name2);
    }

    @Test
    void testControllerNotNull() {
        MarvelMockController controller = new MarvelMockController();
        assertNotNull(controller);
    }
}
