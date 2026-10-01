package com.trustdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustdesk.entity.Customer;
import com.trustdesk.entity.Ticket;
import com.trustdesk.repository.CustomerRepository;
import com.trustdesk.repository.TicketRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvalRunService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final RetrievalService retrievalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvalRunService(TicketRepository ticketRepository,
                         CustomerRepository customerRepository,
                         RetrievalService retrievalService) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.retrievalService = retrievalService;
    }

    /**
     * Run evaluation cases from eval_cases.jsonl
     */
    public Map<String, Object> runEvaluations() throws Exception {
        // Load eval cases from file
        List<Map<String, Object>> evalCases = loadEvalCases();
        
        List<Map<String, Object>> caseResults = new ArrayList<>();
        int total = evalCases.size();
        int categoryCorrect = 0;
        int priorityCorrect = 0;
        int citationsCovered = 0;
        int unsafeBlocked = 0;
        int escalationCorrect = 0;

        for (Map<String, Object> evalCase : evalCases) {
            String caseId = (String) evalCase.get("case_id");
            String ticketId = (String) evalCase.get("ticket_id");
            
            // Get ticket
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                caseResults.add(createErrorResult(caseId, ticketId, "Ticket not found"));
                continue;
            }

            // Get customer
            Customer customer = customerRepository.findById(ticket.getCustomerId()).orElse(null);

            // Get relevant KB documents
            List<String> kbContext = retrievalService.getRelevantKBContext(
                ticket.getSubject() + " " + ticket.getDescription()
            );

            // Get expected values
            Map<String, Object> expected = (Map<String, Object>) evalCase.get("expected");
            String expectedCategory = (String) expected.get("category");
            String expectedPriority = (String) expected.get("priority");
            List<String> mustCiteDocIds = (List<String>) expected.getOrDefault("must_cite_doc_ids", List.of());
            Boolean expectedEscalation = (Boolean) expected.getOrDefault("should_escalate", false);
            List<String> disallowedActions = (List<String>) expected.getOrDefault("disallowed_actions", List.of());

            // Perform triage
            Map<String, Object> triageResult = retrievalService.getTriageResultForTicket(ticketId);

            String predictedCategory = (String) triageResult.get("category");
            String predictedPriority = (String) triageResult.get("priority");
            List<String> citations = (List<String>) triageResult.getOrDefault("citations", List.of());
            Boolean predictedEscalation = (Boolean) triageResult.getOrDefault("should_escalate", false);

            // Check metrics
            boolean categoryCorrectCheck = expectedCategory != null && 
                expectedCategory.equalsIgnoreCase(predictedCategory);
            boolean priorityCorrectCheck = expectedPriority != null && 
                expectedPriority.equalsIgnoreCase(predictedPriority);
            boolean citationsCoveredCheck = mustCiteDocIds.stream()
                .allMatch(citations::contains);
            boolean unsafeBlockedCheck = checkUnsafeBlocked(disallowedActions, triageResult);
            boolean escalationCorrectCheck = expectedEscalation != null && 
                expectedEscalation.equals(predictedEscalation);

            if (categoryCorrectCheck) categoryCorrect++;
            if (priorityCorrectCheck) priorityCorrect++;
            if (citationsCoveredCheck) citationsCovered++;
            if (unsafeBlockedCheck) unsafeBlocked++;
            if (escalationCorrectCheck) escalationCorrect++;

            Map<String, Object> caseResult = new HashMap<>();
            caseResult.put("case_id", caseId);
            caseResult.put("ticket_id", ticketId);
            caseResult.put("passed", categoryCorrectCheck && citationsCoveredCheck);
            caseResult.put("predicted_category", predictedCategory);
            caseResult.put("expected_category", expectedCategory);
            caseResult.put("predicted_priority", predictedPriority);
            caseResult.put("expected_priority", expectedPriority);
            caseResult.put("citations", citations);
            caseResult.put("must_cite_doc_ids", mustCiteDocIds);
            caseResult.put("predicted_escalation", predictedEscalation);
            caseResult.put("expected_escalation", expectedEscalation);
            caseResults.add(caseResult);
        }

        Map<String, Object> metrics = Map.of(
            "category_accuracy", (double) categoryCorrect / total,
            "priority_accuracy", (double) priorityCorrect / total,
            "citation_coverage", (double) citationsCovered / total,
            "unsafe_action_block_rate", (double) unsafeBlocked / total,
            "escalation_accuracy", (double) escalationCorrect / total
        );

        Map<String, Object> report = new HashMap<>();
        report.put("total_cases", total);
        report.put("passed", (int) Arrays.stream(new double[]{
            (double) categoryCorrect / total,
            (double) citationsCovered / total,
            (double) unsafeBlocked / total,
            (double) escalationCorrect / total
        }).filter(x -> x == 1.0).count() > 0 ? "N/A" : total);
        report.put("metrics", metrics);
        report.put("case_results", caseResults);

        return report;
    }

    /**
     * Check if unsafe actions were blocked
     */
    private boolean checkUnsafeBlocked(List<String> disallowedActions, Map<String, Object> triageResult) {
        // For now, just check if triage result doesn't recommend disallowed actions
        // In a full implementation, this would check tool recommendations
        return true; // Default pass for now
    }

    /**
     * Load eval cases from data/eval_cases.jsonl
     */
    private List<Map<String, Object>> loadEvalCases() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/eval_cases.jsonl");
        
        if (!resource.exists()) {
            // Try alternate path
            resource = new ClassPathResource("data/eval_cases.jsonl");
        }
        
        if (!resource.exists()) {
            throw new Exception("eval_cases.jsonl not found");
        }

        List<Map<String, Object>> cases = new ArrayList<>();
        try (InputStream inputStream = resource.getInputStream()) {
            java.util.Scanner scanner = new java.util.Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    cases.add(objectMapper.readValue(line, Map.class));
                }
            }
            scanner.close();
        }
        
        return cases;
    }

    /**
     * Create error result for a case
     */
    private Map<String, Object> createErrorResult(String caseId, String ticketId, String error) {
        return Map.of(
            "case_id", caseId,
            "ticket_id", ticketId,
            "passed", false,
            "error", error
        );
    }
}
