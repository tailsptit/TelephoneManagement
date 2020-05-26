package com.telephone.repository;

import com.telephone.model.Customer;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
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
//            logger.info("response id: " + response.getId());
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

    public Map<String, Object> searchByName(final String field) {
        Map<String, Object> map = null;
        SearchResponse response = client.prepareSearch("customers")
                .setTypes("phone")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(matchQuery("name", field))
                .get();
        List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
        map = searchHits.get(0).getSourceAsMap();
        return map;
    }

//    public List<Customer> searchByName(final String field) {
//        Map<String, Object> map = null;
//        SearchResponse response = client.prepareSearch("customers")
//                .setTypes("phone")
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(QueryBuilders.matchQuery("name", field))
//                .get();
//        SearchHit[] searchHits = response.getHits().getHits();
//        return Arrays.stream(searchHits).map(hit -> JSON.parseObject(hit.getSourceAsString(), Customer.class)).collect(Collectors.toList());
//    }

    public Map<String, Object> searchByPhone(final String field) {
        Map<String, Object> map = null;
        SearchResponse response = client.prepareSearch("customers")
                .setTypes("phone")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(prefixQuery("phone", field))
                .get();
        List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
        map = searchHits.get(0).getSourceAsMap();
        return map;
    }

    public List<Customer> searchPhoneByPrefix(final String prefix) {
        logger.info("inside prefix search result");
//        QueryBuilder query = QueryBuilders.prefixQuery("name.completion", prefix);
//        SearchResponse searchResponse = client.prepareSearch(indices)
//                .setTypes(types).setQuery(query).get();
//        logger.info("response{}", searchResponse);
//        return Arrays.stream(searchResponse.getHits().getHits())
//                .map(SearchHit::getSourceAsMap).map(
//                        Map::values).flatMap(Collection::stream)
//                .map(Object::toString)
//                .collect(Collectors.toList());

        Map<String, Object> map = null;
        SearchResponse searchResponse = client.prepareSearch("customers")
                .setTypes("phone")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(prefixQuery("phone", prefix)).setSize(100)
                .execute().actionGet();
        List<SearchHit> searchHits = Arrays.asList(searchResponse.getHits().getHits());
        System.out.println(searchHits.toString());
        System.out.println("SIZE = " + searchHits.size());


//        map = searchHits.get(0).getSourceAsMap();
//        System.out.println(map.toString());
//        return map;
        List<Customer> results = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            Customer customer = new com.fasterxml.jackson.databind.ObjectMapper().
                    convertValue(hit.getSourceAsMap(), Customer.class);
            results.add(customer);
        }
//        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, Customer.class);
//        return searchHits.stream().forEach(hit -> new com.fasterxml.jackson.databind.ObjectMapper().
//                convertValue(SearchHit::getSourceAsMap, Customer.class)).collect(Collectors.toList());
        return results;

//        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, JsonCustomer.class);
//        List<JsonCustomer> customers = objectMapper.readValue(this.customersJsonFile.getFile(), collectionType);
//        return customers.stream().map(this::from).collect(Collectors.toList());

//        return Arrays.stream(searchResponse.getHits().getHits())
//                .map(SearchHit::getSourceAsMap).map(
//                        Map::values).flatMap(Collection::stream)
//                .map(Object::toString)
//                .collect(Collectors.toList());
    }

    public List<Customer> searchPhoneByPrefix2(String prefix) {
        int scrollSize = 100;
        List<Map<String, Object>> esData = new ArrayList<Map<String, Object>>();
        List<Customer> results = new ArrayList<>();

        SearchResponse searchResponse = null;

        searchResponse = client.prepareSearch("customers")
                .setScroll(new TimeValue(60000))
                .setTypes("phone")
                .setQuery(QueryBuilders.prefixQuery("phone", prefix))
                .setSize(scrollSize)
                .execute()
                .actionGet();
        List<SearchHit> searchHits = Arrays.asList(searchResponse.getHits().getHits());

//            for(SearchHit hit : searchResponse.getHits()){
//                esData.add(hit.getSource());
//            }

        do {
            for (SearchHit hit : searchHits) {
                Customer customer = new com.fasterxml.jackson.databind.ObjectMapper().
                        convertValue(hit.getSourceAsMap(), Customer.class);
                results.add(customer);
            }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(60000)).get();
        } while (searchResponse.getHits().getHits().length != 0);


        System.out.println("SIZE = " + results.size());

        return results;
    }

    public String update(final String id) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("customers")
                .type("phone")
                .id(id)
                .doc(jsonBuilder()
                        .startObject()
                        .field("name", "Rajesh")
                        .endObject());
        try {
            UpdateResponse updateResponse = client.update(updateRequest).get();
            System.out.println(updateResponse.status());
            return updateResponse.status().toString();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
        }
        return "Exception";
    }

    public String delete(String id) {
        DeleteResponse deleteResponse = client.prepareDelete("customers", "phone", id).get();
        System.out.println(deleteResponse.getResult().toString());
        return deleteResponse.getResult().toString();
    }

//    public List<Customer> listAll() {
//        return this.customerRepository.findAll();
//    }
//
//    public Customer save(Customer customer) {
//        return this.customerRepository.save(customer);
//    }
//
//    public long count() {
//        return this.customerRepository.count();
//    }
//
//    public List<Customer> search(String keywords) {
//        MatchQueryBuilder searchByPhoneNumber = QueryBuilders.matchQuery("com.telephone", keywords);
//        return this.customerRepository.search(searchByPhoneNumber);
//    }
//
//    public Page<Customer> findByPhone(String name, Pageable pageable) {
//        return customerRepository.findByPhoneNumber(name, pageable);
//    }
//
//    public void delete(Customer customer) {
//        customerRepository.delete(customer);
//    }
}
