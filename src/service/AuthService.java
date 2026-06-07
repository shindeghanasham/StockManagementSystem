package service;

import model.User;
import persistence.DataStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AuthService {
	private Map<String, User> users;
	private User currentUser;
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	// private String currentUser = "System";

	public AuthService() {
		this.users = DataStorage.loadUsers();
	}

	public boolean login(String username, String password) {
		User user = users.get(username);
		if (user != null && user.getPassword().equals(password)) {
			currentUser = user;
			return true;
		}
		return false;
	}

	public void logAction(String action, String productName, int quantity, double price) {
		String timestamp = LocalDateTime.now().format(FORMATTER);
		String logEntry = String.format("[%s] %s | User: %s | Product: %s | Qty: %d | Price: ₹%.2f", timestamp, action,
				currentUser, productName, quantity, price);
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

	public void logout() {
		currentUser = null;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public boolean isAdmin() {
		return currentUser != null && "ADMIN".equals(currentUser.getRole());
	}

	public boolean addUser(User user) {
		if (users.containsKey(user.getUsername())) {
			return false;
		}
		users.put(user.getUsername(), user);
		DataStorage.saveUsers(users);
		return true;
	}

	public boolean deleteUser(String username) {
		if (username.equals("admin")) {
			return false; // Cannot delete main admin
		}
		User removed = users.remove(username);
		if (removed != null) {
			DataStorage.saveUsers(users);
			return true;
		}
		return false;
	}

	public Map<String, User> getAllUsers() {
		return users;
	}

	public boolean changePassword(String username, String oldPassword, String newPassword) {
		User user = users.get(username);
		if (user != null && user.getPassword().equals(oldPassword)) {
			user.setPassword(newPassword);
			DataStorage.saveUsers(users);
			return true;
		}
		return false;
	}
}