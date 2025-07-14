package com.example.marvelmock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@RestController
public class MarvelMockController {
    @GetMapping("/v1/public/characters")
    public Map<String, Object> getCharacters(@RequestParam(value = "name", required = false) String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("status", "Ok");
        response.put("data", Map.of(
            "results", List.of(
                Map.of(
                    "id", 1011334,
                    "name", name != null ? name : "3-D Man",
                    "description", "A mock character from Marvel API",
                    "modified", "2014-04-29T14:18:17-0400"
                )
            )
        ));
        return response;
    }
}

