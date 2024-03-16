package com.company.elasticsearchdemo.util;


import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.company.elasticsearchdemo.dto.SearchRequestDto;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ESUtil {
    public static Query createMatchAllQuery() {
        return Query.of(q -> q.matchAll(new MatchAllQuery.Builder().build()));
    }

    public static Supplier<Query> buildQueryForFieldAndValue(String fieldName, String searchValue) {
        return () -> Query.of(q -> q.match(new MatchQuery.Builder()
                .field(fieldName)
                .query(searchValue)
                .build()));
    }

    public static Supplier<Query> createBoolQuery(SearchRequestDto dto) {
        return () -> {
            String fieldName1 = dto.getFieldName().get(0).toString();
            String searchValue1 = dto.getSearchValue().get(0);
            String fieldName2 = dto.getFieldName().get(1).toString();
            String searchValue2 = dto.getSearchValue().get(1);

            return Query.of(q -> q.bool(new BoolQuery.Builder()
                    .filter(termQuery(fieldName1, searchValue1))
                    .must(matchQuery(fieldName2, searchValue2))
                    .build()));
        };
    }


    public static Query termQuery(String field, String value) {
        return Query.of(q -> q.term(new TermQuery.Builder()
                .field(field)
                .value(value)
                .build()));
    }

    public static Query matchQuery(String field, String value) {
        return Query.of(q -> q.match(new MatchQuery.Builder()
                .field(field)
                .query(value)
                .build()));
    }

    public static Query buildAutoSuggestQuery(String name) {
        return Query.of(q -> q.match(new MatchQuery.Builder()
                .field("name")
                .query(name)
                .analyzer("custom_index")
                .build()));
    }
}
