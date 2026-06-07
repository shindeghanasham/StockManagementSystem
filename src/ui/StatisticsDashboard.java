package ui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class StatisticsDashboard extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StockService stockService;
	private AuthService authService;
	private String userRole;
	private Timer refreshTimer;

	// Dashboard Components
	private JLabel totalProductsLabel, totalStockLabel, inventoryValueLabel;
	private JLabel todaySalesLabel, todayTransactionsLabel, avgTransactionLabel;
	private JLabel lowStockLabel, outOfStockLabel, overStockLabel;
	private JLabel totalUsersLabel, totalCategoriesLabel, totalSuppliersLabel;
	private JTable topProductsTable, recentActivitiesTable;
	private JProgressBar stockHealthBar;
	private JComboBox<String> periodCombo;
	private JPanel chartPanel;

	// Colors
	private final Color PRIMARY_COLOR = new Color(52, 152, 219);
	private final Color SUCCESS_COLOR = new Color(46, 204, 113);
	private final Color WARNING_COLOR = new Color(241, 196, 15);
	private final Color DANGER_COLOR = new Color(231, 76, 60);
	private final Color INFO_COLOR = new Color(155, 89, 182);

	public StatisticsDashboard(JFrame parent, StockService stockService, AuthService authService) {
		super(parent, "Statistics Dashboard", true);
		this.stockService = stockService;
		this.authService = authService;
		this.userRole = authService.getCurrentUser().getRole();

		initializeUI();
		loadStatistics();
		startAutoRefresh();

		setSize(1200, 800);
		setLocationRelativeTo(parent);
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		// Header Panel
		JPanel headerPanel = createHeaderPanel();

		// Main Content Panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// KPI Cards Panel
		JPanel kpiPanel = createKPIPanel();

		// Charts and Analytics Panel
		JPanel analyticsPanel = createAnalyticsPanel();

		// Tables Panel
		JSplitPane tablesPanel = createTablesPanel();

		// Split pane for analytics and tables
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, analyticsPanel, tablesPanel);
		splitPane.setResizeWeight(0.5);

		mainPanel.add(kpiPanel, BorderLayout.NORTH);
		mainPanel.add(splitPane, BorderLayout.CENTER);

		add(headerPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);

		// Footer
		JPanel footerPanel = createFooterPanel();
		add(footerPanel, BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(PRIMARY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		JLabel titleLabel = new JLabel("Statistics Dashboard");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(Color.WHITE);

		JLabel userLabel = new JLabel("Welcome, " + authService.getCurrentUser().getFullName() + " (" + userRole + ")");
		userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		userLabel.setForeground(Color.WHITE);

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightPanel.setOpaque(false);

		periodCombo = new JComboBox<>(new String[] { "Today", "This Week", "This Month", "This Year" });
		periodCombo.addActionListener(e -> loadStatistics());
		rightPanel.add(new JLabel("Period: "));
		rightPanel.add(periodCombo);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addActionListener(e -> loadStatistics());
		rightPanel.add(refreshBtn);

		panel.add(titleLabel, BorderLayout.WEST);
		panel.add(userLabel, BorderLayout.CENTER);
		panel.add(rightPanel, BorderLayout.EAST);

		return panel;
	}

	private JPanel createKPIPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 4, 15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Row 1 - Product Stats
		totalProductsLabel = createKPICard(panel, "Total Products", "0", SUCCESS_COLOR);
		totalStockLabel = createKPICard(panel, "Total Stock Units", "0", PRIMARY_COLOR);
		inventoryValueLabel = createKPICard(panel, "Inventory Value", "₹0", INFO_COLOR);
		stockHealthBar = createHealthCard(panel, "Stock Health", 100);

		// Row 2 - Sales Stats
		todaySalesLabel = createKPICard(panel, "Today's Sales", "₹0", SUCCESS_COLOR);
		todayTransactionsLabel = createKPICard(panel, "Today's Transactions", "0", PRIMARY_COLOR);
		avgTransactionLabel = createKPICard(panel, "Avg Transaction", "₹0", INFO_COLOR);

		// Row 3 - Alert Stats (Admin only)
		lowStockLabel = createKPICard(panel, "Low Stock Alert", "0", WARNING_COLOR);
		outOfStockLabel = createKPICard(panel, "Out of Stock", "0", DANGER_COLOR);
		overStockLabel = createKPICard(panel, "Over Stock", "0", INFO_COLOR);

		// Row 4 - System Stats (Admin only)
		if (userRole.equals("ADMIN")) {
			totalUsersLabel = createKPICard(panel, "Total Users", "0", PRIMARY_COLOR);
			totalCategoriesLabel = createKPICard(panel, "Categories", "0", SUCCESS_COLOR);
			totalSuppliersLabel = createKPICard(panel, "Suppliers", "0", INFO_COLOR);
		} else {
			// For normal users, show different stats
			createKPICard(panel, "Top Selling Category", "-", SUCCESS_COLOR);
			createKPICard(panel, "Best Selling Brand", "-", PRIMARY_COLOR);
			createKPICard(panel, "Most Active Hour", "-", INFO_COLOR);
		}

		return panel;
	}

	private JLabel createKPICard(JPanel parent, String title, String value, Color color) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 2),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		card.setBackground(Color.WHITE);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
		titleLabel.setForeground(Color.GRAY);

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
		valueLabel.setForeground(color);
		valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(valueLabel, BorderLayout.CENTER);

		parent.add(card);
		return valueLabel;
	}

	private JProgressBar createHealthCard(JPanel parent, String title, int health) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		card.setBackground(Color.WHITE);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
		titleLabel.setForeground(Color.GRAY);

		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(health);
		progressBar.setStringPainted(true);
		progressBar.setForeground(SUCCESS_COLOR);

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(progressBar, BorderLayout.CENTER);

		parent.add(card);
		return progressBar;
	}

	private JPanel createAnalyticsPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
		panel.setBorder(BorderFactory.createTitledBorder("Sales Analytics"));

		// Sales Chart Panel (Simulated with text for now)
		JPanel salesChartPanel = new JPanel(new BorderLayout());
		salesChartPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		salesChartPanel.setBackground(Color.WHITE);

		JLabel chartTitle = new JLabel("Sales Trend", SwingConstants.CENTER);
		chartTitle.setFont(new Font("Arial", Font.BOLD, 14));
		salesChartPanel.add(chartTitle, BorderLayout.NORTH);

		chartPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSalesChart(g);
			}
		};
		chartPanel.setBackground(Color.WHITE);
		chartPanel.setPreferredSize(new Dimension(400, 200));
		salesChartPanel.add(chartPanel, BorderLayout.CENTER);

		// Category Distribution Panel
		JPanel categoryPanel = new JPanel(new BorderLayout());
		categoryPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		categoryPanel.setBackground(Color.WHITE);

		JLabel categoryTitle = new JLabel("Category Distribution", SwingConstants.CENTER);
		categoryTitle.setFont(new Font("Arial", Font.BOLD, 14));
		categoryPanel.add(categoryTitle, BorderLayout.NORTH);

		JPanel categoryListPanel = new JPanel();
		categoryListPanel.setLayout(new BoxLayout(categoryListPanel, BoxLayout.Y_AXIS));
		JScrollPane categoryScroll = new JScrollPane(categoryListPanel);
		categoryPanel.add(categoryScroll, BorderLayout.CENTER);

		// Load category distribution
		Map<String, Integer> categorySales = getCategorySales();
		for (Map.Entry<String, Integer> entry : categorySales.entrySet()) {
			JPanel catItem = new JPanel(new BorderLayout());
			catItem.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			JLabel catName = new JLabel(entry.getKey());
			JLabel catValue = new JLabel(entry.getValue() + " units");
			catItem.add(catName, BorderLayout.WEST);
			catItem.add(catValue, BorderLayout.EAST);
			categoryListPanel.add(catItem);
		}

		panel.add(salesChartPanel);
		panel.add(categoryPanel);

		return panel;
	}

	private void drawSalesChart(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int width = chartPanel.getWidth();
		int height = chartPanel.getHeight();

		// Get sales data for chart
		List<Double> salesData = getWeeklySalesData();
		if (salesData.isEmpty())
			return;

		int barWidth = (width - 100) / salesData.size();
		int maxHeight = height - 80;
		double maxSale = salesData.stream().max(Double::compare).orElse(1.0);

		// Draw bars
		for (int i = 0; i < salesData.size(); i++) {
			int barHeight = (int) ((salesData.get(i) / maxSale) * maxHeight);
			int x = 50 + (i * barWidth);
			int y = height - 40 - barHeight;

			// Color based on height
			if (barHeight > maxHeight * 0.7) {
				g2d.setColor(SUCCESS_COLOR);
			} else if (barHeight > maxHeight * 0.3) {
				g2d.setColor(WARNING_COLOR);
			} else {
				g2d.setColor(DANGER_COLOR);
			}

			g2d.fillRect(x, y, barWidth - 5, barHeight);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(x, y, barWidth - 5, barHeight);

			// Draw day labels
			String day = getDayLabel(i);
			g2d.drawString(day, x + 5, height - 20);
		}

		// Draw axes
		g2d.setColor(Color.BLACK);
		g2d.drawLine(40, height - 40, width - 20, height - 40); // X-axis
		g2d.drawLine(40, 20, 40, height - 40); // Y-axis

		// Draw Y-axis labels
		g2d.drawString("₹" + String.format("%.0f", maxSale), 10, 30);
		g2d.drawString("₹0", 10, height - 45);
	}

	private JSplitPane createTablesPanel() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.5);

		// Top Products Table
		JPanel topProductsPanel = new JPanel(new BorderLayout());
		topProductsPanel.setBorder(BorderFactory.createTitledBorder("Top Selling Products"));

		String[] productColumns = { "Product", "Category", "Units Sold", "Revenue", "Profit" };
		topProductsTable = new JTable();
		topProductsTable.setModel(new DefaultTableModel(productColumns, 0));
		JScrollPane productScroll = new JScrollPane(topProductsTable);
		topProductsPanel.add(productScroll, BorderLayout.CENTER);

		// Recent Activities Table
		JPanel recentPanel = new JPanel(new BorderLayout());
		recentPanel.setBorder(BorderFactory.createTitledBorder("Recent Activities"));

		String[] activityColumns = { "Time", "Action", "User", "Details" };
		recentActivitiesTable = new JTable();
		recentActivitiesTable.setModel(new DefaultTableModel(activityColumns, 0));
		JScrollPane activityScroll = new JScrollPane(recentActivitiesTable);
		recentPanel.add(activityScroll, BorderLayout.CENTER);

		splitPane.setLeftComponent(topProductsPanel);
		splitPane.setRightComponent(recentPanel);

		return splitPane;
	}

	private JPanel createFooterPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.lightGray);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JLabel footerLabel = new JLabel(
				"Last Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
		footerLabel.setFont(new Font("Arial", Font.ITALIC, 10));
		panel.add(footerLabel);

		return panel;
	}

	private void loadStatistics() {
		// Product Statistics
		Map<String, Object> stats = stockService.getProductStatistics();
		totalProductsLabel.setText(String.valueOf(stats.get("totalProducts")));
		totalStockLabel.setText(String.valueOf(stats.get("totalStock")));
		inventoryValueLabel.setText(String.format("₹%.2f", stats.get("totalValue")));

		int health = calculateStockHealth();
		stockHealthBar.setValue(health);
		stockHealthBar.setString(health + "% Healthy");
		if (health > 70) {
			stockHealthBar.setForeground(SUCCESS_COLOR);
		} else if (health > 40) {
			stockHealthBar.setForeground(WARNING_COLOR);
		} else {
			stockHealthBar.setForeground(DANGER_COLOR);
		}

		// Alert Statistics
		List<Product> lowStock = stockService.getLowStockProducts();
		lowStockLabel.setText(String.valueOf(lowStock.size()));

		long outOfStock = stockService.getAllProducts().stream().filter(p -> p.getQuantity() == 0 && p.isActive())
				.count();
		outOfStockLabel.setText(String.valueOf(outOfStock));

		List<Product> overStock = stockService.getOverStockProducts();
		overStockLabel.setText(String.valueOf(overStock.size()));

		// Sales Statistics
		double todaySales = stockService.getTodaySales();
		todaySalesLabel.setText(String.format("₹%.2f", todaySales));

		int todayTransactions = stockService.getTodayTransactions();
		todayTransactionsLabel.setText(String.valueOf(todayTransactions));

		double avgTransaction = todayTransactions > 0 ? todaySales / todayTransactions : 0;
		avgTransactionLabel.setText(String.format("₹%.2f", avgTransaction));

		// System Statistics (Admin only)
		if (userRole.equals("ADMIN")) {
			totalUsersLabel.setText(String.valueOf(authService.getAllUsers().size()));
			totalCategoriesLabel.setText(String.valueOf(stockService.getAllCategories().size()));

			long uniqueSuppliers = stockService.getAllProducts().stream().map(Product::getSupplier)
					.filter(s -> s != null && !s.isEmpty()).distinct().count();
			totalSuppliersLabel.setText(String.valueOf(uniqueSuppliers));
		}

		// Load Top Products
		loadTopProducts();

		// Load Recent Activities
		loadRecentActivities();

		// Refresh chart
		chartPanel.repaint();

		// Update footer
		updateFooter();
	}

	private int calculateStockHealth() {
		List<Product> products = stockService.getAllProducts();
		if (products.isEmpty())
			return 100;

		int healthyCount = 0;
		for (Product p : products) {
			if (!p.isLowStock() && !p.isOverStock() && p.getQuantity() > 0) {
				healthyCount++;
			}
		}

		return (healthyCount * 100) / products.size();
	}

	private void loadTopProducts() {
		DefaultTableModel model = (DefaultTableModel) topProductsTable.getModel();
		model.setRowCount(0);

		// Get sales data from bills
		List<Bill> bills = stockService.getBills();
		Map<String, Integer> productSales = new HashMap<>();
		Map<String, Double> productRevenue = new HashMap<>();

		for (Bill bill : bills) {
			for (CartItem item : bill.getItems()) {
				String productName = item.getProduct().getName();
				int qty = item.getQuantity();
				double revenue = item.getTotal();

				productSales.put(productName, productSales.getOrDefault(productName, 0) + qty);
				productRevenue.put(productName, productRevenue.getOrDefault(productName, 0.0) + revenue);
			}
		}

		// Sort by sales and get top 10
		List<Map.Entry<String, Integer>> sorted = new ArrayList<>(productSales.entrySet());
		sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

		for (int i = 0; i < Math.min(10, sorted.size()); i++) {
			String productName = sorted.get(i).getKey();
			int sold = sorted.get(i).getValue();
			double revenue = productRevenue.getOrDefault(productName, 0.0);
			double profit = revenue * 0.15; // Approximate profit (15% margin)

			// Find product category
			Product product = stockService.getAllProducts().stream().filter(p -> p.getName().equals(productName))
					.findFirst().orElse(null);
			String category = product != null ? stockService.getCategoryName(product.getCategoryId()) : "Unknown";

			model.addRow(new Object[] { productName, category, sold, String.format("₹%.2f", revenue),
					String.format("₹%.2f", profit) });
		}
	}

	private void loadRecentActivities() {
		DefaultTableModel model = (DefaultTableModel) recentActivitiesTable.getModel();
		model.setRowCount(0);

		List<String> auditLogs = stockService.getAuditService().getAuditLog();

		for (int i = Math.max(0, auditLogs.size() - 20); i < auditLogs.size(); i++) {
			String log = auditLogs.get(i);
			// Parse log format: [2024-06-03 10:30:00] ACTION | User: name | Product:
			// product | Qty: 5 | Price: ₹100
			try {
				String[] parts = log.split(" \\| ");
				String timestamp = parts[0].replace("[", "").replace("]", "");
				String action = parts[1];
				String user = parts[2].replace("User: ", "");
				String details = parts[3].replace("Product: ", "") + " " + (parts.length > 4 ? parts[4] : "");

				model.addRow(new Object[] { timestamp, action, user, details });
			} catch (Exception e) {
				model.addRow(new Object[] { "-", log, "-", "-" });
			}
		}
	}

	private Map<String, Integer> getCategorySales() {
		Map<String, Integer> categorySales = new HashMap<>();
		List<Bill> bills = stockService.getBills();

		for (Bill bill : bills) {
			for (CartItem item : bill.getItems()) {
				String category = stockService.getCategoryName(item.getProduct().getCategoryId());
				categorySales.put(category, categorySales.getOrDefault(category, 0) + item.getQuantity());
			}
		}

		return categorySales;
	}

	private List<Double> getWeeklySalesData() {
		List<Double> sales = new ArrayList<>();
		LocalDate today = LocalDate.now();

		for (int i = 6; i >= 0; i--) {
			LocalDate date = today.minusDays(i);
			double daySales = stockService.getBillsByDate(date).stream().mapToDouble(Bill::getGrandTotal).sum();
			sales.add(daySales);
		}

		return sales;
	}

	private String getDayLabel(int index) {
		LocalDate date = LocalDate.now().minusDays(6 - index);
		return date.format(DateTimeFormatter.ofPattern("dd/MM"));
	}

	private void updateFooter() {
		JPanel footer = (JPanel) ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
		if (footer != null) {
			JLabel footerLabel = (JLabel) footer.getComponent(0);
			footerLabel.setText(
					"Last Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
		}
	}

	private void startAutoRefresh() {
		refreshTimer = new Timer(30000, e -> {
		    SwingUtilities.invokeLater(() -> loadStatistics());
		});
		refreshTimer.start();
	}

	@Override
	public void dispose() {
		if (refreshTimer != null) {
			refreshTimer.stop();
		}
		super.dispose();
	}
}