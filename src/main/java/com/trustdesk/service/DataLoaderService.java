package com.trustdesk.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.trustdesk.entity.*;
import com.trustdesk.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class DataLoaderService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final KBDocumentRepository kbDocumentRepository;
    private final ToolCatalogRepository toolCatalogRepository;
    private final ObjectMapper objectMapper;

    public DataLoaderService(CustomerRepository customerRepository,
                             OrderRepository orderRepository,
                             TicketRepository ticketRepository,
                             KBDocumentRepository kbDocumentRepository,
                             ToolCatalogRepository toolCatalogRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.kbDocumentRepository = kbDocumentRepository;
        this.toolCatalogRepository = toolCatalogRepository;
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringOrArrayDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    static class StringOrArrayDeserializer extends StdDeserializer<String> {

        public StringOrArrayDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.getCurrentToken();
            if (token.isNumeric()) {
                return p.getValueAsString();
            }
            if (token == JsonToken.VALUE_STRING) {
                return p.getValueAsString();
            }
            if (token == JsonToken.START_ARRAY) {
                JsonNode array = p.getCodec().readTree(p);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < array.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(array.get(i).asText());
                }
                return sb.toString();
            }
            return p.getValueAsString();
        }
    }

    @PostConstruct
    public void loadData() throws Exception {
        // Check if data already exists to avoid duplicates
        if (customerRepository.count() > 0) {
            System.out.println("Data already loaded. Skipping...");
            return;
        }

        System.out.println("Starting data loading...");

        // Load customers
        List<Customer> customers = loadJsonFile("data/customers.json", Customer[].class);
        customerRepository.saveAll(customers);
        System.out.println("Loaded " + customers.size() + " customers");

        // Load orders
        List<Order> orders = loadJsonFile("data/orders.json", Order[].class);
        orderRepository.saveAll(orders);
        System.out.println("Loaded " + orders.size() + " orders");

        // Load tickets
        List<Ticket> tickets = loadJsonFile("data/tickets.json", Ticket[].class);
        ticketRepository.saveAll(tickets);
        System.out.println("Loaded " + tickets.size() + " tickets");

        // Load KB documents with custom tags deserialization
        List<KBDocument> kbDocuments = loadJsonFile("data/kb_documents.json", KBDocument[].class);
        kbDocumentRepository.saveAll(kbDocuments);
        System.out.println("Loaded " + kbDocuments.size() + " KB documents");

        // Load tool catalog
        List<ToolCatalog> toolCatalog = loadJsonFile("data/tool_catalog.json", ToolCatalog[].class);
        toolCatalogRepository.saveAll(toolCatalog);
        System.out.println("Loaded " + toolCatalog.size() + " tools");

        System.out.println("Data loading complete!");
    }

    private <T> List<T> loadJsonFile(String filePath, Class<T[]> clazz) throws Exception {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (InputStream inputStream = resource.getInputStream()) {
            T[] array = objectMapper.readValue(inputStream, clazz);
            return Arrays.asList(array);
        }
    }
}
