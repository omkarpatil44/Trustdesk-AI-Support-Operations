package com.trustdesk.controller;

import com.trustdesk.entity.*;
import com.trustdesk.repository.TicketRepository;
import com.trustdesk.service.ContextData;
import com.trustdesk.service.FormattedContext;
import com.trustdesk.service.RetrievalService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/retrieval")
@CrossOrigin(origins = "http://localhost:3000")
public class RetrievalController {

    private final RetrievalService retrievalService;
    private final TicketRepository ticketRepository;

    public RetrievalController(RetrievalService retrievalService, TicketRepository ticketRepository) {
        this.retrievalService = retrievalService;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Search KB documents by query
     */
    @GetMapping("/kb/search")
    public List<KBDocument> searchKBDocuments(@RequestParam String query) {
        return retrievalService.searchKBDocuments(query);
    }

    /**
     * Search KB documents by category and query
     */
    @GetMapping("/kb/search/category")
    public List<KBDocument> searchKBDocumentsByCategory(
            @RequestParam String category,
            @RequestParam String query) {
        return retrievalService.searchKBDocumentsByCategory(category, query);
    }

    /**
     * Get customer context by ID
     */
    @GetMapping("/customer/{customerId}")
    public Map<String, Object> getCustomerContext(@PathVariable String customerId) {
        Map<String, Object> response = new HashMap<>();
        
        Customer customer = retrievalService.getCustomerById(customerId);
        response.put("customer", customer);
        
        if (customer != null) {
            response.put("orders", retrievalService.getCustomerOrders(customerId));
            response.put("tickets", retrievalService.getCustomerTicketHistory(customerId));
        }
        
        return response;
    }

    /**
     * Get full context for a ticket (customer + orders + tickets + KB)
     */
    @GetMapping("/full-context/{customerId}")
    public Map<String, Object> getFullContext(@PathVariable String customerId) {
        ContextData context = new ContextData();
        context.setCustomer(retrievalService.getCustomerById(customerId));
        context.setOrders(retrievalService.getCustomerOrders(customerId));
        context.setTickets(retrievalService.getCustomerTicketHistory(customerId));
        
        // For demo, search KB for general info
        context.setKbDocuments(retrievalService.searchKBDocuments("refund"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("contextData", context);
        response.put("formattedContext", FormattedContext.formatForLLM(context));
        
        return response;
    }

    /**
     * Get KB documents by category
     */
    @GetMapping("/kb/category/{category}")
    public List<KBDocument> getKBDocumentsByCategory(@PathVariable String category) {
        return retrievalService.getKBDocumentsByCategory(category);
    }

    /**
     * Get tickets by status
     */
    @GetMapping("/tickets/status/{status}")
    public List<Ticket> getTicketsByStatus(@PathVariable String status) {
        return retrievalService.getTicketsByStatus(status);
    }

    /**
     * Get all tickets (for debugging)
     */
    @GetMapping("/tickets/all")
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }
}
