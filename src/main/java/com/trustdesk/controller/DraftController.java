package com.trustdesk.controller;

import com.trustdesk.entity.Customer;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.Ticket;
import com.trustdesk.service.DraftGenerationService;
import com.trustdesk.service.RetrievalService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/draft")
@CrossOrigin(origins = "http://localhost:3000")
public class DraftController {

    private final DraftGenerationService draftGenerationService;
    private final RetrievalService retrievalService;

    public DraftController(DraftGenerationService draftGenerationService,
                          RetrievalService retrievalService) {
        this.draftGenerationService = draftGenerationService;
        this.retrievalService = retrievalService;
    }

    /**
     * Generate a draft reply for a ticket
     */
    @PostMapping("/{ticketId}")
    public Map<String, Object> generateDraft(@PathVariable String ticketId) {
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

        // Generate draft
        Map<String, Object> draftResult = draftGenerationService.generateDraft(
            ticketId, ticket, customer, orders, kbContext
        );

        // Add ticket metadata
        draftResult.put("ticketId", ticketId);
        draftResult.put("subject", ticket.getSubject());

        return draftResult;
    }
}
