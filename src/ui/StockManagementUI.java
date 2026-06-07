package ui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockManagementUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StockService stockService;
	private AuthService authService;
	private ReportService reportService;
	private JTable productTable;
	private DefaultTableModel tableModel;
	private JTextField searchField;
	private JLabel statusLabel;

	public void setCurrentUser(String username) {
		stockService.setCurrentUser(username);
	}

	private final String[] COLUMNS = { "ID", "Name", "Category", "Quantity", "Min Stock", "Sell Price (₹)",
			"Total Value (₹)" };

	public StockManagementUI(AuthService authService) {
		this.authService = authService;
		this.stockService = new StockService();
		this.reportService = new ReportService(stockService);
		initializeUI();
		loadProductsToTable();
		checkLowStock();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initializeUI() {
		setTitle("Stock Management System - " + authService.getCurrentUser().getFullName() + " ("
				+ authService.getCurrentUser().getRole() + ")");
		setSize(1200, 800);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// Menu Bar
		setJMenuBar(createMenuBar());

		// Top Panel
		JPanel topPanel = createTopPanel();

		// Center Panel
		JPanel centerPanel = createCenterPanel();

		// Bottom Panel
		JPanel bottomPanel = createBottomPanel();

		// Status Bar
		statusLabel = new JLabel("Ready");
		statusLabel.setBorder(BorderFactory.createEtchedBorder());

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(bottomPanel, BorderLayout.CENTER);
		southPanel.add(statusLabel, BorderLayout.SOUTH);

		add(topPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		// File Menu
		JMenu fileMenu = new JMenu("File");
		JMenuItem exportExcelItem = new JMenuItem("Export to CSV");
		JMenuItem logoutItem = new JMenuItem("Logout");
		JMenuItem exitItem = new JMenuItem("Exit");

		exportExcelItem.addActionListener(e -> exportToCsv());
		logoutItem.addActionListener(e -> logout());
		exitItem.addActionListener(e -> System.exit(0));

		fileMenu.add(exportExcelItem);
		fileMenu.addSeparator();
		fileMenu.add(logoutItem);
		fileMenu.add(exitItem);

		// Reports Menu
		JMenu reportsMenu = new JMenu("Reports");
		JMenuItem salesReportItem = new JMenuItem("Sales Report");
		JMenuItem lowStockItem = new JMenuItem("Low Stock Report");
		JMenuItem auditItem = new JMenuItem("Audit Report");

		salesReportItem.addActionListener(e -> reportService.generateSalesReport());
		lowStockItem.addActionListener(e -> reportService.generateLowStockReport());
		auditItem.addActionListener(e -> showAuditReport());

		reportsMenu.add(salesReportItem);
		reportsMenu.add(lowStockItem);
		reportsMenu.add(auditItem);

		// Admin Menu (only for admin users)
		JMenu adminMenu = new JMenu("Admin");
		JMenuItem manageUsersItem = new JMenuItem("Manage Users");
		JMenuItem manageCategoriesItem = new JMenuItem("Manage Categories");

		manageUsersItem.addActionListener(e -> showUserManagementDialog());
		manageCategoriesItem.addActionListener(e -> showCategoryManagementDialog());

		adminMenu.add(manageUsersItem);
		adminMenu.add(manageCategoriesItem);

		menuBar.add(fileMenu);
		menuBar.add(reportsMenu);
		if (authService.isAdmin()) {
			menuBar.add(adminMenu);
		}

		return menuBar;
	}

	private JPanel createTopPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setBackground(new Color(70, 130, 180));

		JLabel titleLabel = new JLabel("Stock Management System", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(Color.WHITE);

		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		searchPanel.setBackground(new Color(70, 130, 180));
		searchPanel.add(new JLabel("Search:"));
		searchField = new JTextField(20);
		JButton searchBtn = new JButton("Search");
		searchBtn.addActionListener(e -> searchProducts());
		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addActionListener(e -> loadProductsToTable());

		searchPanel.add(searchField);
		searchPanel.add(searchBtn);
		searchPanel.add(refreshBtn);

		panel.add(titleLabel, BorderLayout.CENTER);
		panel.add(searchPanel, BorderLayout.EAST);

		return panel;
	}

	private JPanel createCenterPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		tableModel = new DefaultTableModel(COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		productTable = new JTable(tableModel);
		productTable.setRowHeight(25);
		productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
		productTable.setFont(new Font("Arial", Font.PLAIN, 12));

		// Color rows based on stock level
		productTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				try {
					int qty = (int) tableModel.getValueAt(row, 3);
					int minStock = (int) tableModel.getValueAt(row, 4);
					if (!isSelected) {
						if (qty <= minStock) {
							c.setBackground(new Color(255, 200, 200));
						} else {
							c.setBackground(Color.WHITE);
						}
					}
				} catch (Exception ex) {
					c.setBackground(Color.WHITE);
				}
				return c;
			}
		});

		JScrollPane scrollPane = new JScrollPane(productTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createBottomPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 9, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setBackground(new Color(240, 240, 240));

		JButton addBtn = createStyledButton("Add Product", new Color(46, 204, 113));
		JButton editBtn = createStyledButton("Edit Product", new Color(52, 152, 219));
		JButton viewBtn = createStyledButton("View Details", new Color(155, 89, 182));
		JButton deleteBtn = createStyledButton("Delete Product", new Color(231, 76, 60));
		JButton updateStockBtn = createStyledButton("Update Stock", new Color(241, 196, 15));
		JButton sellBtn = createStyledButton("Sell Product", new Color(230, 126, 34));
		JButton transferBtn = createStyledButton("Transfer Stock", new Color(26, 188, 156));
		JButton bulkOpsBtn = createStyledButton("Bulk Operations", new Color(149, 165, 166));
		JButton statsBtn = createStyledButton("Statistics", new Color(155, 89, 182));

		JButton lowStockBtn = createStyledButton("Low Stock Alert", new Color(230, 126, 34));
		JButton invoiceBtn = createStyledButton("Generate Invoice", new Color(155, 89, 182));
		JButton billingBtn = createStyledButton("POS Billing", new Color(46, 204, 113));
		JButton dashboardBtn = createStyledButton("Dashboard", new Color(52, 152, 219));

		// 🔥 NEW FIXED BUTTON (THIS WAS MISSING)
		JButton viewInvoicesBtn = createStyledButton("View Invoices", new Color(142, 68, 173));

		JButton adminBtn = createStyledButton("Admin Panel", new Color(231, 76, 60));
		JButton auditBtn = createStyledButton("Audit Report", new Color(142, 68, 173));
		JButton refreshBtn = createStyledButton("Refresh", new Color(52, 73, 94));
		JButton bulkImportBtn = createStyledButton("Bulk Import", new Color(46, 204, 113));
		bulkImportBtn.addActionListener(e -> openBulkImportDialog());
		panel.add(bulkImportBtn);

		// ACTIONS
		addBtn.addActionListener(e -> showAddProductDialogEnhanced());
		editBtn.addActionListener(e -> showEditProductDialog());
		viewBtn.addActionListener(e -> showProductDetailsDialog());
		deleteBtn.addActionListener(e -> deleteProduct());
		updateStockBtn.addActionListener(e -> showUpdateStockDialog());
		sellBtn.addActionListener(e -> showSellProductDialog());
		transferBtn.addActionListener(e -> showStockTransferDialog());
		bulkOpsBtn.addActionListener(e -> showBulkOperationsDialog());
		statsBtn.addActionListener(e -> showProductStatistics());

		lowStockBtn.addActionListener(e -> reportService.generateLowStockReport());
		invoiceBtn.addActionListener(e -> showInvoiceDialog());
		billingBtn.addActionListener(e -> openBillingSystem());
		dashboardBtn.addActionListener(e -> openStatisticsDashboard());

		// 🔥 FIXED INVOICE VIEWER ACTION
		viewInvoicesBtn.addActionListener(e -> openInvoiceViewer());

		adminBtn.addActionListener(e -> openAdminDashboard());
		auditBtn.addActionListener(e -> showAuditReport());
		refreshBtn.addActionListener(e -> loadProductsToTable());

		// ROW 1
		panel.add(addBtn);
		panel.add(editBtn);
		panel.add(viewBtn);
		panel.add(deleteBtn);
		panel.add(updateStockBtn);
		panel.add(sellBtn);
		panel.add(transferBtn);
		panel.add(bulkOpsBtn);
		panel.add(statsBtn);

		// ROW 2
		panel.add(lowStockBtn);
		panel.add(invoiceBtn);
		panel.add(billingBtn);
		panel.add(dashboardBtn);

		// 🔥 IMPORTANT: ADDED HERE
		panel.add(viewInvoicesBtn);

		if (authService.isAdmin()) {
			panel.add(adminBtn);
		} else {
			panel.add(new JPanel());
		}

		panel.add(auditBtn);
		panel.add(refreshBtn);

		return panel;
	}

	// Add these methods to the StockManagementUI class

	private void showProductDetailsDialog() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product to view!");
			return;
		}

		String productId = (String) tableModel.getValueAt(selectedRow, 0);
		Product product = stockService.getProduct(productId);

		if (product == null)
			return;

		JDialog dialog = new JDialog(this, "Product Details", true);
		dialog.setLayout(new BorderLayout());
		dialog.setSize(500, 600);
		dialog.setLocationRelativeTo(this);

		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Add all product details
		addDetailRow(infoPanel, gbc, 0, "Product ID:", product.getId());
		addDetailRow(infoPanel, gbc, 1, "Barcode:", product.getBarcode());
		addDetailRow(infoPanel, gbc, 2, "Name:", product.getName());
		addDetailRow(infoPanel, gbc, 3, "Category:", stockService.getCategoryName(product.getCategoryId()));
		addDetailRow(infoPanel, gbc, 4, "Brand:", product.getBrand());
		addDetailRow(infoPanel, gbc, 5, "Supplier:", product.getSupplier());
		addDetailRow(infoPanel, gbc, 6, "Location:", product.getLocation());
		addDetailRow(infoPanel, gbc, 7, "Unit:", product.getUnit());
		addDetailRow(infoPanel, gbc, 8, "Quantity:", String.valueOf(product.getQuantity()));
		addDetailRow(infoPanel, gbc, 9, "Min Stock:", String.valueOf(product.getMinStockLevel()));
		addDetailRow(infoPanel, gbc, 10, "Max Stock:", String.valueOf(product.getMaxStockLevel()));
		addDetailRow(infoPanel, gbc, 11, "Cost Price:", String.format("₹%.2f", product.getCostPrice()));
		addDetailRow(infoPanel, gbc, 12, "Sell Price:", String.format("₹%.2f", product.getSellPrice()));
		addDetailRow(infoPanel, gbc, 13, "MRP:", String.format("₹%.2f", product.getMrp()));
		addDetailRow(infoPanel, gbc, 14, "Weight:", product.getWeight() + " kg");
		addDetailRow(infoPanel, gbc, 15, "Total Value:", String.format("₹%.2f", product.getTotalValue()));
		addDetailRow(infoPanel, gbc, 16, "Profit:", String.format("₹%.2f", product.getProfit()));
		addDetailRow(infoPanel, gbc, 17, "Profit Margin:", String.format("%.2f%%", product.getProfitMargin()));
		addDetailRow(infoPanel, gbc, 18, "Added Date:", product.getFormattedAddedDate());
		addDetailRow(infoPanel, gbc, 19, "Last Updated:", product.getFormattedLastUpdated());
		addDetailRow(infoPanel, gbc, 20, "Added By:", product.getAddedBy());
		addDetailRow(infoPanel, gbc, 21, "Status:", product.isActive() ? "Active" : "Inactive");

		JScrollPane scrollPane = new JScrollPane(infoPanel);
		dialog.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton closeBtn = new JButton("Close");
		closeBtn.addActionListener(e -> dialog.dispose());
		buttonPanel.add(closeBtn);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		dialog.setVisible(true);
	}

	private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
		gbc.gridx = 0;
		gbc.gridy = row;
		JLabel labelComp = new JLabel(label);
		labelComp.setFont(new Font("Arial", Font.BOLD, 12));
		panel.add(labelComp, gbc);

		gbc.gridx = 1;
		JLabel valueComp = new JLabel(value);
		valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
		panel.add(valueComp, gbc);
	}

	private void addDashboardButtons() {
		JButton statsDashboardBtn = createStyledButton("Statistics", new Color(155, 89, 182));
		statsDashboardBtn.addActionListener(e -> openStatisticsDashboard());

		JPanel bottomPanel = (JPanel) ((BorderLayout) getContentPane().getLayout())
				.getLayoutComponent(BorderLayout.SOUTH);
		if (bottomPanel instanceof JPanel) {
			// Add to existing buttons or create new row
			bottomPanel.add(statsDashboardBtn);
		}
	}

	// Add these methods
	private void openStatisticsDashboard() {
		StatisticsDashboard dashboard = new StatisticsDashboard(this, stockService, authService);
		dashboard.setVisible(true);
	}

	private void openAdminDashboard() {
		if (authService.isAdmin()) {
			AdminDashboard adminDashboard = new AdminDashboard(this, stockService, authService);
			adminDashboard.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "Access denied! Admin only.");
		}
	}
	
	// Add this method
	private void openBulkImportDialog() {
	    if (!authService.isAdmin()) {
	        JOptionPane.showMessageDialog(this, "Access denied! Admin only.");
	        return;
	    }
	    BulkImportDialog dialog = new BulkImportDialog(this, stockService, authService);
	    dialog.setVisible(true);
	}

	// Add this button to the bottom panel
	private void addInvoiceViewerButton() {
		JButton viewInvoicesBtn = createStyledButton("View Invoices", new Color(155, 89, 182));
		viewInvoicesBtn.addActionListener(e -> openInvoiceViewer());

		// Add to existing panel
		JPanel bottomPanel = (JPanel) ((BorderLayout) getContentPane().getLayout())
				.getLayoutComponent(BorderLayout.SOUTH);
		if (bottomPanel instanceof JPanel) {
			bottomPanel.add(viewInvoicesBtn);
		}
	}

	private void openInvoiceViewer() {
		InvoiceViewerUI viewer = new InvoiceViewerUI(this, stockService, authService);
		viewer.setVisible(true);
	}

	private void showEditProductDialog() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product to edit!");
			return;
		}

		String productId = (String) tableModel.getValueAt(selectedRow, 0);
		Product product = stockService.getProduct(productId);

		if (product == null)
			return;

		ProductCRUDDialog dialog = new ProductCRUDDialog(this, stockService, authService, product, true,
				this::loadProductsToTable);
		dialog.setVisible(true);
	}

	private void showAddProductDialogEnhanced() {
		ProductCRUDDialog dialog = new ProductCRUDDialog(this, stockService, authService, null, false,
				this::loadProductsToTable);
		dialog.setVisible(true);
	}

	private void showBulkOperationsDialog() {
		JDialog dialog = new JDialog(this, "Bulk Operations", true);
		dialog.setLayout(new BorderLayout());
		dialog.setSize(500, 400);
		dialog.setLocationRelativeTo(this);

		JTabbedPane tabbedPane = new JTabbedPane();

		// Bulk Price Update Tab
		JPanel pricePanel = createBulkPricePanel();
		tabbedPane.addTab("Bulk Price Update", pricePanel);

		// Bulk Stock Update Tab
		JPanel stockPanel = createBulkStockPanel();
		tabbedPane.addTab("Bulk Stock Update", stockPanel);

		// Bulk Delete Tab
		JPanel deletePanel = createBulkDeletePanel();
		tabbedPane.addTab("Bulk Delete", deletePanel);

		dialog.add(tabbedPane, BorderLayout.CENTER);

		JButton closeBtn = new JButton("Close");
		closeBtn.addActionListener(e -> dialog.dispose());
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(closeBtn);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		dialog.setVisible(true);
	}

	private JPanel createBulkPricePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea textArea = new JTextArea(15, 40);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setText("Format: ProductID,NewPrice\nExample:\nPROD001,1500\nPROD002,2500");
		JScrollPane scrollPane = new JScrollPane(textArea);

		JButton updateBtn = new JButton("Update Prices");
		updateBtn.addActionListener(e -> {
			try {
				Map<String, Double> priceUpdates = new HashMap<>();
				String[] lines = textArea.getText().split("\n");
				for (String line : lines) {
					if (line.trim().isEmpty() || line.startsWith("Format") || line.startsWith("Example"))
						continue;
					String[] parts = line.split(",");
					if (parts.length == 2) {
						priceUpdates.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
					}
				}

				if (stockService.bulkUpdatePrices(priceUpdates)) {
					JOptionPane.showMessageDialog(panel, "Prices updated successfully!");
					loadProductsToTable();
				} else {
					JOptionPane.showMessageDialog(panel, "Failed to update prices!");
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
			}
		});

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(updateBtn, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createBulkStockPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea textArea = new JTextArea(15, 40);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setText("Format: ProductID,QuantityToAdd\nExample:\nPROD001,100\nPROD002,50");
		JScrollPane scrollPane = new JScrollPane(textArea);

		JButton updateBtn = new JButton("Add Stock");
		updateBtn.addActionListener(e -> {
			try {
				Map<String, Integer> stockUpdates = new HashMap<>();
				String[] lines = textArea.getText().split("\n");
				for (String line : lines) {
					if (line.trim().isEmpty() || line.startsWith("Format") || line.startsWith("Example"))
						continue;
					String[] parts = line.split(",");
					if (parts.length == 2) {
						stockUpdates.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
					}
				}

				if (stockService.bulkAddStock(stockUpdates)) {
					JOptionPane.showMessageDialog(panel, "Stock added successfully!");
					loadProductsToTable();
				} else {
					JOptionPane.showMessageDialog(panel, "Failed to add stock!");
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
			}
		});

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(updateBtn, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createBulkDeletePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JList<String> productList = new JList<>();
		DefaultListModel<String> listModel = new DefaultListModel<>();

		for (Product product : stockService.getAllProducts()) {
			if (product.isActive()) {
				listModel.addElement(product.getId() + " - " + product.getName());
			}
		}
		productList.setModel(listModel);
		productList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(productList);

		JButton deleteBtn = new JButton("Delete Selected");
		deleteBtn.setBackground(new Color(231, 76, 60));
		deleteBtn.setForeground(Color.WHITE);
		deleteBtn.addActionListener(e -> {
			List<String> selectedIds = new ArrayList<>();
			for (String selected : productList.getSelectedValuesList()) {
				selectedIds.add(selected.split(" - ")[0]);
			}

			int confirm = JOptionPane.showConfirmDialog(panel, "Delete " + selectedIds.size() + " products?",
					"Confirm Bulk Delete", JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				if (stockService.bulkDeleteProducts(selectedIds)) {
					JOptionPane.showMessageDialog(panel, "Products deleted successfully!");
					loadProductsToTable();
				}
			}
		});

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(deleteBtn, BorderLayout.SOUTH);

		return panel;
	}

	private void showStockTransferDialog() {
		JDialog dialog = new JDialog(this, "Transfer Stock", true);
		dialog.setLayout(new GridBagLayout());
		dialog.setSize(500, 400);
		dialog.setLocationRelativeTo(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// From Product
		gbc.gridx = 0;
		gbc.gridy = 0;
		dialog.add(new JLabel("From Product:"), gbc);
		gbc.gridx = 1;
		JComboBox<String> fromProductCombo = new JComboBox<>();
		for (Product p : stockService.getAllProducts()) {
			if (p.isActive()) {
				fromProductCombo.addItem(p.getId() + " - " + p.getName() + " (Stock: " + p.getQuantity() + ")");
			}
		}
		dialog.add(fromProductCombo, gbc);

		// To Product
		gbc.gridx = 0;
		gbc.gridy = 1;
		dialog.add(new JLabel("To Product:"), gbc);
		gbc.gridx = 1;
		JComboBox<String> toProductCombo = new JComboBox<>();
		for (Product p : stockService.getAllProducts()) {
			if (p.isActive()) {
				toProductCombo.addItem(p.getId() + " - " + p.getName());
			}
		}
		dialog.add(toProductCombo, gbc);

		// Quantity
		gbc.gridx = 0;
		gbc.gridy = 2;
		dialog.add(new JLabel("Quantity:"), gbc);
		gbc.gridx = 1;
		JTextField quantityField = new JTextField(15);
		dialog.add(quantityField, gbc);

		// Buttons
		JPanel buttonPanel = new JPanel();
		JButton transferBtn = new JButton("Transfer");
		JButton cancelBtn = new JButton("Cancel");

		transferBtn.addActionListener(e -> {
			try {
				String fromProductId = ((String) fromProductCombo.getSelectedItem()).split(" - ")[0];
				String toProductId = ((String) toProductCombo.getSelectedItem()).split(" - ")[0];
				int quantity = Integer.parseInt(quantityField.getText().trim());

				if (quantity <= 0) {
					JOptionPane.showMessageDialog(dialog, "Quantity must be positive!");
					return;
				}

				if (stockService.transferStock(fromProductId, toProductId, quantity)) {
					JOptionPane.showMessageDialog(dialog, "Stock transferred successfully!");
					loadProductsToTable();
					dialog.dispose();
				} else {
					JOptionPane.showMessageDialog(dialog, "Transfer failed! Check stock availability.");
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(dialog, "Invalid quantity!");
			}
		});

		cancelBtn.addActionListener(e -> dialog.dispose());

		buttonPanel.add(transferBtn);
		buttonPanel.add(cancelBtn);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		dialog.add(buttonPanel, gbc);

		dialog.setVisible(true);
	}

	private void showProductStatistics() {
		Map<String, Object> stats = stockService.getProductStatistics();

		StringBuilder sb = new StringBuilder();
		sb.append("\n").append("=".repeat(60)).append("\n");
		sb.append(String.format("%35s\n", "PRODUCT STATISTICS"));
		sb.append("=".repeat(60)).append("\n");
		sb.append(String.format("Total Products: %d\n", stats.get("totalProducts")));
		sb.append(String.format("Total Stock Units: %d\n", stats.get("totalStock")));
		sb.append(String.format("Total Inventory Value: ₹%.2f\n", stats.get("totalValue")));
		sb.append(String.format("Total Potential Profit: ₹%.2f\n", stats.get("totalProfit")));
		sb.append(String.format("Low Stock Products: %d\n", stats.get("lowStockCount")));
		sb.append(String.format("Over Stock Products: %d\n", stats.get("overStockCount")));
		sb.append(String.format("Average Profit Margin: %.2f%%\n", stats.get("averageProfitMargin")));
		sb.append("=".repeat(60)).append("\n");

		JTextArea textArea = new JTextArea(sb.toString());
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 300));

		JOptionPane.showMessageDialog(this, scrollPane, "Product Statistics", JOptionPane.INFORMATION_MESSAGE);
	}

	private JButton createStyledButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFont(new Font("Arial", Font.BOLD, 12));
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return button;
	}

	private void showSellProductDialog() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product to sell!");
			return;
		}

		String productId = (String) tableModel.getValueAt(selectedRow, 0);
		Product product = stockService.getProduct(productId);

		if (product == null)
			return;

		String quantityStr = JOptionPane.showInputDialog(this,
				"Product: " + product.getName() + "\nAvailable quantity: " + product.getQuantity() + "\nSell price: ₹"
						+ product.getSellPrice() + "\nMin Stock Level: " + product.getMinStockLevel()
						+ "\n\nEnter quantity to sell:",
				"Sell Product", JOptionPane.QUESTION_MESSAGE);

		if (quantityStr != null) {
			try {
				int quantity = Integer.parseInt(quantityStr);
				if (quantity <= 0) {
					JOptionPane.showMessageDialog(this, "Quantity must be positive!");
					return;
				}

				if (quantity > product.getQuantity()) {
					JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + product.getQuantity());
					return;
				}

				double total = quantity * product.getSellPrice();
				double gst = total * 0.18;
				double grandTotal = total + gst;

				int confirm = JOptionPane.showConfirmDialog(this, String.format(
						"Sell %d units of %s\nSubtotal: ₹%.2f\nGST (18%%): ₹%.2f\nGrand Total: ₹%.2f\n\nConfirm sale?",
						quantity, product.getName(), total, gst, grandTotal), "Confirm Sale",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					if (stockService.sellProduct(productId, quantity, authService.getCurrentUser().getUsername())) {
						JOptionPane.showMessageDialog(this, "Sale completed successfully!");
						loadProductsToTable();
						showInvoice(product, quantity, total, gst, grandTotal);
						checkLowStock();
					}
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number!");
			}
		}
	}

	private void showInvoice(Product product, int quantity, double subtotal, double gst, double grandTotal) {
		StringBuilder invoice = new StringBuilder();
		invoice.append("\n").append("=".repeat(60)).append("\n");
		invoice.append(String.format("%40s\n", "TAX INVOICE"));
		invoice.append("=".repeat(60)).append("\n");
		invoice.append("ABC STOCK MANAGEMENT SYSTEMS\n");
		invoice.append("123 Business Park, Mumbai - 400001\n");
		invoice.append("GSTIN: 27AAAAA1234A1Z\n");
		invoice.append("-".repeat(60)).append("\n");
		invoice.append(String.format("Date: %s\n", java.time.LocalDate.now()));
		invoice.append(String.format("Time: %s\n",
				java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))));
		invoice.append(String.format("Sold By: %s\n", authService.getCurrentUser().getFullName()));
		invoice.append("-".repeat(60)).append("\n");
		invoice.append(String.format("%-20s %10s %12s %12s\n", "Item", "Qty", "Price", "Total"));
		invoice.append("-".repeat(60)).append("\n");
		invoice.append(String.format("%-20s %10d %12.2f %12.2f\n", product.getName(), quantity, product.getSellPrice(),
				subtotal));
		invoice.append("-".repeat(60)).append("\n");
		invoice.append(String.format("%-44s %12.2f\n", "Subtotal:", subtotal));
		invoice.append(String.format("%-44s %12.2f\n", "GST (18%):", gst));
		invoice.append("=".repeat(60)).append("\n");
		invoice.append(String.format("%-44s %12.2f\n", "GRAND TOTAL (₹):", grandTotal));
		invoice.append("=".repeat(60)).append("\n");
		invoice.append(String.format("%40s\n", "Thank you! Visit again!"));
		invoice.append("=".repeat(60)).append("\n");

		JTextArea textArea = new JTextArea(invoice.toString());
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 500));

		JOptionPane.showMessageDialog(this, scrollPane, "Invoice", JOptionPane.INFORMATION_MESSAGE);
		System.out.println(invoice.toString());
	}

	private void exportToCsv() {
		String timestamp = java.time.LocalDateTime.now()
				.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		String filename = "stock_report_" + timestamp + ".csv";
		reportService.exportToCsv(filename, productTable);
		statusLabel.setText("Exported to " + filename);
	}

	private void logout() {
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			new LoginUI().setVisible(true);
			dispose();
		}
	}

	private void checkLowStock() {
		List<Product> lowStock = stockService.getLowStockProducts();
		if (!lowStock.isEmpty()) {
			statusLabel.setText("⚠️ Warning: " + lowStock.size() + " product(s) are low on stock!");
			statusLabel.setForeground(Color.RED);
		} else {
			statusLabel.setText("All stocks are at adequate levels");
			statusLabel.setForeground(Color.BLACK);
		}
	}

	private void showUserManagementDialog() {
		if (!authService.isAdmin()) {
			JOptionPane.showMessageDialog(this, "Access denied! Admin only.");
			return;
		}

		JDialog dialog = new JDialog(this, "User Management", true);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(this);

		JPanel panel = new JPanel(new BorderLayout());

		String[] columns = { "Username", "Full Name", "Role" };
		DefaultTableModel userModel = new DefaultTableModel(columns, 0);
		JTable userTable = new JTable(userModel);

		// Load users
		authService.getAllUsers().values().forEach(user -> {
			userModel.addRow(new Object[] { user.getUsername(), user.getFullName(), user.getRole() });
		});

		JScrollPane scrollPane = new JScrollPane(userTable);

		JPanel buttonPanel = new JPanel();
		JButton addUserBtn = new JButton("Add User");
		JButton deleteUserBtn = new JButton("Delete User");
		JButton closeBtn = new JButton("Close");

		addUserBtn.addActionListener(e -> showAddUserDialog(dialog, userModel));
		deleteUserBtn.addActionListener(e -> {
			int selectedRow = userTable.getSelectedRow();
			if (selectedRow != -1) {
				String username = (String) userModel.getValueAt(selectedRow, 0);
				if (authService.deleteUser(username)) {
					userModel.removeRow(selectedRow);
					JOptionPane.showMessageDialog(dialog, "User deleted successfully!");
				} else {
					JOptionPane.showMessageDialog(dialog, "Cannot delete admin user!");
				}
			}
		});
		closeBtn.addActionListener(e -> dialog.dispose());

		buttonPanel.add(addUserBtn);
		buttonPanel.add(deleteUserBtn);
		buttonPanel.add(closeBtn);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.add(panel);
		dialog.setVisible(true);
	}

	private void showAddUserDialog(JDialog parent, DefaultTableModel userModel) {
		JDialog dialog = new JDialog(parent, "Add User", true);
		dialog.setLayout(new GridBagLayout());
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(parent);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		JTextField usernameField = new JTextField(15);
		JTextField fullNameField = new JTextField(15);
		JPasswordField passwordField = new JPasswordField(15);
		JComboBox<String> roleCombo = new JComboBox<>(new String[] { "USER", "ADMIN" });

		gbc.gridx = 0;
		gbc.gridy = 0;
		dialog.add(new JLabel("Username:"), gbc);
		gbc.gridx = 1;
		dialog.add(usernameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		dialog.add(new JLabel("Full Name:"), gbc);
		gbc.gridx = 1;
		dialog.add(fullNameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		dialog.add(new JLabel("Password:"), gbc);
		gbc.gridx = 1;
		dialog.add(passwordField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		dialog.add(new JLabel("Role:"), gbc);
		gbc.gridx = 1;
		dialog.add(roleCombo, gbc);

		JButton saveBtn = new JButton("Save");
		JButton cancelBtn = new JButton("Cancel");

		saveBtn.addActionListener(e -> {
			String username = usernameField.getText().trim();
			String fullName = fullNameField.getText().trim();
			String password = new String(passwordField.getPassword());
			String role = (String) roleCombo.getSelectedItem();

			if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
				JOptionPane.showMessageDialog(dialog, "All fields are required!");
				return;
			}

			User user = new User(username, password, role, fullName);
			if (authService.addUser(user)) {
				userModel.addRow(new Object[] { username, fullName, role });
				JOptionPane.showMessageDialog(dialog, "User added successfully!");
				dialog.dispose();
			} else {
				JOptionPane.showMessageDialog(dialog, "Username already exists!");
			}
		});

		cancelBtn.addActionListener(e -> dialog.dispose());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(saveBtn);
		buttonPanel.add(cancelBtn);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		dialog.add(buttonPanel, gbc);

		dialog.setVisible(true);
	}

	private void showCategoryManagementDialog() {
		JDialog dialog = new JDialog(this, "Category Management", true);
		dialog.setSize(500, 400);
		dialog.setLocationRelativeTo(this);

		JPanel panel = new JPanel(new BorderLayout());

		String[] columns = { "ID", "Name", "Description" };
		DefaultTableModel categoryModel = new DefaultTableModel(columns, 0);
		JTable categoryTable = new JTable(categoryModel);

		stockService.getAllCategories().values().forEach(cat -> {
			categoryModel.addRow(new Object[] { cat.getId(), cat.getName(), cat.getDescription() });
		});

		JScrollPane scrollPane = new JScrollPane(categoryTable);

		JPanel buttonPanel = new JPanel();
		JButton addCatBtn = new JButton("Add Category");
		JButton closeBtn = new JButton("Close");

		addCatBtn.addActionListener(e -> {
			String id = JOptionPane.showInputDialog(dialog, "Category ID:");
			String name = JOptionPane.showInputDialog(dialog, "Category Name:");
			String desc = JOptionPane.showInputDialog(dialog, "Description:");

			if (id != null && name != null) {
				Category category = new Category(id, name, desc);
				stockService.addCategory(category);
				categoryModel.addRow(new Object[] { id, name, desc });
			}
		});

		closeBtn.addActionListener(e -> dialog.dispose());

		buttonPanel.add(addCatBtn);
		buttonPanel.add(closeBtn);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.add(panel);
		dialog.setVisible(true);
	}

	private void showInvoiceDialog() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product to generate invoice!");
			return;
		}

		String productId = (String) tableModel.getValueAt(selectedRow, 0);
		Product product = stockService.getProduct(productId);

		if (product == null)
			return;

		String quantityStr = JOptionPane
				.showInputDialog(this,
						"Generate invoice for product: " + product.getName() + "\nSell price: ₹"
								+ product.getSellPrice() + "\n\nEnter quantity:",
						"Generate Invoice", JOptionPane.QUESTION_MESSAGE);

		if (quantityStr != null) {
			try {
				int quantity = Integer.parseInt(quantityStr);
				if (quantity <= 0) {
					JOptionPane.showMessageDialog(this, "Quantity must be positive!");
					return;
				}

				double subtotal = quantity * product.getSellPrice();
				double gst = subtotal * 0.18;
				double grandTotal = subtotal + gst;

				showInvoice(product, quantity, subtotal, gst, grandTotal);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number!");
			}
		}
	}

	private void openBillingSystem() {
		BillingUI billingUI = new BillingUI(this, stockService, authService);
		billingUI.setVisible(true);
		loadProductsToTable(); // Refresh stock after billing
	}

	private void searchProducts() {
		String searchTerm = searchField.getText().trim();
		if (searchTerm.isEmpty()) {
			loadProductsToTable();
			return;
		}

		List<Product> results = stockService.searchProducts(searchTerm);
		updateTableWithProducts(results);

		if (results.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No products found matching: " + searchTerm);
		}
	}

	private void showAuditReport() {
		stockService.getAuditService().displayAuditReport();

		List<String> logs = stockService.getAuditService().getAuditLog();
		if (logs.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No audit records found.");
			return;
		}

		StringBuilder report = new StringBuilder();
		report.append("STOCK AUDIT REPORT\n");
		report.append("=".repeat(80)).append("\n");
		for (String log : logs) {
			report.append(log).append("\n");
		}
		report.append("=".repeat(80));

		JTextArea textArea = new JTextArea(report.toString());
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(800, 500));

		JOptionPane.showMessageDialog(this, scrollPane, "Audit Report", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showUpdateStockDialog() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product to update!");
			return;
		}

		String productId = (String) tableModel.getValueAt(selectedRow, 0);
		Product product = stockService.getProduct(productId);

		if (product == null)
			return;

		String newQuantityStr = JOptionPane.showInputDialog(this,
				"Product: " + product.getName() + "\nCurrent quantity: " + product.getQuantity()
						+ "\nMinimum stock level: " + product.getMinStockLevel() + "\n\nEnter new quantity:",
				"Update Stock", JOptionPane.QUESTION_MESSAGE);

		if (newQuantityStr != null) {
			try {
				int newQuantity = Integer.parseInt(newQuantityStr);
				if (newQuantity < 0) {
					JOptionPane.showMessageDialog(this, "Quantity cannot be negative!");
					return;
				}

				if (stockService.updateQuantity(productId, newQuantity)) {
					JOptionPane.showMessageDialog(this, "Stock updated successfully!");
					loadProductsToTable();
					checkLowStock();
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number!");
			}
		}
	}

	private void deleteProduct() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product to delete!");
			return;
		}

		String productId = (String) tableModel.getValueAt(selectedRow, 0);
		String productName = (String) tableModel.getValueAt(selectedRow, 1);

		int confirm = JOptionPane.showConfirmDialog(this, "Delete product '" + productName + "'?", "Confirm Delete",
				JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			if (stockService.deleteProduct(productId)) {
				JOptionPane.showMessageDialog(this, "Product deleted successfully!");
				loadProductsToTable();
			}
		}
	}

	public void loadProductsToTable() {
		List<Product> products = stockService.getAllProducts();
		updateTableWithProducts(products);
	}

	private void updateTableWithProducts(List<Product> products) {
		tableModel.setRowCount(0);
		for (Product product : products) {
			if (product.isActive()) {
				Object[] row = { product.getId(), product.getName(),
						stockService.getCategoryName(product.getCategoryId()), product.getQuantity(),
						product.getMinStockLevel(), String.format("₹%.2f", product.getSellPrice()),
						String.format("₹%.2f", product.getTotalValue()) };
				tableModel.addRow(row);
			}
		}
	}
}