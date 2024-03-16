package com.company.elasticsearchdemo.service;

import com.company.elasticsearchdemo.model.Item;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class JsonDataService {

    private final ObjectMapper objectMapper;

    public JsonDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Item> readItemsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/items.json");
            InputStream inputStream = resource.getInputStream();
            return objectMapper.readValue(inputStream, new TypeReference<List<Item>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
