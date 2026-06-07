package service;

import model.Product;
import persistence.DataStorage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class ReportService {
	private StockService stockService;

	public ReportService(StockService stockService) {
		this.stockService = stockService;
	}

	public void exportToCsv(String filename, JTable table) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			TableModel tableModel = table.getModel();

			// Write headers
			for (int col = 0; col < tableModel.getColumnCount(); col++) {
				writer.write(escapeCsv(tableModel.getColumnName(col)));
				if (col < tableModel.getColumnCount() - 1) {
					writer.write(",");
				}
			}
			writer.newLine();

			// Write data rows
			for (int row = 0; row < tableModel.getRowCount(); row++) {
				for (int col = 0; col < tableModel.getColumnCount(); col++) {
					Object value = tableModel.getValueAt(row, col);
					writer.write(escapeCsv(value != null ? value.toString() : ""));
					if (col < tableModel.getColumnCount() - 1) {
						writer.write(",");
					}
				}
				writer.newLine();
			}

			JOptionPane.showMessageDialog(null, "CSV report exported successfully to:\n" + filename);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error exporting to CSV: " + e.getMessage(), "Export Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private String escapeCsv(String value) {
		if (value == null) {
			return "";
		}
		String escaped = value.replace("\"", "\"\"");
		if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
			return "\"" + escaped + "\"";
		}
		return escaped;
	}

	public void generateSalesReport() {
		List<Map<String, Object>> sales = DataStorage.loadSales();

		if (sales.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No sales records found!");
			return;
		}

		StringBuilder report = new StringBuilder();
		report.append("\n").append("=".repeat(80)).append("\n");
		report.append(String.format("%50s\n", "SALES REPORT"));
		report.append("=".repeat(80)).append("\n");
		report.append(String.format("Generated: %s\n",
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
		report.append("-".repeat(80)).append("\n");

		double totalRevenue = 0;
		double totalGST = 0;
		Map<String, Integer> categorySales = new HashMap<>();

		for (Map<String, Object> sale : sales) {
			report.append(String.format("Date: %s\n", sale.get("date")));
			report.append(String.format("Product: %s\n", sale.get("productName")));
			report.append(String.format("Quantity: %d\n", sale.get("quantity")));
			report.append(String.format("Unit Price: ₹%.2f\n", sale.get("unitPrice")));
			report.append(String.format("Subtotal: ₹%.2f\n", sale.get("subtotal")));
			report.append(String.format("GST (18%%): ₹%.2f\n", sale.get("gst")));
			report.append(String.format("Total: ₹%.2f\n", sale.get("grandTotal")));
			report.append("-".repeat(40)).append("\n");

			totalRevenue += (double) sale.get("grandTotal");
			totalGST += (double) sale.get("gst");

			String category = (String) sale.get("category");
			categorySales.put(category, categorySales.getOrDefault(category, 0) + (int) sale.get("quantity"));
		}

		report.append("\n").append("=".repeat(80)).append("\n");
		report.append("SUMMARY STATISTICS\n");
		report.append("=".repeat(80)).append("\n");
		report.append(String.format("Total Sales Transactions: %d\n", sales.size()));
		report.append(String.format("Total Revenue: ₹%.2f\n", totalRevenue));
		report.append(String.format("Total GST Collected: ₹%.2f\n", totalGST));
		report.append(String.format("Average Transaction Value: ₹%.2f\n", totalRevenue / sales.size()));

		report.append("\nCategory-wise Sales:\n");
		report.append("-".repeat(40)).append("\n");
		for (Map.Entry<String, Integer> entry : categorySales.entrySet()) {
			report.append(String.format("%-20s: %d units\n", entry.getKey(), entry.getValue()));
		}

		report.append("=".repeat(80)).append("\n");

		// Display report
		javax.swing.JTextArea textArea = new javax.swing.JTextArea(report.toString());
		textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
		javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
		scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));
		JOptionPane.showMessageDialog(null, scrollPane, "Sales Report", JOptionPane.INFORMATION_MESSAGE);
	}

	public void generateLowStockReport() {
		List<Product> lowStockProducts = stockService.getLowStockProducts();

		if (lowStockProducts.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No low stock products found!");
			return;
		}

		StringBuilder report = new StringBuilder();
		report.append("\n").append("=".repeat(80)).append("\n");
		report.append(String.format("%45s\n", "LOW STOCK ALERT REPORT"));
		report.append("=".repeat(80)).append("\n");
		report.append(
				String.format("%-15s %-25s %-15s %-15s\n", "Product ID", "Product Name", "Current Stock", "Min Level"));
		report.append("-".repeat(80)).append("\n");

		for (Product product : lowStockProducts) {
			report.append(String.format("%-15s %-25s %-15d %-15d\n", product.getId(), product.getName(),
					product.getQuantity(), product.getMinStockLevel()));
		}

		report.append("=".repeat(80)).append("\n");
		report.append(String.format("Total %d products need attention!\n", lowStockProducts.size()));

		javax.swing.JTextArea textArea = new javax.swing.JTextArea(report.toString());
		textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
		javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
		scrollPane.setPreferredSize(new java.awt.Dimension(600, 400));
		JOptionPane.showMessageDialog(null, scrollPane, "Low Stock Report", JOptionPane.WARNING_MESSAGE);
	}
}