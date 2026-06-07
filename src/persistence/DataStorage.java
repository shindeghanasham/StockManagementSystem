package persistence;

import model.*;
import java.io.*;
import java.util.*;

public class DataStorage {
	private static final String PRODUCTS_FILE = "data/products.dat";
	private static final String USERS_FILE = "data/users.dat";
	private static final String CATEGORIES_FILE = "data/categories.dat";
	private static final String AUDIT_FILE = "data/audit_log.txt";
	private static final String SALES_FILE = "data/sales.dat";
	private static final String BILLS_FILE = "data/bills.dat";

	// Make sure this method exists in DataStorage.java
	public static void saveBills(List<Bill> bills) {
	    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BILLS_FILE))) {
	        oos.writeObject(bills);
	    } catch (IOException e) {
	        System.err.println("Error saving bills: " + e.getMessage());
	    }
	}

	@SuppressWarnings("unchecked")
	public static List<Bill> loadBills() {
		File file = new File(BILLS_FILE);
		if (!file.exists())
			return new ArrayList<>();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			return (List<Bill>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return new ArrayList<>();
		}
	}

	static {
		// Create data directory if not exists
		new File("data").mkdirs();
	}

	// Product persistence
	public static void saveProducts(Map<String, Product> products) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PRODUCTS_FILE))) {
			oos.writeObject(products);
		} catch (IOException e) {
			System.err.println("Error saving products: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Product> loadProducts() {
		File file = new File(PRODUCTS_FILE);
		if (!file.exists())
			return new HashMap<>();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			return (Map<String, Product>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return new HashMap<>();
		}
	}

	// User persistence
	public static void saveUsers(Map<String, User> users) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
			oos.writeObject(users);
		} catch (IOException e) {
			System.err.println("Error saving users: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, User> loadUsers() {
		File file = new File(USERS_FILE);
		if (!file.exists())
			return createDefaultUsers();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			return (Map<String, User>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return createDefaultUsers();
		}
	}

	private static Map<String, User> createDefaultUsers() {
		Map<String, User> users = new HashMap<>();
		users.put("admin", new User("admin", "admin123", "ADMIN", "System Administrator"));
		users.put("user", new User("user", "user123", "USER", "Regular User"));
		saveUsers(users);
		return users;
	}

	// Category persistence
	public static void saveCategories(Map<String, Category> categories) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CATEGORIES_FILE))) {
			oos.writeObject(categories);
		} catch (IOException e) {
			System.err.println("Error saving categories: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Category> loadCategories() {
		File file = new File(CATEGORIES_FILE);
		if (!file.exists())
			return createDefaultCategories();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			return (Map<String, Category>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return createDefaultCategories();
		}
	}

	private static Map<String, Category> createDefaultCategories() {
		Map<String, Category> categories = new HashMap<>();
		categories.put("CAT001", new Category("CAT001", "Electronics", "Electronic items and gadgets"));
		categories.put("CAT002", new Category("CAT002", "Clothing", "Apparel and fashion items"));
		categories.put("CAT003", new Category("CAT003", "Groceries", "Food and daily essentials"));
		categories.put("CAT004", new Category("CAT004", "Furniture", "Home and office furniture"));
		saveCategories(categories);
		return categories;
	}

	// Sales record persistence
	public static void saveSales(List<Map<String, Object>> sales) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SALES_FILE))) {
			oos.writeObject(sales);
		} catch (IOException e) {
			System.err.println("Error saving sales: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> loadSales() {
		File file = new File(SALES_FILE);
		if (!file.exists())
			return new ArrayList<>();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			return (List<Map<String, Object>>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return new ArrayList<>();
		}
	}

	// Audit log persistence
	public static void saveAuditLog(String logEntry) {
		try (FileWriter fw = new FileWriter(AUDIT_FILE, true); BufferedWriter bw = new BufferedWriter(fw)) {
			bw.write(logEntry);
			bw.newLine();
		} catch (IOException e) {
			System.err.println("Error saving audit log: " + e.getMessage());
		}
	}

	public static List<String> loadAuditLog() {
		List<String> logs = new ArrayList<>();
		File file = new File(AUDIT_FILE);
		if (!file.exists())
			return logs;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				logs.add(line);
			}
		} catch (IOException e) {
			System.err.println("Error loading audit log: " + e.getMessage());
		}
		return logs;
	}
}