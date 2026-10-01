package com.trustdesk.controller;

import com.trustdesk.entity.Customer;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.Ticket;
import com.trustdesk.service.AITriageService;
import com.trustdesk.service.RetrievalService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/triage")
@CrossOrigin(origins = "http://localhost:3000")
public class TriageController {

    private final AITriageService aiTriageService;
    private final RetrievalService retrievalService;

    public TriageController(AITriageService aiTriageService, 
                           RetrievalService retrievalService) {
        this.aiTriageService = aiTriageService;
        this.retrievalService = retrievalService;
    }

    /**
     * Perform AI triage on a ticket
     */
    @PostMapping("/{ticketId}")
    public Map<String, Object> triageTicket(@PathVariable String ticketId) {
        // Get ticket
        Ticket ticket = retrievalService.getTicketById(ticketId);
        if (ticket == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ticket not found");
            return error;
        }

        // Get customer
        Customer customer = retrievalService.getCustomerById(ticket.getCustomerId());

        // Get orders
        List<Order> orders = retrievalService.getCustomerOrders(ticket.getCustomerId());

        // Get relevant KB documents for context
        List<String> kbContext = retrievalService.getRelevantKBContext(ticket.getSubject() + " " + ticket.getDescription());

        // Perform triage
        Map<String, Object> triageResult = aiTriageService.triageTicket(
            ticketId, ticket, customer, orders, kbContext
        );

        // Add ticket metadata
        triageResult.put("ticketId", ticketId);
        triageResult.put("subject", ticket.getSubject());

        return triageResult;
    }
}
