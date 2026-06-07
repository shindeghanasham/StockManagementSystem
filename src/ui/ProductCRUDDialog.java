package ui;

import model.*;
import service.*;
import javax.swing.*;
import java.awt.*;

public class ProductCRUDDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StockService stockService;
	private AuthService authService;
	private Product currentProduct;
	private boolean isEditMode;
	private Runnable onSaveCallback;

	// Form fields
	private JTextField idField, barcodeField, nameField, quantityField, minStockField, maxStockField;
	private JTextField costPriceField, sellPriceField, mrpField, weightField;
	private JTextField brandField, supplierField, locationField, unitField;
	private JTextArea descriptionArea;
	private JComboBox<String> categoryCombo;
	private JCheckBox activeCheckBox;
	public ProductCRUDDialog(JFrame parent, StockService stockService, AuthService authService, Product product,
			boolean isEditMode, Runnable onSaveCallback) {
		super(parent, isEditMode ? "Edit Product" : "Add Product", true);
		this.stockService = stockService;
		this.authService = authService;
		this.currentProduct = product;
		this.isEditMode = isEditMode;
		this.onSaveCallback = onSaveCallback;

		initializeUI();

		if (isEditMode && product != null) {
			loadProductData();
		}

		setSize(700, 750);
		setLocationRelativeTo(parent);
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		// Main panel with tabs
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Basic Information", createBasicInfoPanel());
		tabbedPane.addTab("Pricing & Stock", createPricingStockPanel());
		tabbedPane.addTab("Additional Info", createAdditionalInfoPanel());

		// Button panel
		JPanel buttonPanel = createButtonPanel();

		add(tabbedPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createBasicInfoPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Product ID
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Product ID:*"), gbc);
		gbc.gridx = 1;
		idField = new JTextField(20);
		if (isEditMode)
			idField.setEnabled(false);
		panel.add(idField, gbc);

		// Barcode
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Barcode:"), gbc);
		gbc.gridx = 1;
		barcodeField = new JTextField(20);
		panel.add(barcodeField, gbc);

		// Product Name
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Product Name:*"), gbc);
		gbc.gridx = 1;
		nameField = new JTextField(20);
		panel.add(nameField, gbc);

		// Category
		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(new JLabel("Category:*"), gbc);
		gbc.gridx = 1;
		categoryCombo = new JComboBox<>();
		loadCategories();
		panel.add(categoryCombo, gbc);

		// Brand
		gbc.gridx = 0;
		gbc.gridy = 4;
		panel.add(new JLabel("Brand:"), gbc);
		gbc.gridx = 1;
		brandField = new JTextField(20);
		panel.add(brandField, gbc);

		// Supplier
		gbc.gridx = 0;
		gbc.gridy = 5;
		panel.add(new JLabel("Supplier:"), gbc);
		gbc.gridx = 1;
		supplierField = new JTextField(20);
		panel.add(supplierField, gbc);

		// Location
		gbc.gridx = 0;
		gbc.gridy = 6;
		panel.add(new JLabel("Storage Location:"), gbc);
		gbc.gridx = 1;
		locationField = new JTextField(20);
		panel.add(locationField, gbc);

		// Unit
		gbc.gridx = 0;
		gbc.gridy = 7;
		panel.add(new JLabel("Unit of Measure:"), gbc);
		gbc.gridx = 1;
		unitField = new JTextField("Pcs", 20);
		panel.add(unitField, gbc);

		// Active status
		gbc.gridx = 0;
		gbc.gridy = 8;
		panel.add(new JLabel("Status:"), gbc);
		gbc.gridx = 1;
		activeCheckBox = new JCheckBox("Active", true);
		panel.add(activeCheckBox, gbc);

		return panel;
	}

	private JPanel createPricingStockPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Quantity
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Current Stock:*"), gbc);
		gbc.gridx = 1;
		quantityField = new JTextField("0", 20);
		panel.add(quantityField, gbc);

		// Min Stock Level
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Min Stock Level:*"), gbc);
		gbc.gridx = 1;
		minStockField = new JTextField("10", 20);
		panel.add(minStockField, gbc);

		// Max Stock Level
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Max Stock Level:"), gbc);
		gbc.gridx = 1;
		maxStockField = new JTextField("100", 20);
		panel.add(maxStockField, gbc);

		// Cost Price
		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(new JLabel("Cost Price (₹):*"), gbc);
		gbc.gridx = 1;
		costPriceField = new JTextField(20);
		panel.add(costPriceField, gbc);

		// Sell Price
		gbc.gridx = 0;
		gbc.gridy = 4;
		panel.add(new JLabel("Sell Price (₹):*"), gbc);
		gbc.gridx = 1;
		sellPriceField = new JTextField(20);
		panel.add(sellPriceField, gbc);

		// MRP
		gbc.gridx = 0;
		gbc.gridy = 5;
		panel.add(new JLabel("MRP (₹):"), gbc);
		gbc.gridx = 1;
		mrpField = new JTextField(20);
		panel.add(mrpField, gbc);

		// Weight
		gbc.gridx = 0;
		gbc.gridy = 6;
		panel.add(new JLabel("Weight (kg):"), gbc);
		gbc.gridx = 1;
		weightField = new JTextField("0", 20);
		panel.add(weightField, gbc);

		// Info panel for profit calculation
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Profit Information"));
		infoPanel.setLayout(new GridLayout(2, 1));
		JLabel profitLabel = new JLabel("Profit per unit: ₹0.00");
		JLabel marginLabel = new JLabel("Profit Margin: 0%");
		infoPanel.add(profitLabel);
		infoPanel.add(marginLabel);

		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 2;
		panel.add(infoPanel, gbc);

		// Add listeners to update profit info
		costPriceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				updateProfitInfo(profitLabel, marginLabel);
			}

			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				updateProfitInfo(profitLabel, marginLabel);
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				updateProfitInfo(profitLabel, marginLabel);
			}
		});

		sellPriceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				updateProfitInfo(profitLabel, marginLabel);
			}

			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				updateProfitInfo(profitLabel, marginLabel);
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				updateProfitInfo(profitLabel, marginLabel);
			}
		});

		return panel;
	}

	private JPanel createAdditionalInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Description
		JPanel descPanel = new JPanel(new BorderLayout());
		descPanel.setBorder(BorderFactory.createTitledBorder("Description"));
		descriptionArea = new JTextArea(5, 30);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(descriptionArea);
		descPanel.add(scrollPane, BorderLayout.CENTER);

		// Stats panel for edit mode
		if (isEditMode) {
			JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
			statsPanel.setBorder(BorderFactory.createTitledBorder("Product Statistics"));
			statsPanel.add(new JLabel("Added Date:"));
			statsPanel.add(new JLabel(""));
			statsPanel.add(new JLabel("Last Updated:"));
			statsPanel.add(new JLabel(""));
			statsPanel.add(new JLabel("Added By:"));
			statsPanel.add(new JLabel(""));
			statsPanel.add(new JLabel("Total Value:"));
			statsPanel.add(new JLabel(""));

			panel.add(descPanel, BorderLayout.CENTER);
			panel.add(statsPanel, BorderLayout.SOUTH);
		} else {
			panel.add(descPanel, BorderLayout.CENTER);
		}

		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton saveButton = new JButton(isEditMode ? "Update Product" : "Save Product");
		saveButton.setBackground(new Color(46, 204, 113));
		saveButton.setForeground(Color.WHITE);
		saveButton.setFont(new Font("Arial", Font.BOLD, 14));
		saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		saveButton.addActionListener(e -> saveProduct());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBackground(new Color(231, 76, 60));
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
		cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		cancelButton.addActionListener(e -> dispose());

		panel.add(saveButton);
		panel.add(cancelButton);

		return panel;
	}

	private void loadCategories() {
		categoryCombo.removeAllItems();
		stockService.getAllCategories().values().forEach(cat -> {
			categoryCombo.addItem(cat.getId() + " - " + cat.getName());
		});
	}

	private void loadProductData() {
		if (currentProduct == null)
			return;

		idField.setText(currentProduct.getId());
		barcodeField.setText(currentProduct.getBarcode());
		nameField.setText(currentProduct.getName());
		quantityField.setText(String.valueOf(currentProduct.getQuantity()));
		minStockField.setText(String.valueOf(currentProduct.getMinStockLevel()));
		maxStockField.setText(String.valueOf(currentProduct.getMaxStockLevel()));
		costPriceField.setText(String.valueOf(currentProduct.getCostPrice()));
		sellPriceField.setText(String.valueOf(currentProduct.getSellPrice()));
		mrpField.setText(String.valueOf(currentProduct.getMrp()));
		brandField.setText(currentProduct.getBrand());
		supplierField.setText(currentProduct.getSupplier());
		locationField.setText(currentProduct.getLocation());
		unitField.setText(currentProduct.getUnit());
		weightField.setText(String.valueOf(currentProduct.getWeight()));
		descriptionArea.setText(currentProduct.getDescription());
		activeCheckBox.setSelected(currentProduct.isActive());

		// Select category
		String categoryItem = currentProduct.getCategoryId() + " - "
				+ stockService.getCategoryName(currentProduct.getCategoryId());
		categoryCombo.setSelectedItem(categoryItem);
	}

	private void updateProfitInfo(JLabel profitLabel, JLabel marginLabel) {
		try {
			double cost = Double.parseDouble(costPriceField.getText().trim());
			double sell = Double.parseDouble(sellPriceField.getText().trim());
			double profit = sell - cost;
			double margin = (profit / cost) * 100;

			profitLabel.setText(String.format("Profit per unit: ₹%.2f", profit));
			marginLabel.setText(String.format("Profit Margin: %.2f%%", margin));

			if (profit < 0) {
				profitLabel.setForeground(Color.RED);
				marginLabel.setForeground(Color.RED);
			} else {
				profitLabel.setForeground(new Color(46, 204, 113));
				marginLabel.setForeground(new Color(46, 204, 113));
			}
		} catch (NumberFormatException e) {
			profitLabel.setText("Profit per unit: ₹0.00");
			marginLabel.setText("Profit Margin: 0%");
		}
	}

	private void saveProduct() {
		try {
			// Validate required fields
			if (idField.getText().trim().isEmpty()) {
				showError("Product ID is required!");
				return;
			}
			if (nameField.getText().trim().isEmpty()) {
				showError("Product Name is required!");
				return;
			}
			if (categoryCombo.getSelectedItem() == null) {
				showError("Category is required!");
				return;
			}

			String id = idField.getText().trim();
			String barcode = barcodeField.getText().trim();
			String name = nameField.getText().trim();
			String categoryId = ((String) categoryCombo.getSelectedItem()).split(" - ")[0];
			int quantity = Integer.parseInt(quantityField.getText().trim());
			int minStock = Integer.parseInt(minStockField.getText().trim());
			int maxStock = Integer.parseInt(maxStockField.getText().trim());
			double costPrice = Double.parseDouble(costPriceField.getText().trim());
			double sellPrice = Double.parseDouble(sellPriceField.getText().trim());
			double mrp = mrpField.getText().trim().isEmpty() ? sellPrice
					: Double.parseDouble(mrpField.getText().trim());
			String brand = brandField.getText().trim();
			String supplier = supplierField.getText().trim();
			String location = locationField.getText().trim();
			String unit = unitField.getText().trim();
			double weight = weightField.getText().trim().isEmpty() ? 0
					: Double.parseDouble(weightField.getText().trim());
			String description = descriptionArea.getText().trim();
			boolean active = activeCheckBox.isSelected();

			if (quantity < 0) {
				showError("Quantity cannot be negative!");
				return;
			}

			if (sellPrice <= 0) {
				showError("Sell price must be greater than 0!");
				return;
			}

			if (barcode.isEmpty()) {
				barcode = id;
			}

			if (isEditMode) {
				// Update existing product
				currentProduct.setBarcode(barcode);
				currentProduct.setName(name);
				currentProduct.setCategoryId(categoryId);
				currentProduct.setQuantity(quantity);
				currentProduct.setMinStockLevel(minStock);
				currentProduct.setMaxStockLevel(maxStock);
				currentProduct.setCostPrice(costPrice);
				currentProduct.setSellPrice(sellPrice);
				currentProduct.setMrp(mrp);
				currentProduct.setBrand(brand);
				currentProduct.setSupplier(supplier);
				currentProduct.setLocation(location);
				currentProduct.setUnit(unit);
				currentProduct.setWeight(weight);
				currentProduct.setDescription(description);
				currentProduct.setActive(active);
				currentProduct.setUpdatedBy(authService.getCurrentUser().getUsername());

				if (stockService.updateProduct(currentProduct)) {
					JOptionPane.showMessageDialog(this, "Product updated successfully!");
					if (onSaveCallback != null)
						onSaveCallback.run();
					dispose();
				} else {
					showError("Failed to update product!");
				}
			} else {
				// Add new product
				if (stockService.isProductIdExists(id)) {
					showError("Product ID already exists!");
					return;
				}

				Product product = new Product(id, barcode, name, categoryId, quantity, minStock, maxStock, costPrice,
						sellPrice, mrp, brand, supplier, location, unit, weight, description,
						authService.getCurrentUser().getUsername());
				product.setActive(active);

				if (stockService.addProduct(product)) {
					JOptionPane.showMessageDialog(this, "Product added successfully!");
					if (onSaveCallback != null)
						onSaveCallback.run();
					dispose();
				} else {
					showError("Failed to add product!");
				}
			}

		} catch (NumberFormatException ex) {
			showError("Please enter valid numbers for quantity and prices!");
		} catch (Exception ex) {
			showError("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}