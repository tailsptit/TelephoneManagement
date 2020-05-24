package com.telephone.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.gson.Gson;
import com.telephone.model.Customer;
import com.telephone.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = Logger.getLogger(DataLoader.class.getName());
    private static final int MaxLine = 10000;

    @Value("classpath:data/customers.json")
    private Resource customersJsonFile;

    @Autowired
    private CustomerRepository customerService;

    @Override
    public void run(String... args) throws Exception {
        genCustomersFile();
        logger.info("Insert data to ElasticSearch");
        List<Customer> customers = this.loadCustomersFromFile();
        customers.forEach(customerService::create);
        logger.info("Complete to insert data to ElasticSearch");
    }

    private void genCustomersFile(){
        try {
            Map<String, String> customer = new HashMap<>();

            customer.put("name", "TaiLS");
            customer.put("phone", "0");
            Writer writer = new FileWriter("./../customers_.json");
            new Gson().toJson(customer, writer);
            writer.close();
        } catch (IOException e){
            System.out.println(e.toString());
        }
    }

    private List<Customer> loadCustomersFromFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, JsonCustomer.class);
        List<JsonCustomer> customers = objectMapper.readValue(this.customersJsonFile.getFile(), collectionType);
        return customers.stream().map(this::from).collect(Collectors.toList());
    }

    private Customer from(JsonCustomer jsonCustomer) {
        return new Customer(jsonCustomer.getName(), jsonCustomer.getPhone());
    }
}