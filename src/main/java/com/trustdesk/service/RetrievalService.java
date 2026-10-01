package com.trustdesk.service;

import com.trustdesk.entity.*;
import com.trustdesk.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class RetrievalService {

    private final KBDocumentRepository kbDocumentRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    public RetrievalService(KBDocumentRepository kbDocumentRepository,
                            CustomerRepository customerRepository,
                            OrderRepository orderRepository,
                            TicketRepository ticketRepository) {
        this.kbDocumentRepository = kbDocumentRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Search KB documents by query string (searches title and content)
     */
    public List<KBDocument> searchKBDocuments(String query) {
        return kbDocumentRepository.searchByQuery(query);
    }

    /**
     * Search KB documents by category and query
     */
    public List<KBDocument> searchKBDocumentsByCategory(String category, String query) {
        return kbDocumentRepository.searchByCategoryAndQuery(category, query);
    }

    /**
     * Get customer by ID
     */
    public Customer getCustomerById(String customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    /**
     * Get orders by customer ID
     */
    public List<Order> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * Get recent ticket history for a customer
     */
    public List<Ticket> getCustomerTicketHistory(String customerId) {
        return ticketRepository.findRecentTicketsByCustomerId(customerId, org.springframework.data.domain.Pageable.ofSize(5));
    }

    /**
     * Get tickets by status
     */
    public List<Ticket> getTicketsByStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    /**
     * Get KB document by category
     */
    public List<KBDocument> getKBDocumentsByCategory(String category) {
        return kbDocumentRepository.findByCategory(category);
    }

    /**
     * Get a ticket by ID
     */
    public Ticket getTicketById(String ticketId) {
        return ticketRepository.findById(ticketId).orElse(null);
    }

    /**
     * Get relevant KB context for a ticket query
     */
    public List<String> getRelevantKBContext(String query) {
        List<KBDocument> docs = searchKBDocuments(query);
        return docs.stream()
            .map(doc -> doc.getTitle() + "\n" + doc.getContent())
            .collect(Collectors.toList());
    }

    /**
     * Get triage result for a ticket (helper for evals)
     */
    public Map<String, Object> getTriageResultForTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            return Map.of("error", "Ticket not found");
        }

        Customer customer = customerRepository.findById(ticket.getCustomerId()).orElse(null);
        List<Order> orders = orderRepository.findByCustomerId(ticket.getCustomerId());
        
        // Get relevant KB documents and extract their IDs
        List<KBDocument> kbDocs = searchKBDocuments(ticket.getSubject() + " " + ticket.getDescription());
        List<String> citations = kbDocs.stream()
            .map(KBDocument::getDocId)
            .collect(Collectors.toList());

        return Map.of(
            "category", ticket.getCategory() != null ? ticket.getCategory() : "general",
            "priority", ticket.getPriority() != null ? ticket.getPriority() : "medium",
            "citations", citations,
            "should_escalate", false
        );
    }
}
