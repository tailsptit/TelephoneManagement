package com.telephone.controller;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

import com.telephone.model.Customer;

import com.telephone.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
//import com.search.demo.repository.AutoCompleteSearchRepository;

/**
 * This class is to demo how ElasticsearchTemplate can be used to Save/Retrieve
 */

@RestController
@RequestMapping(value = "/customers")
public class CustomerController {
    private static final Logger logger = Logger.getLogger(CustomerRepository.class.getName());

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/create")
    public String create(@RequestBody Customer customer) {
        logger.info("API POST - create is called");
        return customerRepository.create(customer);
    }

    @GetMapping("/search/name/{field}")
    public Map<String, Object> searchByName(@PathVariable final String field) {
        logger.info("API GET - searchByName is called");
        return customerRepository.searchByName(field);
    }

    @GetMapping("search/phone/{field}")
    public Map<String, Object> searchByPhone(@PathVariable final String field) {
        logger.info("API GET - searchByPhone is called");
        return customerRepository.searchByPhone(field);
    }

    @GetMapping("search/phone-prefix/{prefix}")
    public List<Customer> searchPhoneByPrefix(@PathVariable final String prefix) {
        logger.info("API GET - searchPhoneByPrefix is called");
//        CompletionSuggestionBuilder prefix = SuggestBuilders.completionSuggestion(FIELD).prefix("Tai");
        return customerRepository.searchPhoneByPrefix(prefix);
    }

    @GetMapping("search/phone-prefix2/{prefix}")
    public List<Customer> searchPhoneByPrefix2(@PathVariable final String prefix) {
        logger.info("API GET - searchPhoneByPrefix2 is called");
//        CompletionSuggestionBuilder prefix = SuggestBuilders.completionSuggestion(FIELD).prefix("Tai");
        return customerRepository.searchPhoneByPrefix2(prefix);
    }


    @GetMapping("/update/{id}")
    public String update(@PathVariable final String id) throws IOException {
        logger.info("API UPDATE - update/name is called");
        return customerRepository.update(id);
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable final String id) {
        logger.info("API DELETE - search/name is called");
        return customerRepository.delete(id);
    }
}