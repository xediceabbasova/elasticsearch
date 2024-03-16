package com.company.elasticsearchdemo.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.company.elasticsearchdemo.dto.SearchRequestDto;
import com.company.elasticsearchdemo.model.Item;
import com.company.elasticsearchdemo.repository.ItemRepository;
import com.company.elasticsearchdemo.util.ESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final JsonDataService jsonDataService;
    private final ElasticsearchClient elasticsearchClient;

    public ItemService(ItemRepository itemRepository, JsonDataService jsonDataService, ElasticsearchClient elasticsearchClient) {
        this.itemRepository = itemRepository;
        this.jsonDataService = jsonDataService;
        this.elasticsearchClient = elasticsearchClient;
    }

    public Item createIndex(Item item) {
        return itemRepository.save(item);
    }

    public void addItemsFromJson() {
        log.info("Adding Items from Json");
        List<Item> items = jsonDataService.readItemsFromJson();
        itemRepository.saveAll(items);
    }

    public List<Item> getAllItemsFromAllIndexes() {
        Query query = ESUtil.createMatchAllQuery();
        log.info("Elasticsearch query: {}", query.toString());
        SearchResponse<Item> response = null;
        try {
            response = elasticsearchClient.search(q -> q.query(query).size(1000), Item.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Elasticsearch response: {}", response.toString());

        return extractItemsFromResponse(response);
    }


    public List<Item> getAllDataFromIndex(String indexName) {
        try {
            var query = ESUtil.createMatchAllQuery();
            log.info("Elasticsearch query {}", query.toString());

            SearchResponse<Item> response = elasticsearchClient.search(
                    q -> q.index(indexName).query(query).size(1000), Item.class);

            log.info("Elasticsearch response {}", response.toString());

            return extractItemsFromResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Item> searchItemsByFieldAndValue(SearchRequestDto searchRequestDto) {
        SearchResponse<Item> response = null;
        try {
            Supplier<Query> querySupplier = ESUtil.buildQueryForFieldAndValue(searchRequestDto.getFieldName().get(0),
                    searchRequestDto.getSearchValue().get(0));//sorgu olustur

            log.info("Elasticsearch query {}", querySupplier.toString());

            response = elasticsearchClient.search(q -> q.index("items_index")
                    .query(querySupplier.get()), Item.class);//sorguyu calistir ve cevabi alir

            log.info("Elasticsearch response: {}", response.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return extractItemsFromResponse(response);
    }

    public List<Item> searchItemsByNameAndBrand(String name, String brand) {
        return itemRepository.searchByNameAndBrand(name, brand);
    }

    public List<Item> boolQueryFieldAndValue(SearchRequestDto searchRequestDto) {
        try {
            var supplier = ESUtil.createBoolQuery(searchRequestDto);
            log.info("Elasticsearch query: " + supplier.get().toString());

            SearchResponse<Item> response = elasticsearchClient.search(q ->
                    q.index("items_index").query(supplier.get()), Item.class);
            log.info("Elasticsearch response: {}", response.toString());

            return extractItemsFromResponse(response);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Set<String> findSuggestedItemNames(String itemName) {
        Query autoSuggestQuery = ESUtil.buildAutoSuggestQuery(itemName);
        log.info("Elasticsearch query: {}", autoSuggestQuery.toString());

        try {
            return elasticsearchClient.search(q -> q.index("items_index").query(autoSuggestQuery), Item.class)
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .map(Item::getName)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> autoSuggestItemsByNameWithQuery(String name) {
        List<Item> items = itemRepository.customAutocompleteSearch(name);
        log.info("Elasticsearch response: {}", items.toString());
        return items
                .stream()
                .map(Item::getName)
                .collect(Collectors.toList());
    }

    public List<Item> extractItemsFromResponse(SearchResponse<Item> response) {
        return response
                .hits()
                .hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }


}
