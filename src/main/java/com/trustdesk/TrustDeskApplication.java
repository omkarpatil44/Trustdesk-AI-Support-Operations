package com.trustdesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustdesk.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TrustDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrustDeskApplication.class, args);
    }

    @Bean
    public CommandLineRunner testDataRetrieval(RetrievalService retrievalService, ObjectMapper objectMapper) {
        return args -> {
            System.out.println();
            System.out.println("=== TESTING RETRIEVAL SERVICE ===");
            System.out.println();
            
            var kbDocs = retrievalService.searchKBDocuments("refund");
            kbDocs.forEach(doc -> System.out.println("  - " + doc.getDocId() + ": " + doc.getTitle()));
            
            var customer = retrievalService.getCustomerById("CUST-001");
            if (customer != null) {
                System.out.println("  Customer: " + customer.getName() + " (" + customer.getEmail() + ")");
                var orders = retrievalService.getCustomerOrders("CUST-001");
                System.out.println("  Orders: " + orders.size());
                var tickets = retrievalService.getCustomerTicketHistory("CUST-001");
                System.out.println("  Tickets: " + tickets.size());
            }
            
            var context = new ContextData();
            context.setCustomer(retrievalService.getCustomerById("CUST-001"));
            context.setOrders(retrievalService.getCustomerOrders("CUST-001"));
            context.setTickets(retrievalService.getCustomerTicketHistory("CUST-001"));
            context.setKbDocuments(retrievalService.searchKBDocuments("shipping"));
            
            String formatted = FormattedContext.formatForLLM(context);
            System.out.println();
            System.out.println("Formatted context length: " + formatted.length() + " chars");
            
            System.out.println();
            System.out.println("=== RETRIEVAL TESTS COMPLETE ===");
            System.out.println();
        };
    }
}