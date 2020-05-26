package com.telephone.repository;

import com.telephone.model.Customer;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;

@Service
public class CustomerRepository {
    private static final Logger logger = Logger.getLogger(CustomerRepository.class.getName());

    @Autowired
    Client client;

    public String create(Customer customer) {
        try {
            IndexResponse response = client.prepareIndex("customers", "phone", customer.getPhone())
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("name", customer.getName())
                            .field("phone", customer.getPhone())
                            .endObject()
                    )
                    .get();
            logger.info("response id: " + response.getId());
            return response.getResult().toString();
        } catch (IOException e) {
            return e.toString();
        }
    }

    public Map<String, Object> view(final String id) {
        GetResponse getResponse = client.prepareGet("customers", "phone", id).get();
        System.out.println(getResponse.getSource());
        return getResponse.getSource();
    }

    public Map<String, Object> searchCustomerByName(final String field) {
        Map<String, Object> map = null;
        SearchResponse response = client.prepareSearch("customers")
                .setTypes("phone")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(prefixQuery("name", field))
                .get();
        List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
        map = searchHits.get(0).getSourceAsMap();
        return map;
    }

    public List<Customer> searchCustomerByPhone(String prefix) {
        int scrollSize = 100;
        List<Customer> results = new ArrayList<>();
        SearchResponse searchResponse = client.prepareSearch("customers")
                .setScroll(new TimeValue(60000))
                .setTypes("phone")
                .setQuery(prefixQuery("phone", prefix))
                .setSize(scrollSize)
                .execute()
                .actionGet();
        List<SearchHit> searchHits = Arrays.asList(searchResponse.getHits().getHits());
        do {
            for (SearchHit hit : searchHits) {
                Customer customer = new com.fasterxml.jackson.databind.ObjectMapper().
                        convertValue(hit.getSourceAsMap(), Customer.class);
                results.add(customer);
            }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(60000)).get();
        } while (searchResponse.getHits().getHits().length != 0);

        System.out.println("Number of customer match is = " + results.size());
        return results;
    }

    public String delete(String phone) {
        DeleteResponse deleteResponse = client.prepareDelete("customers", "phone", phone).get();
        System.out.println(deleteResponse.getResult().toString());
        return deleteResponse.getResult().toString();
    }
}
