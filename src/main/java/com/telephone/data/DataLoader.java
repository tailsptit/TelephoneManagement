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
        int num = 5;
        int rows = (int) Math.pow(10, num);
        logger.info("Insert " + rows + " record of data to ElasticSearch");
        genCustomers("84123456789", num);
//        List<Customer> customers = this.loadCustomersFromFile();
//        customers.forEach(customerService::create);
        logger.info("Complete to insert " + rows + " record of data to ElasticSearch");
    }

    private List<Customer> loadCustomersFromFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, JsonCustomer.class);
        List<JsonCustomer> customers = objectMapper.readValue(this.customersJsonFile.getFile(), collectionType);
        return customers.stream().map(this::from).collect(Collectors.toList());
    }

    private void genCustomers(String phone, int num) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, JsonCustomer.class);
        Customer customer = new Customer();
        String prefixPhone = phone.substring(0, 11 - num);
        String prefixName = "TaiLS_" + prefixPhone;
        String suffixPhone;
        for (int i = 0; i < Math.pow(10, num); i++) {
            suffixPhone = String.format("%0" + num + "d", i);
            customer.setName(prefixName + suffixPhone);
            customer.setPhone(prefixPhone + suffixPhone);
            customerService.create(customer);
        }
    }

    private Customer from(JsonCustomer jsonCustomer) {
        return new Customer(jsonCustomer.getName(), jsonCustomer.getPhone());
    }
}