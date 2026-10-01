package com.trustdesk.service;

import com.trustdesk.entity.*;

import java.util.List;

/**
 * Data transfer object containing all retrieved context for a ticket
 */
public class ContextData {
    
    private Customer customer;
    private List<Order> orders;
    private List<Ticket> tickets;
    private List<KBDocument> kbDocuments;

    public ContextData() {}

    public ContextData(Customer customer, List<Order> orders, List<Ticket> tickets, List<KBDocument> kbDocuments) {
        this.customer = customer;
        this.orders = orders;
        this.tickets = tickets;
        this.kbDocuments = kbDocuments;
    }

    // Getters and Setters
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public List<KBDocument> getKbDocuments() { return kbDocuments; }
    public void setKbDocuments(List<KBDocument> kbDocuments) { this.kbDocuments = kbDocuments; }

    public boolean hasCustomer() { return customer != null; }
    public boolean hasOrders() { return orders != null && !orders.isEmpty(); }
    public boolean hasTickets() { return tickets != null && !tickets.isEmpty(); }
    public boolean hasKbDocuments() { return kbDocuments != null && !kbDocuments.isEmpty(); }
}
