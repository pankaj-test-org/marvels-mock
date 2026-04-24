package com.example.marvelmock;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class MarvelMockControllerTests {

    @Test
    void testGetQueryParam_withValidParam() {
        MarvelMockController controller = new MarvelMockController();
        URI uri = URI.create("http://localhost:8080/v1/public/characters?name=Spider-Man");

        String result = controller.getQueryParam(uri, "name");

        assertEquals("Spider-Man", result);
    }

    @Test
    void testGetQueryParam_withoutParam() {
        MarvelMockController controller = new MarvelMockController();
        URI uri = URI.create("http://localhost:8080/v1/public/characters");

        String result = controller.getQueryParam(uri, "name");

        assertNull(result);
    }

    @Test
    void testGetQueryParam_withMultipleParams() {
        MarvelMockController controller = new MarvelMockController();
        URI uri = URI.create("http://localhost:8080/v1/public/characters?name=Iron-Man&limit=10");

        String name = controller.getQueryParam(uri, "name");
        String limit = controller.getQueryParam(uri, "limit");

        assertEquals("Iron-Man", name);
        assertEquals("10", limit);
    }

    @Test
    void testGetQueryParam_withEmptyValue() {
        MarvelMockController controller = new MarvelMockController();
        URI uri = URI.create("http://localhost:8080/v1/public/characters?name=");

        String result = controller.getQueryParam(uri, "name");

        assertNull(result);
    }
}
