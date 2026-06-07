package service;

import persistence.DataStorage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String currentUser = "System"; // Default user
    
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    public void logAction(String action, String details, int count, double amount) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] %s | User: %s | Details: %s | Count: %d | Amount: ₹%.2f", 
                                       timestamp, action, currentUser, details, count, amount);
        DataStorage.saveAuditLog(logEntry);
        System.out.println(logEntry);
    }
    
    public List<String> getAuditLog() {
        return DataStorage.loadAuditLog();
    }
    
    public void displayAuditReport() {
        List<String> logs = getAuditLog();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STOCK AUDIT REPORT");
        System.out.println("=".repeat(80));
        
        if (logs.isEmpty()) {
            System.out.println("No audit records found.");
        } else {
            for (String log : logs) {
                System.out.println(log);
            }
        }
        System.out.println("=".repeat(80) + "\n");
    }
}