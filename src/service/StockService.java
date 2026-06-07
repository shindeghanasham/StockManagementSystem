package service;

import model.Product;
import model.Category;
import model.Bill;
import persistence.DataStorage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StockService {
	private Map<String, Product> products;
	private Map<String, Category> categories;
	private AuditService auditService;
	private List<Map<String, Object>> salesRecords;
	private List<Bill> bills;
	private String currentUsername = "System";

	public StockService() {
		this.products = DataStorage.loadProducts();
		this.categories = DataStorage.loadCategories();
		this.auditService = new AuditService();
		this.salesRecords = DataStorage.loadSales();
		this.bills = DataStorage.loadBills();
	}

	public void saveBillsToFile() {
		DataStorage.saveBills(bills);
	}

	public List<Bill> getDeletedBills() {
		return bills.stream().filter(Bill::isDeleted).collect(Collectors.toList());
	}

	public List<Bill> getActiveBills() {
		return bills.stream().filter(b -> !b.isDeleted()).collect(Collectors.toList());
	}

	public boolean permanentDeleteBill(String billNumber) {
		Bill bill = getBillByNumber(billNumber);
		if (bill != null) {
			bills.remove(bill);
			saveBillsToFile();
			auditService.logAction("PERMANENT_DELETE_BILL", billNumber, 0, bill.getGrandTotal());
			return true;
		}
		return false;
	}

	public boolean restoreBill(String billNumber, String restoredBy) {
		Bill bill = getBillByNumber(billNumber);
		if (bill != null && bill.isDeleted()) {
			bill.restore(restoredBy);
			saveBillsToFile();
			auditService.logAction("RESTORE_BILL", billNumber, 0, bill.getGrandTotal());
			return true;
		}
		return false;
	}

	private Bill getBillByNumber(String billNumber) {
		return bills.stream().filter(b -> b.getBillNumber().equals(billNumber)).findFirst().orElse(null);
	}

	public void setCurrentUser(String username) {
		this.currentUsername = username;
		auditService.setCurrentUser(username);
	}

	// CREATE Operation
	public boolean addProduct(Product product) {
		if (products.containsKey(product.getId())) {
			return false;
		}
		product.setAddedBy(currentUsername);
		product.setUpdatedBy(currentUsername);
		products.put(product.getId(), product);
		saveData();
		auditService.logAction("ADD", product.getName(), product.getQuantity(), product.getSellPrice());
		checkStockAlerts(product);
		return true;
	}

	// READ Operations
	public Product getProduct(String id) {
		return products.get(id);
	}

	public Product getProductByBarcode(String barcode) {
		return products.values().stream().filter(p -> p.getBarcode().equals(barcode)).findFirst().orElse(null);
	}

	public List<Product> getAllProducts() {
		return new ArrayList<>(products.values());
	}

	public List<Product> getActiveProducts() {
		return products.values().stream().filter(Product::isActive).collect(Collectors.toList());
	}

	public List<Product> searchProducts(String keyword) {
		String lowerKeyword = keyword.toLowerCase();
		return products.values().stream().filter(p -> p.isActive())
				.filter(p -> p.getName().toLowerCase().contains(lowerKeyword)
						|| p.getId().toLowerCase().contains(lowerKeyword)
						|| p.getBarcode().toLowerCase().contains(lowerKeyword)
						|| p.getBrand().toLowerCase().contains(lowerKeyword))
				.collect(Collectors.toList());
	}

	public List<Product> getProductsByCategory(String categoryId) {
		return products.values().stream().filter(p -> p.getCategoryId().equals(categoryId) && p.isActive())
				.collect(Collectors.toList());
	}

	public List<Product> getProductsByBrand(String brand) {
		return products.values().stream().filter(p -> p.getBrand().equalsIgnoreCase(brand) && p.isActive())
				.collect(Collectors.toList());
	}

	public List<Product> getProductsBySupplier(String supplier) {
		return products.values().stream().filter(p -> p.getSupplier().equalsIgnoreCase(supplier) && p.isActive())
				.collect(Collectors.toList());
	}

	public List<Product> getLowStockProducts() {
		return products.values().stream().filter(Product::isLowStock).filter(Product::isActive)
				.collect(Collectors.toList());
	}

	public List<Product> getOverStockProducts() {
		return products.values().stream().filter(Product::isOverStock).filter(Product::isActive)
				.collect(Collectors.toList());
	}

	public List<Product> getExpiringProducts() {
		return products.values().stream().filter(p -> p.isActive())
				.filter(p -> p.getQuantity() <= p.getMinStockLevel() * 1.2).collect(Collectors.toList());
	}

	// UPDATE Operations
	public boolean updateProduct(Product updatedProduct) {
		Product existing = products.get(updatedProduct.getId());
		if (existing == null) {
			return false;
		}

		// Preserve system fields
		updatedProduct.setAddedDate(existing.getAddedDate());
		updatedProduct.setAddedBy(existing.getAddedBy());
		updatedProduct.setLastUpdated(LocalDateTime.now());
		updatedProduct.setUpdatedBy(currentUsername);

		products.put(updatedProduct.getId(), updatedProduct);
		saveData();
		auditService.logAction("UPDATE", updatedProduct.getName(),
				updatedProduct.getQuantity() - existing.getQuantity(), updatedProduct.getSellPrice());
		checkStockAlerts(updatedProduct);
		return true;
	}

	public boolean updateQuantity(String productId, int newQuantity) {
		Product product = products.get(productId);
		if (product == null)
			return false;

		int oldQuantity = product.getQuantity();
		product.setQuantity(newQuantity);
		product.setUpdatedBy(currentUsername);
		product.setLastUpdated(LocalDateTime.now());
		saveData();
		auditService.logAction("UPDATE_STOCK", product.getName(), newQuantity - oldQuantity, product.getSellPrice());
		checkStockAlerts(product);
		return true;
	}

	public boolean updatePrice(String productId, double newSellPrice) {
		Product product = products.get(productId);
		if (product == null)
			return false;

		double oldPrice = product.getSellPrice();
		product.setSellPrice(newSellPrice);
		product.setUpdatedBy(currentUsername);
		product.setLastUpdated(LocalDateTime.now());
		saveData();
		auditService.logAction("UPDATE_PRICE", product.getName(), 0, newSellPrice - oldPrice);
		return true;
	}

	public boolean addStock(String productId, int quantity, String reason) {
		Product product = products.get(productId);
		if (product == null)
			return false;

		int oldQuantity = product.getQuantity();
		product.setQuantity(oldQuantity + quantity);
		product.setUpdatedBy(currentUsername);
		product.setLastUpdated(LocalDateTime.now());
		saveData();
		auditService.logAction("ADD_STOCK", product.getName(), quantity, product.getSellPrice());
		auditService.logAction("REASON", reason, 0, 0);
		checkStockAlerts(product);
		return true;
	}

	public boolean removeStock(String productId, int quantity, String reason) {
		Product product = products.get(productId);
		if (product == null || product.getQuantity() < quantity)
			return false;

		int oldQuantity = product.getQuantity();
		product.setQuantity(oldQuantity - quantity);
		product.setUpdatedBy(currentUsername);
		product.setLastUpdated(LocalDateTime.now());
		saveData();
		auditService.logAction("REMOVE_STOCK", product.getName(), quantity, product.getSellPrice());
		auditService.logAction("REASON", reason, 0, 0);
		checkStockAlerts(product);
		return true;
	}

	// DELETE Operations
	public boolean deleteProduct(String productId) {
		Product product = products.remove(productId);
		if (product != null) {
			saveData();
			auditService.logAction("DELETE", product.getName(), product.getQuantity(), product.getSellPrice());
			return true;
		}
		return false;
	}

	public boolean softDeleteProduct(String productId) {
		Product product = products.get(productId);
		if (product == null)
			return false;

		product.setActive(false);
		product.setUpdatedBy(currentUsername);
		product.setLastUpdated(LocalDateTime.now());
		saveData();
		auditService.logAction("SOFT_DELETE", product.getName(), product.getQuantity(), product.getSellPrice());
		return true;
	}

	public boolean restoreProduct(String productId) {
		Product product = products.get(productId);
		if (product == null)
			return false;

		product.setActive(true);
		product.setUpdatedBy(currentUsername);
		product.setLastUpdated(LocalDateTime.now());
		saveData();
		auditService.logAction("RESTORE", product.getName(), product.getQuantity(), product.getSellPrice());
		return true;
	}

	public boolean bulkDeleteProducts(List<String> productIds) {
		boolean allDeleted = true;
		for (String id : productIds) {
			if (!deleteProduct(id)) {
				allDeleted = false;
			}
		}
		if (allDeleted) {
			saveData();
		}
		return allDeleted;
	}

	// Bulk Operations
	public boolean bulkUpdatePrices(Map<String, Double> priceUpdates) {
		for (Map.Entry<String, Double> entry : priceUpdates.entrySet()) {
			Product product = products.get(entry.getKey());
			if (product != null) {
				product.setSellPrice(entry.getValue());
				product.setUpdatedBy(currentUsername);
				product.setLastUpdated(LocalDateTime.now());
			}
		}
		saveData();
		auditService.logAction("BULK_PRICE_UPDATE", "Multiple products", priceUpdates.size(), 0);
		return true;
	}

	public boolean bulkAddStock(Map<String, Integer> stockUpdates) {
		for (Map.Entry<String, Integer> entry : stockUpdates.entrySet()) {
			Product product = products.get(entry.getKey());
			if (product != null) {
				product.setQuantity(product.getQuantity() + entry.getValue());
				product.setUpdatedBy(currentUsername);
				product.setLastUpdated(LocalDateTime.now());
			}
		}
		saveData();
		auditService.logAction("BULK_STOCK_ADD", "Multiple products", stockUpdates.size(), 0);
		return true;
	}

	// Stock Transfer
	public boolean transferStock(String fromProductId, String toProductId, int quantity) {
		Product fromProduct = products.get(fromProductId);
		Product toProduct = products.get(toProductId);

		if (fromProduct == null || toProduct == null)
			return false;
		if (fromProduct.getQuantity() < quantity)
			return false;

		fromProduct.setQuantity(fromProduct.getQuantity() - quantity);
		toProduct.setQuantity(toProduct.getQuantity() + quantity);

		fromProduct.setUpdatedBy(currentUsername);
		toProduct.setUpdatedBy(currentUsername);
		fromProduct.setLastUpdated(LocalDateTime.now());
		toProduct.setLastUpdated(LocalDateTime.now());

		saveData();
		auditService.logAction("STOCK_TRANSFER", fromProduct.getName() + " -> " + toProduct.getName(), quantity, 0);
		return true;
	}

	// Validation Methods
	public boolean isProductIdExists(String id) {
		return products.containsKey(id);
	}

	public boolean isBarcodeExists(String barcode) {
		return products.values().stream().anyMatch(p -> p.getBarcode().equals(barcode));
	}

	public Map<String, Object> getProductStatistics() {
		Map<String, Object> stats = new HashMap<>();
		List<Product> activeProducts = getActiveProducts();

		stats.put("totalProducts", activeProducts.size());
		stats.put("totalStock", activeProducts.stream().mapToInt(Product::getQuantity).sum());
		stats.put("totalValue", activeProducts.stream().mapToDouble(Product::getTotalValue).sum());
		stats.put("totalProfit", activeProducts.stream().mapToDouble(Product::getProfit).sum());
		stats.put("lowStockCount", getLowStockProducts().size());
		stats.put("overStockCount", getOverStockProducts().size());
		stats.put("averageProfitMargin",
				activeProducts.stream().mapToDouble(Product::getProfitMargin).average().orElse(0));

		return stats;
	}

	private void checkStockAlerts(Product product) {
		if (product.isLowStock()) {
			String alert = String.format("⚠️ LOW STOCK ALERT: Product '%s' has only %d units left! (Minimum: %d)",
					product.getName(), product.getQuantity(), product.getMinStockLevel());
			System.err.println(alert);
		}
		if (product.isOverStock()) {
			String alert = String.format("⚠️ OVER STOCK ALERT: Product '%s' has %d units! (Maximum: %d)",
					product.getName(), product.getQuantity(), product.getMaxStockLevel());
			System.err.println(alert);
		}
	}

	// Category Management
	public void addCategory(Category category) {
		categories.put(category.getId(), category);
		DataStorage.saveCategories(categories);
		auditService.logAction("ADD_CATEGORY", category.getName(), 0, 0);
	}

	public Map<String, Category> getAllCategories() {
		return categories;
	}

	public String getCategoryName(String categoryId) {
		Category category = categories.get(categoryId);
		return category != null ? category.getName() : "Uncategorized";
	}

	// Sales and Billing
	public boolean sellProduct(String productId, int quantity, String username) {
		Product product = products.get(productId);
		if (product == null || product.getQuantity() < quantity || !product.isActive()) {
			return false;
		}

		double subtotal = quantity * product.getSellPrice();
		double gst = subtotal * 0.18;
		double grandTotal = subtotal + gst;

		product.setQuantity(product.getQuantity() - quantity);
		product.setUpdatedBy(username);
		product.setLastUpdated(LocalDateTime.now());

		// Record sale
		Map<String, Object> sale = new HashMap<>();
		sale.put("date", java.time.LocalDateTime.now()
				.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		sale.put("productId", productId);
		sale.put("productName", product.getName());
		sale.put("category", getCategoryName(product.getCategoryId()));
		sale.put("quantity", quantity);
		sale.put("unitPrice", product.getSellPrice());
		sale.put("subtotal", subtotal);
		sale.put("gst", gst);
		sale.put("grandTotal", grandTotal);
		sale.put("soldBy", username);

		salesRecords.add(sale);
		saveData();
		auditService.logAction("SELL", product.getName(), quantity, product.getSellPrice());
		checkStockAlerts(product);

		return true;
	}

	public void saveBill(Bill bill) {
		bills.add(bill);
		DataStorage.saveBills(bills);
		auditService.logAction("BILL", bill.getBillNumber(), bill.getItems().size(), bill.getGrandTotal());
	}

	public List<Bill> getBills() {
		return new ArrayList<>(bills);
	}

	public List<Bill> getBillsByDate(LocalDate date) {
		return bills.stream().filter(bill -> bill.getBillDate().toLocalDate().equals(date))
				.collect(Collectors.toList());
	}

	public double getTodaySales() {
		return getBillsByDate(LocalDate.now()).stream().mapToDouble(Bill::getGrandTotal).sum();
	}

	public int getTodayTransactions() {
		return getBillsByDate(LocalDate.now()).size();
	}

	public List<Map<String, Object>> getSalesRecords() {
		return new ArrayList<>(salesRecords);
	}

	private void saveData() {
		DataStorage.saveProducts(products);
		DataStorage.saveSales(salesRecords);
	}

	public AuditService getAuditService() {
		return auditService;
	}
}