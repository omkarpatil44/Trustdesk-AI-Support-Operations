package com.trustdesk.service;

import com.trustdesk.entity.*;

import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

/**
 * Formats ContextData into a structured string for LLM prompts
 */
public class FormattedContext {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Format context data into a structured string for LLM
     */
    public static String formatForLLM(ContextData context) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== CUSTOMER CONTEXT ===\n");
        if (context.hasCustomer()) {
            Customer customer = context.getCustomer();
            sb.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
            sb.append("Name: ").append(customer.getName()).append("\n");
            sb.append("Email: ").append(customer.getEmail()).append("\n");
            sb.append("Phone: ").append(customer.getPhone()).append("\n");
            sb.append("Account Status: ").append(customer.getAccountStatus()).append("\n");
            sb.append("Account Created: ").append(customer.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        } else {
            sb.append("No customer information found.\n");
        }
        sb.append("\n");
        
        sb.append("=== ORDER HISTORY ===\n");
        if (context.hasOrders()) {
            for (Order order : context.getOrders()) {
                sb.append("Order ID: ").append(order.getOrderId()).append("\n");
                sb.append("  Product: ").append(order.getProductName()).append("\n");
                sb.append("  Category: ").append(order.getProductCategory()).append("\n");
                sb.append("  Amount: $").append(order.getAmount()).append("\n");
                sb.append("  Status: ").append(order.getStatus()).append("\n");
                sb.append("  Purchase Date: ").append(order.getPurchaseDate().format(DATE_FORMATTER)).append("\n");
                if (order.getDeliveryDate() != null) {
                    sb.append("  Delivery Date: ").append(order.getDeliveryDate().format(DATE_FORMATTER)).append("\n");
                }
                if (order.getWarrantyExpiryDate() != null) {
                    sb.append("  Warranty Expires: ").append(order.getWarrantyExpiryDate().format(DATE_FORMATTER)).append("\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("No orders found for this customer.\n\n");
        }
        
        sb.append("=== TICKET HISTORY ===\n");
        if (context.hasTickets()) {
            for (Ticket ticket : context.getTickets()) {
                sb.append("Ticket ID: ").append(ticket.getTicketId()).append("\n");
                sb.append("  Subject: ").append(ticket.getSubject()).append("\n");
                sb.append("  Status: ").append(ticket.getStatus()).append("\n");
                sb.append("  Priority: ").append(ticket.getPriority()).append("\n");
                sb.append("  Category: ").append(ticket.getCategory()).append("\n");
                sb.append("  Created: ").append(ticket.getCreatedAt().format(DATE_FORMATTER)).append("\n");
                sb.append("  Description: ").append(ticket.getDescription()).append("\n\n");
            }
        } else {
            sb.append("No ticket history found.\n\n");
        }
        
        sb.append("=== RELEVANT KNOWLEDGE BASE ARTICLES ===\n");
        if (context.hasKbDocuments()) {
            for (KBDocument doc : context.getKbDocuments()) {
                sb.append("Document: ").append(doc.getDocId()).append(" - ").append(doc.getTitle()).append("\n");
                sb.append("Category: ").append(doc.getCategory()).append("\n");
                sb.append("Tags: ").append(doc.getTags()).append("\n");
                sb.append("Content: ").append(doc.getContent()).append("\n\n");
            }
        } else {
            sb.append("No relevant KB documents found.\n");
        }
        
        return sb.toString();
    }
}
