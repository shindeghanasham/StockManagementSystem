package service;

import model.Product;
import model.Category;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkImportService {
	private StockService stockService;
	private AuthService authService;
	private List<String> errors;
	private int successCount;
	private int failCount;

	public BulkImportService(StockService stockService, AuthService authService) {
		this.stockService = stockService;
		this.authService = authService;
		this.errors = new ArrayList<>();
		this.successCount = 0;
		this.failCount = 0;
	}

	public Map<String, Object> importProductsFromCsv(File csvFile) {
		resetCounters();
		Map<String, Object> result = new HashMap<>();

		if (csvFile == null || !csvFile.getName().toLowerCase().endsWith(".csv")) {
			result.put("success", false);
			result.put("message", "Unsupported file format. Please use a .csv file.");
			result.put("errors", errors);
			return result;
		}

		try {
			List<String> lines = Files.readAllLines(csvFile.toPath(), StandardCharsets.UTF_8);

			if (lines.isEmpty()) {
				result.put("success", false);
				result.put("message", "The file is empty.");
				result.put("errors", errors);
				return result;
			}

			String[] header = splitCsvLine(lines.get(0));
			if (!validateHeader(header)) {
				result.put("success", false);
				result.put("message", "Invalid CSV format. Please use the template.");
				result.put("errors", errors);
				return result;
			}

			for (int rowNum = 1; rowNum < lines.size(); rowNum++) {
				String line = lines.get(rowNum).trim();
				if (line.isEmpty()) {
					continue;
				}

				String[] values = splitCsvLine(line);
				processRow(values, rowNum + 1);
			}

			result.put("success", true);
			result.put("successCount", successCount);
			result.put("failCount", failCount);
			result.put("errors", errors);
			result.put("message", String.format("Import completed! %d successful, %d failed", successCount, failCount));
		} catch (IOException e) {
			result.put("success", false);
			result.put("message", "Error reading file: " + e.getMessage());
			result.put("errors", errors);
		}

		return result;
	}

	private boolean validateHeader(String[] headerRow) {
		if (headerRow == null || headerRow.length < 16) {
			return false;
		}

		String[] expectedHeaders = { "Product ID", "Barcode", "Product Name", "Category", "Quantity", "Min Stock Level",
			"Max Stock Level", "Cost Price", "Sell Price", "MRP", "Brand", "Supplier", "Location", "Unit",
			"Weight (kg)", "Description" };

		for (int i = 0; i < expectedHeaders.length; i++) {
			if (!expectedHeaders[i].equalsIgnoreCase(headerRow[i].trim())) {
				return false;
			}
		}
		return true;
	}

private void processRow(String[] values, int rowNum) {
		try {
			String id = getField(values, 0).trim();
			String barcode = getField(values, 1).trim();
			String name = getField(values, 2).trim();
			String categoryName = getField(values, 3).trim();
			int quantity = (int) parseDouble(getField(values, 4));
			int minStock = (int) parseDouble(getField(values, 5));
			int maxStock = (int) parseDouble(getField(values, 6));
			double costPrice = parseDouble(getField(values, 7));
			double sellPrice = parseDouble(getField(values, 8));
			double mrp = parseDouble(getField(values, 9));
			String brand = getField(values, 10);
			String supplier = getField(values, 11);
			String location = getField(values, 12);
			String unit = getField(values, 13);
			double weight = parseDouble(getField(values, 14));
			String description = getField(values, 15);

			if (id.isEmpty()) {
				addError(rowNum, "Product ID is required");
				return;
			}

			if (name.isEmpty()) {
				addError(rowNum, "Product Name is required");
				return;
			}

			if (categoryName.isEmpty()) {
				addError(rowNum, "Category is required");
				return;
			}

			if (quantity < 0) {
				addError(rowNum, "Quantity cannot be negative");
				return;
			}

			if (sellPrice <= 0) {
				addError(rowNum, "Sell price must be greater than 0");
				return;
			}

			if (stockService.isProductIdExists(id)) {
				addError(rowNum, "Product ID already exists: " + id);
				return;
			}

			String categoryId = getOrCreateCategory(categoryName);
			if (categoryId == null) {
				addError(rowNum, "Failed to create category: " + categoryName);
				return;
			}

			if (barcode.isEmpty()) {
				barcode = id;
			}

			if (minStock <= 0)
				minStock = 10;
			if (maxStock <= 0)
				maxStock = minStock * 10;
			if (mrp <= 0)
				mrp = sellPrice;
			if (unit.isEmpty())
				unit = "Pcs";

			Product product = new Product(id, barcode, name, categoryId, quantity, minStock, maxStock, costPrice,
				sellPrice, mrp, brand, supplier, location, unit, weight, description,
				authService.getCurrentUser().getUsername());

			if (stockService.addProduct(product)) {
				successCount++;
			} else {
				addError(rowNum, "Failed to add product");
			}

		} catch (Exception e) {
			addError(rowNum, "Error processing row: " + e.getMessage());
		}
	}

	private String getField(String[] values, int index) {
		if (values == null || index < 0 || index >= values.length) {
			return "";
		}
		return values[index];
	}

	private String getOrCreateCategory(String categoryName) {
		// Check if category exists
		for (Category cat : stockService.getAllCategories().values()) {
			if (cat.getName().equalsIgnoreCase(categoryName)) {
				return cat.getId();
			}
		}

		// Create new category
		String categoryId = "CAT" + System.currentTimeMillis();
		Category newCategory = new Category(categoryId, categoryName, "Auto-created from bulk import");
		stockService.addCategory(newCategory);

		return categoryId;
	}

	private String[] splitCsvLine(String line) {
		List<String> tokens = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				tokens.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		tokens.add(current.toString());
		return tokens.toArray(new String[0]);
	}

	private double parseDouble(String value) {
		if (value == null || value.trim().isEmpty()) {
			return 0;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void addError(int rowNum, String message) {
		failCount++;
		errors.add(String.format("Row %d: %s", rowNum, message));
	}

	private void resetCounters() {
		successCount = 0;
		failCount = 0;
		errors.clear();
	}

	public File createImportTemplate() {
		try {
			File tempFile = File.createTempFile("product_import_template", ".csv");
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
				String[] headers = { "Product ID", "Barcode", "Product Name", "Category", "Quantity",
					"Min Stock Level", "Max Stock Level", "Cost Price", "Sell Price", "MRP", "Brand", "Supplier",
					"Location", "Unit", "Weight (kg)", "Description" };
				writer.write(String.join(",", headers));
				writer.newLine();

				String[] example = { "PROD001", "123456789", "Sample Product", "Electronics", "100", "10", "500", "500",
					"750", "1000", "Samsung", "Samsung Corp", "Warehouse A", "Pcs", "1.5",
					"This is a sample product description" };
				writer.write(String.join(",", example));
				writer.newLine();
			}
			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}