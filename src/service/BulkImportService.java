package service;

import model.Product;
import model.Category;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;

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

	public Map<String, Object> importProductsFromExcel(File excelFile) {
		resetCounters();
		Map<String, Object> result = new HashMap<>();

		try (InputStream inputStream = new FileInputStream(excelFile)) {
			Workbook workbook = getWorkbook(inputStream, excelFile.getName());
			Sheet sheet = workbook.getSheetAt(0);

			// Validate header row
			if (!validateHeader(sheet.getRow(0))) {
				result.put("success", false);
				result.put("message", "Invalid Excel format. Please use the template.");
				result.put("errors", errors);
				return result;
			}

			// Process each row
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);
				if (row == null || isRowEmpty(row))
					continue;

				processRow(row, rowNum + 1);
			}

			workbook.close();

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

	private Workbook getWorkbook(InputStream inputStream, String fileName) throws IOException {
		if (fileName.endsWith(".xlsx")) {
			return new XSSFWorkbook(inputStream);
		} else if (fileName.endsWith(".xls")) {
			return new HSSFWorkbook(inputStream);
		} else {
			throw new IllegalArgumentException("Unsupported file format. Please use .xlsx or .xls");
		}
	}

	private boolean validateHeader(Row headerRow) {
		if (headerRow == null)
			return false;

		String[] expectedHeaders = { "Product ID", "Barcode", "Product Name", "Category", "Quantity", "Min Stock Level",
				"Max Stock Level", "Cost Price", "Sell Price", "MRP", "Brand", "Supplier", "Location", "Unit",
				"Weight (kg)", "Description" };

		for (int i = 0; i < expectedHeaders.length; i++) {
			Cell cell = headerRow.getCell(i);
			if (cell == null || !getCellValueAsString(cell).equals(expectedHeaders[i])) {
				return false;
			}
		}
		return true;
	}

	private void processRow(Row row, int rowNum) {
		try {
			String id = getCellValueAsString(row.getCell(0)).trim();
			String barcode = getCellValueAsString(row.getCell(1)).trim();
			String name = getCellValueAsString(row.getCell(2)).trim();
			String categoryName = getCellValueAsString(row.getCell(3)).trim();
			int quantity = (int) getCellValueAsNumeric(row.getCell(4));
			int minStock = (int) getCellValueAsNumeric(row.getCell(5));
			int maxStock = (int) getCellValueAsNumeric(row.getCell(6));
			double costPrice = getCellValueAsNumeric(row.getCell(7));
			double sellPrice = getCellValueAsNumeric(row.getCell(8));
			double mrp = getCellValueAsNumeric(row.getCell(9));
			String brand = getCellValueAsString(row.getCell(10));
			String supplier = getCellValueAsString(row.getCell(11));
			String location = getCellValueAsString(row.getCell(12));
			String unit = getCellValueAsString(row.getCell(13));
			double weight = getCellValueAsNumeric(row.getCell(14));
			String description = getCellValueAsString(row.getCell(15));

			// Validate required fields
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

			// Check if product already exists
			if (stockService.isProductIdExists(id)) {
				addError(rowNum, "Product ID already exists: " + id);
				return;
			}

			// Get or create category
			String categoryId = getOrCreateCategory(categoryName);
			if (categoryId == null) {
				addError(rowNum, "Failed to create category: " + categoryName);
				return;
			}

			// Generate barcode if not provided
			if (barcode.isEmpty()) {
				barcode = id;
			}

			// Set default values for optional fields
			if (minStock <= 0)
				minStock = 10;
			if (maxStock <= 0)
				maxStock = minStock * 10;
			if (mrp <= 0)
				mrp = sellPrice;
			if (unit.isEmpty())
				unit = "Pcs";

			// Create product
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

	private String getCellValueAsString(Cell cell) {
		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			}
			return String.valueOf((long) cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		default:
			return "";
		}
	}

	private double getCellValueAsNumeric(Cell cell) {
		if (cell == null)
			return 0;

		switch (cell.getCellType()) {
		case NUMERIC:
			return cell.getNumericCellValue();
		case STRING:
			try {
				return Double.parseDouble(cell.getStringCellValue());
			} catch (NumberFormatException e) {
				return 0;
			}
		default:
			return 0;
		}
	}

	private boolean isRowEmpty(Row row) {
		for (int i = 0; i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				String value = getCellValueAsString(cell);
				if (!value.trim().isEmpty()) {
					return false;
				}
			}
		}
		return true;
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
			File tempFile = File.createTempFile("product_import_template", ".xlsx");
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet sheet = workbook.createSheet("Products");

				// Create header row
				Row headerRow = sheet.createRow(0);
				String[] headers = { "Product ID*", "Barcode", "Product Name*", "Category*", "Quantity*",
						"Min Stock Level", "Max Stock Level", "Cost Price", "Sell Price*", "MRP", "Brand", "Supplier",
						"Location", "Unit", "Weight (kg)", "Description" };

				CellStyle headerStyle = workbook.createCellStyle();
				Font headerFont = workbook.createFont();
				headerFont.setBold(true);
				headerFont.setColor(IndexedColors.WHITE.getIndex());
				headerStyle.setFont(headerFont);
				headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
				headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				headerStyle.setBorderBottom(BorderStyle.THIN);
				headerStyle.setBorderTop(BorderStyle.THIN);
				headerStyle.setBorderLeft(BorderStyle.THIN);
				headerStyle.setBorderRight(BorderStyle.THIN);

				for (int i = 0; i < headers.length; i++) {
					Cell cell = headerRow.createCell(i);
					cell.setCellValue(headers[i]);
					cell.setCellStyle(headerStyle);
					sheet.setColumnWidth(i, 5000);
				}

				// Add example data row
				Row exampleRow = sheet.createRow(1);
				String[] example = { "PROD001", "123456789", "Sample Product", "Electronics", "100", "10", "500", "500",
						"750", "1000", "Samsung", "Samsung Corp", "Warehouse A", "Pcs", "1.5",
						"This is a sample product description" };

				for (int i = 0; i < example.length; i++) {
					exampleRow.createCell(i).setCellValue(example[i]);
				}

				// Add instruction sheet
				Sheet instructionSheet = workbook.createSheet("Instructions");
				Row instructionRow = instructionSheet.createRow(0);
				instructionRow.createCell(0).setCellValue("IMPORT INSTRUCTIONS:");

				String[] instructions = { "1. Fields marked with * are required", "2. Product ID must be unique",
						"3. Category will be auto-created if not exists",
						"4. Barcode will be set to Product ID if not provided",
						"5. Minimum Stock Level default is 10 if not provided",
						"6. Maximum Stock Level default is Min Stock * 10 if not provided",
						"7. MRP defaults to Sell Price if not provided", "8. Unit defaults to 'Pcs' if not provided",
						"9. Quantity cannot be negative", "10. Sell Price must be greater than 0", "",
						"SUPPORTED FILE FORMATS: .xlsx, .xls", "MAX FILE SIZE: 10MB" };

				for (int i = 0; i < instructions.length; i++) {
					Row row = instructionSheet.createRow(i + 1);
					row.createCell(0).setCellValue(instructions[i]);
				}
				instructionSheet.setColumnWidth(0, 15000);

				// Write to file
				try (FileOutputStream fileOut = new FileOutputStream(tempFile)) {
					workbook.write(fileOut);
				}
			}
			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}