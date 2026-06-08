package ui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class BillingUI extends JDialog {
	private StockService stockService;
	private AuthService authService;
	private SettingsService settingsService;
	private Bill currentBill;
	private DefaultTableModel cartTableModel;
	private JTextField searchField;
	private JTable productTable;
	private JTable cartTable;
	private JLabel subtotalLabel, discountLabel, gstLabel, grandTotalLabel;
	private JComboBox<String> paymentMethodCombo;
	private JTextField customerNameField, customerPhoneField;

	private final String[] CART_COLUMNS = { "Product", "Qty", "Price", "Disc%", "Total" };
	private final String[] PRODUCT_COLUMNS = { "ID", "Name", "Price", "Stock" };

	public BillingUI(JFrame parent, StockService stockService, AuthService authService) {
		super(parent, "Billing System", true);
		this.stockService = stockService;
		this.authService = authService;
		this.settingsService = new SettingsService();
		this.currentBill = new Bill();
		currentBill.setSoldBy(authService.getCurrentUser().getFullName());

		initializeUI();
		loadProducts();
		setSize(1200, 800);
		setLocationRelativeTo(parent);
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		// Top Panel - Search and Customer
		JPanel topPanel = createTopPanel();

		// Center Panel - Split Pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(createProductPanel());
		splitPane.setBottomComponent(createCartPanel());
		splitPane.setResizeWeight(0.4);

		// Bottom Panel - Bill Summary
		JPanel bottomPanel = createBillSummaryPanel();

		add(topPanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private JPanel createTopPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setBackground(new Color(70, 130, 180));

		JLabel titleLabel = new JLabel("Point of Sale (POS) Billing System", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(Color.WHITE);

		JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		customerPanel.setBackground(new Color(70, 130, 180));
		customerPanel.add(new JLabel("Customer:"));
		customerNameField = new JTextField(15);
		customerPhoneField = new JTextField(10);
		customerPanel.add(customerNameField);
		customerPanel.add(new JLabel("Phone:"));
		customerPanel.add(customerPhoneField);

		JButton customerBtn = new JButton("Add Customer");
		customerBtn.addActionListener(e -> showCustomerDialog());
		customerPanel.add(customerBtn);

		panel.add(titleLabel, BorderLayout.CENTER);
		panel.add(customerPanel, BorderLayout.EAST);

		return panel;
	}

	private JPanel createProductPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Products"));

		// Search bar
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchPanel.add(new JLabel("Search:"));
		searchField = new JTextField(20);
		JButton searchBtn = new JButton("Search");
		searchBtn.addActionListener(e -> searchProducts());
		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addActionListener(e -> loadProducts());
		searchPanel.add(searchField);
		searchPanel.add(searchBtn);
		searchPanel.add(refreshBtn);

		// Product table
		productTable = new JTable();
		productTable.setModel(new DefaultTableModel(PRODUCT_COLUMNS, 0));
		productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		productTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					addToCart();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(productTable);

		JButton addToCartBtn = new JButton("Add to Cart (Double-click or Click)");
		addToCartBtn.addActionListener(e -> addToCart());

		panel.add(searchPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(addToCartBtn, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createCartPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

		// Cart table
		cartTableModel = new DefaultTableModel(CART_COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1 || column == 3; // Qty and Discount% are editable
			}
		};

		cartTable = new JTable(cartTableModel);
		cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Handle cell editing with proper parsing
		cartTable.getModel().addTableModelListener(e -> {
			int row = e.getFirstRow();
			int col = e.getColumn();
			if (col == 1 || col == 3) {
				updateCartItem(row);
			}
		});

		JScrollPane scrollPane = new JScrollPane(cartTable);

		// Cart buttons
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton removeBtn = new JButton("Remove Selected");
		JButton clearCartBtn = new JButton("Clear Cart");

		removeBtn.addActionListener(e -> removeFromCart());
		clearCartBtn.addActionListener(e -> clearCart());

		buttonPanel.add(removeBtn);
		buttonPanel.add(clearCartBtn);

		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createBillSummaryPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Bill Summary"));
		panel.setPreferredSize(new Dimension(0, 200));

		// Summary labels
		JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 10, 10));
		summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		subtotalLabel = new JLabel("Subtotal: ₹0.00", SwingConstants.CENTER);
		subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
		discountLabel = new JLabel("Discount: ₹0.00", SwingConstants.CENTER);
		discountLabel.setFont(new Font("Arial", Font.BOLD, 14));
		gstLabel = new JLabel("GST (18%): ₹0.00", SwingConstants.CENTER);
		gstLabel.setFont(new Font("Arial", Font.BOLD, 14));
		grandTotalLabel = new JLabel("Grand Total: ₹0.00", SwingConstants.CENTER);
		grandTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
		grandTotalLabel.setForeground(new Color(46, 204, 113));

		summaryPanel.add(subtotalLabel);
		summaryPanel.add(discountLabel);
		summaryPanel.add(gstLabel);
		summaryPanel.add(grandTotalLabel);

		// Payment panel
		JPanel paymentPanel = new JPanel(new FlowLayout());
		paymentPanel.add(new JLabel("Payment Method:"));
		paymentMethodCombo = new JComboBox<>(new String[] { "CASH", "CARD", "UPI", "CREDIT" });
		paymentPanel.add(paymentMethodCombo);

		// Action buttons
		JPanel actionPanel = new JPanel(new FlowLayout());
		JButton generateBillBtn = new JButton("Generate Bill");
		JButton printBillBtn = new JButton("Print Bill");
		JButton cancelBtn = new JButton("Cancel");

		generateBillBtn.setBackground(new Color(46, 204, 113));
		generateBillBtn.setForeground(Color.WHITE);
		printBillBtn.setBackground(new Color(52, 152, 219));
		printBillBtn.setForeground(Color.WHITE);
		cancelBtn.setBackground(new Color(231, 76, 60));
		cancelBtn.setForeground(Color.WHITE);

		generateBillBtn.addActionListener(e -> generateBill());
		printBillBtn.addActionListener(e -> printBill());
		cancelBtn.addActionListener(e -> dispose());

		actionPanel.add(generateBillBtn);
		actionPanel.add(printBillBtn);
		actionPanel.add(cancelBtn);

		panel.add(summaryPanel, BorderLayout.CENTER);
		panel.add(paymentPanel, BorderLayout.WEST);
		panel.add(actionPanel, BorderLayout.EAST);

		return panel;
	}

	private void loadProducts() {
		List<Product> products = stockService.getAllProducts();
		updateProductTable(products);
	}

	private void searchProducts() {
		String searchTerm = searchField.getText().trim();
		if (searchTerm.isEmpty()) {
			loadProducts();
			return;
		}
		List<Product> results = stockService.searchProducts(searchTerm);
		updateProductTable(results);
	}

	private void updateProductTable(List<Product> products) {
		DefaultTableModel model = (DefaultTableModel) productTable.getModel();
		model.setRowCount(0);

		for (Product product : products) {
			if (product.isActive() && product.getQuantity() > 0) {
				model.addRow(new Object[] { product.getId(), product.getName(),
						String.format("₹%.2f", product.getSellPrice()), product.getQuantity() });
			}
		}
	}

	private void addToCart() {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a product!");
			return;
		}

		String productId = (String) productTable.getValueAt(selectedRow, 0);
		Product product = stockService.getProduct(productId);

		if (product == null)
			return;

		String qtyStr = JOptionPane.showInputDialog(this, "Product: " + product.getName() + "\nAvailable: "
				+ product.getQuantity() + "\nPrice: ₹" + product.getSellPrice() + "\n\nEnter quantity:");

		if (qtyStr != null) {
			try {
				int quantity = Integer.parseInt(qtyStr);
				if (quantity <= 0) {
					JOptionPane.showMessageDialog(this, "Quantity must be positive!");
					return;
				}
				if (quantity > product.getQuantity()) {
					JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + product.getQuantity());
					return;
				}

				// Check if product already in cart
				boolean found = false;
				for (int i = 0; i < cartTableModel.getRowCount(); i++) {
					if (cartTableModel.getValueAt(i, 0).equals(product.getName())) {
						int currentQty = parseQuantity(cartTableModel.getValueAt(i, 1));
						cartTableModel.setValueAt(currentQty + quantity, i, 1);
						updateCartItem(i);
						found = true;
						break;
					}
				}

				if (!found) {
					CartItem cartItem = new CartItem(product, quantity);
					currentBill.addItem(cartItem);
					cartTableModel.addRow(
							new Object[] { product.getName(), quantity, String.format("₹%.2f", product.getSellPrice()),
									"0", String.format("₹%.2f", cartItem.getTotal()) });
				}

				updateBillSummary();
				JOptionPane.showMessageDialog(this, "Product added to cart!");

			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number!");
			}
		}
	}

	private int parseQuantity(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	private double parseDoubleValue(Object value) {
		if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof String) {
			try {
				return Double.parseDouble((String) value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	private void updateCartItem(int row) {
		String productName = (String) cartTableModel.getValueAt(row, 0);

		// Safely parse quantity
		Object qtyObj = cartTableModel.getValueAt(row, 1);
		int newQuantity = parseQuantity(qtyObj);

		// Safely parse discount
		Object discObj = cartTableModel.getValueAt(row, 3);
		double discountPercent = parseDoubleValue(discObj);

		// Find and update cart item
		for (CartItem item : currentBill.getItems()) {
			if (item.getProduct().getName().equals(productName)) {
				if (newQuantity <= 0) {
					currentBill.removeItem(currentBill.getItems().indexOf(item));
					cartTableModel.removeRow(row);
				} else {
					// Check if quantity exceeds available stock
					if (newQuantity > item.getProduct().getQuantity()) {
						JOptionPane.showMessageDialog(this,
								"Insufficient stock! Available: " + item.getProduct().getQuantity(), "Stock Alert",
								JOptionPane.WARNING_MESSAGE);
						cartTableModel.setValueAt(item.getQuantity(), row, 1);
						return;
					}

					item.setQuantity(newQuantity);
					item.setDiscountPercent(discountPercent);
					cartTableModel.setValueAt(String.format("₹%.2f", item.getTotal()), row, 4);
				}
				break;
			}
		}

		updateBillSummary();
	}

	private void removeFromCart() {
		int selectedRow = cartTable.getSelectedRow();
		if (selectedRow != -1) {
			currentBill.removeItem(selectedRow);
			cartTableModel.removeRow(selectedRow);
			updateBillSummary();
		} else {
			JOptionPane.showMessageDialog(this, "Please select an item to remove!");
		}
	}

	private void clearCart() {
		int confirm = JOptionPane.showConfirmDialog(this, "Clear entire cart?", "Confirm", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			currentBill = new Bill();
			currentBill.setSoldBy(authService.getCurrentUser().getFullName());
			cartTableModel.setRowCount(0);
			updateBillSummary();
		}
	}

	private void updateBillSummary() {
		currentBill.calculateTotals();
		subtotalLabel.setText(String.format("Subtotal: ₹%.2f", currentBill.getSubtotal()));
		discountLabel.setText(String.format("Discount: ₹%.2f", currentBill.getTotalDiscount()));
		gstLabel.setText(String.format("GST (18%%): ₹%.2f", currentBill.getTotalGst()));
		grandTotalLabel.setText(String.format("Grand Total: ₹%.2f", currentBill.getGrandTotal()));
	}

	private void generateBill() {
		if (currentBill.getItems().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cart is empty!");
			return;
		}

		// Set customer info
		String customerName = customerNameField.getText().trim();
		if (!customerName.isEmpty()) {
			currentBill.setCustomerName(customerName);
		}
		currentBill.setCustomerPhone(customerPhoneField.getText().trim());
		currentBill.setPaymentMethod((String) paymentMethodCombo.getSelectedItem());

		// Handle UPI payment separately
		if (currentBill.getPaymentMethod().equals("UPI")) {
			processUPIPayment();
			return;
		}

		// For other payment methods
		String paidStr = JOptionPane.showInputDialog(this,
				String.format("Total Amount: ₹%.2f\nEnter Amount Received:", currentBill.getGrandTotal()));

		if (paidStr != null) {
			try {
				double paid = Double.parseDouble(paidStr);
				if (paid < currentBill.getGrandTotal()) {
					JOptionPane.showMessageDialog(this, "Insufficient payment amount!");
					return;
				}
				currentBill.setPaidAmount(paid);
				currentBill.setPaymentStatus("PAID");

				// Process sale and update stock
				boolean success = true;
				for (CartItem item : currentBill.getItems()) {
					if (!stockService.sellProduct(item.getProduct().getId(), item.getQuantity(),
							authService.getCurrentUser().getUsername())) {
						success = false;
						break;
					}
				}

				if (success) {
					stockService.saveBill(currentBill);
					JOptionPane.showMessageDialog(this,
							String.format("Bill Generated Successfully!\nBill No: %s\nChange: ₹%.2f",
									currentBill.getBillNumber(), currentBill.getBalanceAmount()));

					printBill();
					clearCart();
					loadProducts();

					int newBill = JOptionPane.showConfirmDialog(this, "Start new bill?", "New Bill",
							JOptionPane.YES_NO_OPTION);
					if (newBill == JOptionPane.NO_OPTION) {
						dispose();
					}
				} else {
					JOptionPane.showMessageDialog(this, "Error processing sale!");
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Invalid amount!");
			}
		}
	}

	private void processUPIPayment() {
		if (currentBill.getItems().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cart is empty!");
			return;
		}

		JDialog upiDialog = new JDialog(this, "UPI Payment", true);
		upiDialog.setLayout(new BorderLayout());
		upiDialog.setSize(600, 700);
		upiDialog.setLocationRelativeTo(this);

		// Create selection panel for UPI accounts
		JPanel selectionPanel = new JPanel(new FlowLayout());
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Select UPI Account"));

		JComboBox<String> upiAccountCombo = new JComboBox<>();
		UPISettings settings = settingsService.getUPISettings();

		for (UPISettings.UPIAccount account : settings.getUpiAccounts().values()) {
			if (account.isActive()) {
				upiAccountCombo.addItem(account.getUpiId());
			}
		}
		upiAccountCombo.setSelectedItem(settings.getDefaultUPIId());

		selectionPanel.add(new JLabel("Pay to:"));
		selectionPanel.add(upiAccountCombo);

		// Create QR Code panel with selected UPI ID
		JPanel qrPanel = new JPanel(new BorderLayout());
		updateQRCodePanel(qrPanel, settings, (String) upiAccountCombo.getSelectedItem());

		upiAccountCombo.addActionListener(e -> {
			String selectedUPI = (String) upiAccountCombo.getSelectedItem();
			updateQRCodePanel(qrPanel, settings, selectedUPI);
		});

		// Payment verification panel
		JPanel verifyPanel = createVerifyPanel(upiDialog, settings);

		upiDialog.add(selectionPanel, BorderLayout.NORTH);
		upiDialog.add(qrPanel, BorderLayout.CENTER);
		upiDialog.add(verifyPanel, BorderLayout.SOUTH);
		upiDialog.setVisible(true);
	}

	private void updateQRCodePanel(JPanel qrPanel, UPISettings settings, String selectedUPI) {
		qrPanel.removeAll();
		JPanel newQRPanel = UPIQRCodeGenerator.createQRCodePanel(currentBill, settings, selectedUPI);
		qrPanel.add(newQRPanel, BorderLayout.CENTER);
		qrPanel.revalidate();
		qrPanel.repaint();
	}

	private JPanel createVerifyPanel(JDialog parent, UPISettings settings) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Verify Payment"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("UPI Transaction ID:"), gbc);
		gbc.gridx = 1;
		JTextField txnIdField = new JTextField(20);
		panel.add(txnIdField, gbc);

		JButton verifyBtn = new JButton("Verify Payment");
		verifyBtn.setBackground(new Color(46, 204, 113));
		verifyBtn.setForeground(Color.WHITE);
		verifyBtn.addActionListener(e -> {
			String txnId = txnIdField.getText().trim();
			if (txnId.isEmpty()) {
				JOptionPane.showMessageDialog(parent, "Please enter Transaction ID!");
				return;
			}

			currentBill.setUpiTransactionId(txnId);
			currentBill.setPaymentStatus("PAID");
			currentBill.setPaidAmount(currentBill.getGrandTotal());

			// Process sale
			boolean success = true;
			for (CartItem item : currentBill.getItems()) {
				if (!stockService.sellProduct(item.getProduct().getId(), item.getQuantity(),
						authService.getCurrentUser().getUsername())) {
					success = false;
					break;
				}
			}

			if (success) {
				stockService.saveBill(currentBill);
				JOptionPane.showMessageDialog(parent, String.format(
						"Payment Successful!\nTransaction ID: %s\nBill No: %s", txnId, currentBill.getBillNumber()));
				parent.dispose();

				printBill();
				clearCart();
				loadProducts();

				int newBill = JOptionPane.showConfirmDialog(this, "Start new bill?", "New Bill",
						JOptionPane.YES_NO_OPTION);
				if (newBill == JOptionPane.NO_OPTION) {
					dispose();
				}
			} else {
				JOptionPane.showMessageDialog(parent, "Error processing sale!");
			}
		});

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		panel.add(verifyBtn, gbc);

		if (settings.isAutoVerifyPayment()) {
			JLabel autoLabel = new JLabel("Auto-verification is enabled. Payment will be auto-verified.");
			autoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
			autoLabel.setForeground(Color.GRAY);
			gbc.gridy = 2;
			panel.add(autoLabel, gbc);
		}

		return panel;
	}

	private void printBill() {
		String billText = currentBill.getFormattedBill();

		JTextArea textArea = new JTextArea(billText);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 600));

		int option = JOptionPane.showConfirmDialog(this, scrollPane, "Bill Preview - Print?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

		if (option == JOptionPane.OK_OPTION) {
			try {
				textArea.print();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage());
			}
		}

		// Also print to console for physical printing
		System.out.println(billText);
	}

	private void showCustomerDialog() {
		JDialog dialog = new JDialog(this, "Customer Details", true);
		dialog.setLayout(new GridBagLayout());
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		JTextField nameField = new JTextField(customerNameField.getText(), 15);
		JTextField phoneField = new JTextField(customerPhoneField.getText(), 15);
		JTextField emailField = new JTextField(15);

		gbc.gridx = 0;
		gbc.gridy = 0;
		dialog.add(new JLabel("Customer Name:"), gbc);
		gbc.gridx = 1;
		dialog.add(nameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		dialog.add(new JLabel("Phone Number:"), gbc);
		gbc.gridx = 1;
		dialog.add(phoneField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		dialog.add(new JLabel("Email:"), gbc);
		gbc.gridx = 1;
		dialog.add(emailField, gbc);

		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(e -> {
			customerNameField.setText(nameField.getText());
			customerPhoneField.setText(phoneField.getText());
			dialog.dispose();
		});

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		dialog.add(saveBtn, gbc);

		dialog.setVisible(true);
	}
}