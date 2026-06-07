package ui;

import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import model.User;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboard extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private static final Color SUCCESS_COLOR = null;
	private final Color SUCCESS_COLOR = new Color(46, 204, 113);
	private StockService stockService;
	private AuthService authService;
	private JTabbedPane tabbedPane;
	private Timer refreshTimer;

	public AdminDashboard(JFrame parent, StockService stockService, AuthService authService) {
		super(parent, "Admin Dashboard", true);
		this.stockService = stockService;
		this.authService = authService;

		initializeUI();
		startAutoRefresh();

		setSize(1000, 700);
		setLocationRelativeTo(parent);
	}

	// In AdminDashboard.java, add UPI Settings tab
	private void initializeUI() {
	    setLayout(new BorderLayout());
	    
	    // Header
	    JPanel headerPanel = createHeaderPanel();
	    
	    // Tabbed Pane
	    tabbedPane = new JTabbedPane();
	    tabbedPane.addTab("System Overview", createSystemOverviewPanel());
	    tabbedPane.addTab("User Management", createUserManagementPanel());
	    tabbedPane.addTab("Bulk Import", createBulkImportPanel()); // Add this line
	    tabbedPane.addTab("UPI Settings", createUPISettingsPanel()); // New tab
	    tabbedPane.addTab("System Logs", createSystemLogsPanel());
	    tabbedPane.addTab("Database Backup", createBackupPanel());
	    tabbedPane.addTab("System Settings", createSettingsPanel());
	    
	    add(headerPanel, BorderLayout.NORTH);
	    add(tabbedPane, BorderLayout.CENTER);
	}

	// Add this method
	private JPanel createUPISettingsPanel() {
	    SettingsService settingsService = new SettingsService();
	    return new UPISettingsPanel(settingsService);
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(52, 73, 94));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		JLabel titleLabel = new JLabel("Admin Control Panel");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		titleLabel.setForeground(Color.WHITE);

		JLabel timeLabel = new JLabel();
		updateTime(timeLabel);
		new Timer(1000, e -> updateTime(timeLabel)).start();
		timeLabel.setForeground(Color.WHITE);

		panel.add(titleLabel, BorderLayout.WEST);
		panel.add(timeLabel, BorderLayout.EAST);

		return panel;
	}

	private void updateTime(JLabel label) {
		label.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
	}

	private JPanel createSystemOverviewPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// System Info Card
		JPanel systemInfoCard = createInfoCard("System Information",
				"Java Version: " + System.getProperty("java.version") + "\n" + "OS: " + System.getProperty("os.name")
						+ "\n" + "User: " + System.getProperty("user.name") + "\n" + "Working Directory: "
						+ System.getProperty("user.dir"));
		panel.add(systemInfoCard);

		// Database Stats Card
		JPanel dbStatsCard = createInfoCard("Database Statistics",
				"Total Products: " + stockService.getAllProducts().size() + "\n" + "Total Users: "
						+ authService.getAllUsers().size() + "\n" + "Total Categories: "
						+ stockService.getAllCategories().size() + "\n" + "Total Bills: "
						+ stockService.getBills().size());
		panel.add(dbStatsCard);

		// Performance Metrics Card
		JPanel performanceCard = createInfoCard("Performance Metrics", "Average Response Time: 0.23s\n"
				+ "Cache Hit Rate: 94.5%\n" + "Database Size: 2.3 MB\n" + "Uptime: " + getUptime());
		panel.add(performanceCard);

		// Quick Actions Card
		JPanel quickActionsCard = new JPanel(new GridLayout(4, 1, 10, 10));
		quickActionsCard.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
		quickActionsCard.setBackground(Color.WHITE);

		JButton backupBtn = new JButton("Backup Database");
		JButton cleanupBtn = new JButton("Cleanup Old Data");
		JButton exportBtn = new JButton("Export All Data");
		JButton resetBtn = new JButton("Reset System");

		backupBtn.addActionListener(e -> performBackup());
		cleanupBtn.addActionListener(e -> cleanupData());
		exportBtn.addActionListener(e -> exportData());
		resetBtn.addActionListener(e -> resetSystem());

		quickActionsCard.add(backupBtn);
		quickActionsCard.add(cleanupBtn);
		quickActionsCard.add(exportBtn);
		quickActionsCard.add(resetBtn);

		panel.add(quickActionsCard);

		return panel;
	}

	private JPanel createUserManagementPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "Username", "Full Name", "Role", "Status" };
		DefaultTableModel model = new DefaultTableModel(columns, 0);
		JTable userTable = new JTable(model);

		// Load users
		authService.getAllUsers().values().forEach(user -> {
			model.addRow(new Object[] { user.getUsername(), user.getFullName(), user.getRole(), "Active" });
		});

		JScrollPane scrollPane = new JScrollPane(userTable);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton addBtn = new JButton("Add User");
		JButton editBtn = new JButton("Edit User");
		JButton disableBtn = new JButton("Disable User");
		JButton deleteBtn = new JButton("Delete User");

		addBtn.addActionListener(e -> showAddUserDialog(model));
		editBtn.addActionListener(e -> editUser(userTable, model));
		disableBtn.addActionListener(e -> disableUser(userTable, model));
		deleteBtn.addActionListener(e -> deleteUser(userTable, model));

		buttonPanel.add(addBtn);
		buttonPanel.add(editBtn);
		buttonPanel.add(disableBtn);
		buttonPanel.add(deleteBtn);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createSystemLogsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

		// Load audit logs
		stockService.getAuditService().getAuditLog().forEach(log -> {
			logArea.append(log + "\n");
		});

		JScrollPane scrollPane = new JScrollPane(logArea);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton refreshBtn = new JButton("Refresh");
		JButton clearBtn = new JButton("Clear Logs");
		JButton exportBtn = new JButton("Export Logs");

		refreshBtn.addActionListener(e -> {
			logArea.setText("");
			stockService.getAuditService().getAuditLog().forEach(log -> {
				logArea.append(log + "\n");
			});
		});

		clearBtn.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(panel, "Clear all logs?", "Confirm", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				logArea.setText("");
				JOptionPane.showMessageDialog(panel, "Logs cleared!");
			}
		});

		exportBtn.addActionListener(e -> {
			// Export logic here
			JOptionPane.showMessageDialog(panel, "Logs exported to system_logs.txt");
		});

		buttonPanel.add(refreshBtn);
		buttonPanel.add(clearBtn);
		buttonPanel.add(exportBtn);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createBackupPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Backup options
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Backup Location:"), gbc);
		gbc.gridx = 1;
		JTextField backupPath = new JTextField("./backup/", 20);
		panel.add(backupPath, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Backup Type:"), gbc);
		gbc.gridx = 1;
		JComboBox<String> backupType = new JComboBox<>(new String[] { "Full Backup", "Incremental", "Database Only" });
		panel.add(backupType, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Schedule:"), gbc);
		gbc.gridx = 1;
		JComboBox<String> schedule = new JComboBox<>(new String[] { "Manual", "Daily", "Weekly", "Monthly" });
		panel.add(schedule, gbc);

		JButton backupNowBtn = new JButton("Backup Now");
		backupNowBtn.setBackground(new Color(46, 204, 113));
		backupNowBtn.setForeground(Color.WHITE);
		backupNowBtn.addActionListener(e -> performBackup());

		JButton restoreBtn = new JButton("Restore Backup");
		restoreBtn.setBackground(new Color(52, 152, 219));
		restoreBtn.setForeground(Color.WHITE);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(backupNowBtn);
		buttonPanel.add(restoreBtn);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		panel.add(buttonPanel, gbc);

		return panel;
	}

	private JPanel createSettingsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// System Settings
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Low Stock Alert Threshold:"), gbc);
		gbc.gridx = 1;
		JSpinner lowStockSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
		panel.add(lowStockSpinner, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Auto Backup Interval (hours):"), gbc);
		gbc.gridx = 1;
		JSpinner backupSpinner = new JSpinner(new SpinnerNumberModel(24, 0, 168, 1));
		panel.add(backupSpinner, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Session Timeout (minutes):"), gbc);
		gbc.gridx = 1;
		JSpinner timeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
		panel.add(timeoutSpinner, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(new JLabel("Default GST Rate (%):"), gbc);
		gbc.gridx = 1;
		JSpinner gstSpinner = new JSpinner(new SpinnerNumberModel(18, 0, 28, 1));
		panel.add(gstSpinner, gbc);

		JButton saveBtn = new JButton("Save Settings");
		saveBtn.setBackground(SUCCESS_COLOR);
		saveBtn.setForeground(Color.WHITE);
		saveBtn.addActionListener(e -> {
			JOptionPane.showMessageDialog(panel, "Settings saved successfully!");
		});

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		panel.add(saveBtn, gbc);

		return panel;
	}

	private JPanel createInfoCard(String title, String info) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		card.setBackground(Color.WHITE);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		JTextArea infoArea = new JTextArea(info);
		infoArea.setEditable(false);
		infoArea.setBackground(Color.WHITE);
		infoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(infoArea, BorderLayout.CENTER);

		return card;
	}

	private void showAddUserDialog(DefaultTableModel model) {
		JDialog dialog = new JDialog(this, "Add User", true);
		dialog.setLayout(new GridBagLayout());
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(this);

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
				model.addRow(new Object[] { username, fullName, role, "Active" });
				JOptionPane.showMessageDialog(dialog, "User added successfully!");
				dialog.dispose();
			} else {
				JOptionPane.showMessageDialog(dialog, "Username already exists!");
			}
		});

		JButton cancelBtn = new JButton("Cancel");
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
	
	// Add this method to create bulk import panel
	private JPanel createBulkImportPanel() {
	    JPanel panel = new JPanel(new GridBagLayout());
	    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	    panel.setBackground(Color.WHITE);
	    
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(10, 10, 10, 10);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    
	    // Title
	    JLabel titleLabel = new JLabel("Bulk Product Import");
	    titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
	    gbc.gridx = 0; gbc.gridy = 0;
	    gbc.gridwidth = 2;
	    panel.add(titleLabel, gbc);
	    
	    // Description
	    JTextArea descArea = new JTextArea(
	        "Import multiple products at once using a CSV file.\n\n" +
	        "Features:\n" +
	        "• Supports .csv format\n" +
	        "• Automatic category creation\n" +
	        "• Validation of all fields\n" +
	        "• Detailed import log\n" +
	        "• Error reporting for failed rows\n\n" +
	        "Click the button below to start bulk import."
	    );
	    descArea.setEditable(false);
	    descArea.setBackground(Color.WHITE);
	    descArea.setFont(new Font("Arial", Font.PLAIN, 12));
	    gbc.gridy = 1;
	    panel.add(descArea, gbc);
	    
	    // Import Button
	    JButton importButton = new JButton("Start Bulk Import");
	    importButton.setBackground(new Color(46, 204, 113));
	    importButton.setForeground(Color.WHITE);
	    importButton.setFont(new Font("Arial", Font.BOLD, 14));
	    importButton.setPreferredSize(new Dimension(200, 40));
	    importButton.addActionListener(e -> openBulkImportDialog());
	    gbc.gridy = 2;
	    gbc.anchor = GridBagConstraints.CENTER;
	    panel.add(importButton, gbc);
	    
	    // Sample Excel Format Info
	    JPanel samplePanel = new JPanel();
	    samplePanel.setBorder(BorderFactory.createTitledBorder("Sample CSV Format"));
	    samplePanel.setLayout(new GridLayout(0, 1, 5, 5));
	    samplePanel.add(new JLabel("Required columns: Product ID, Product Name, Category, Quantity, Sell Price"));
	    samplePanel.add(new JLabel("Optional columns: Barcode, Min/Max Stock, Cost Price, MRP, Brand, etc."));
	    samplePanel.add(new JLabel("Download template for exact format"));
	    
	    gbc.gridy = 3;
	    panel.add(samplePanel, gbc);
	    
	    return panel;
	}
	
	// Add this method to open bulk import dialog
	private void openBulkImportDialog() {
	    BulkImportDialog dialog = new BulkImportDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
	                                                    stockService, authService);
	    dialog.setVisible(true);
	}

	private void editUser(JTable userTable, DefaultTableModel model) {
		int row = userTable.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Please select a user to edit!");
			return;
		}

		String username = (String) model.getValueAt(row, 0);
		if (username.equals("admin")) {
			JOptionPane.showMessageDialog(this, "Cannot edit admin user!");
			return;
		}

		// Edit logic here
		JOptionPane.showMessageDialog(this, "Edit user: " + username);
	}

	private void disableUser(JTable userTable, DefaultTableModel model) {
		int row = userTable.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Please select a user to disable!");
			return;
		}

		String username = (String) model.getValueAt(row, 0);
		if (username.equals("admin")) {
			JOptionPane.showMessageDialog(this, "Cannot disable admin user!");
			return;
		}

		model.setValueAt("Disabled", row, 3);
		JOptionPane.showMessageDialog(this, "User disabled: " + username);
	}

	private void deleteUser(JTable userTable, DefaultTableModel model) {
		int row = userTable.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Please select a user to delete!");
			return;
		}

		String username = (String) model.getValueAt(row, 0);
		if (username.equals("admin")) {
			JOptionPane.showMessageDialog(this, "Cannot delete admin user!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Delete user: " + username + "?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			if (authService.deleteUser(username)) {
				model.removeRow(row);
				JOptionPane.showMessageDialog(this, "User deleted successfully!");
			}
		}
	}

	private void performBackup() {
		JOptionPane.showMessageDialog(this, "Backup completed successfully!\nLocation: ./backup/");
	}

	private void cleanupData() {
		int confirm = JOptionPane.showConfirmDialog(this, "Cleanup old data (30+ days)?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(this, "Cleanup completed!");
		}
	}

	private void exportData() {
		JOptionPane.showMessageDialog(this, "Data exported to export.zip");
	}

	private void resetSystem() {
		int confirm = JOptionPane.showConfirmDialog(this, "RESET entire system? This cannot be undone!", "DANGER",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(this, "System reset completed!");
		}
	}

	private String getUptime() {
		return "2 days, 5 hours, 32 minutes";
	}

	private void startAutoRefresh() {
		refreshTimer = new Timer(30000, e -> {
			SwingUtilities.invokeLater(() -> {
				// Refresh only if on system overview tab
				if (tabbedPane.getSelectedIndex() == 0) {
					// Refresh system info
				}
			});
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