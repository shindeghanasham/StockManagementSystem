package ui;

import model.UPISettings;
import service.SettingsService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class UPISettingsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SettingsService settingsService;
	private UPISettings upiSettings;
	private JTable upiAccountsTable;
	private DefaultTableModel tableModel;
	private JComboBox<String> defaultUPICombo;
	private JTextField storeNameField, storeCityField, storeAddressField;
	private JCheckBox enableUPICheckBox, autoVerifyCheckBox;
	private JSpinner surchargeSpinner;
	private JLabel statusLabel;

	private final String[] COLUMNS = { "UPI ID", "Bank Name", "Account Holder", "Active", "Actions" };

	public UPISettingsPanel(SettingsService settingsService) {
		this.settingsService = settingsService;
		this.upiSettings = settingsService.getUPISettings();

		initializeUI();
		loadUPIAccounts();
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Create tabbed pane for different settings
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("UPI Accounts", createUPIAccountsPanel());
		tabbedPane.addTab("Store Information", createStoreInfoPanel());
		tabbedPane.addTab("Payment Settings", createPaymentSettingsPanel());
		tabbedPane.addTab("QR Code Settings", createQRSettingsPanel());

		add(tabbedPane, BorderLayout.CENTER);

		// Bottom panel with save button
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveButton = new JButton("Save All Settings");
		saveButton.setBackground(new Color(46, 204, 113));
		saveButton.setForeground(Color.WHITE);
		saveButton.setFont(new Font("Arial", Font.BOLD, 14));
		saveButton.addActionListener(e -> saveAllSettings());
		bottomPanel.add(saveButton);

		statusLabel = new JLabel(" ");
		statusLabel.setForeground(Color.GREEN);
		bottomPanel.add(statusLabel);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	private JPanel createUPIAccountsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Table for UPI accounts
		tableModel = new DefaultTableModel(COLUMNS, 0) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 3)
					return Boolean.class;
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 3; // Only Active checkbox is editable
			}
		};

		upiAccountsTable = new JTable(tableModel);
		upiAccountsTable.setRowHeight(30);
		upiAccountsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

		// Add button renderer for actions column
		upiAccountsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
		upiAccountsTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

		JScrollPane scrollPane = new JScrollPane(upiAccountsTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		// Add UPI Account Panel
		JPanel addPanel = new JPanel(new GridBagLayout());
		addPanel.setBorder(BorderFactory.createTitledBorder("Add New UPI Account"));
		addPanel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JTextField upiIdField = new JTextField(20);
		JTextField bankNameField = new JTextField(20);
		JTextField holderNameField = new JTextField(20);

		gbc.gridx = 0;
		gbc.gridy = 0;
		addPanel.add(new JLabel("UPI ID:"), gbc);
		gbc.gridx = 1;
		addPanel.add(upiIdField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		addPanel.add(new JLabel("Bank Name:"), gbc);
		gbc.gridx = 1;
		addPanel.add(bankNameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		addPanel.add(new JLabel("Account Holder:"), gbc);
		gbc.gridx = 1;
		addPanel.add(holderNameField, gbc);

		JButton addButton = new JButton("Add UPI Account");
		addButton.setBackground(new Color(52, 152, 219));
		addButton.setForeground(Color.WHITE);
		addButton.addActionListener(e -> {
			String upiId = upiIdField.getText().trim();
			String bankName = bankNameField.getText().trim();
			String holderName = holderNameField.getText().trim();

			if (upiId.isEmpty() || bankName.isEmpty()) {
				JOptionPane.showMessageDialog(panel, "UPI ID and Bank Name are required!");
				return;
			}

			UPISettings.UPIAccount account = new UPISettings.UPIAccount(upiId, bankName, holderName, true);
			upiSettings.addUPIAccount(account);
			loadUPIAccounts();

			upiIdField.setText("");
			bankNameField.setText("");
			holderNameField.setText("");

			updateDefaultUPICombo();
			JOptionPane.showMessageDialog(panel, "UPI Account added successfully!");
		});

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		addPanel.add(addButton, gbc);

		panel.add(addPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createStoreInfoPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Store Name
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Store Name:"), gbc);
		gbc.gridx = 1;
		storeNameField = new JTextField(upiSettings.getStoreName(), 30);
		panel.add(storeNameField, gbc);

		// Store City
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Store City:"), gbc);
		gbc.gridx = 1;
		storeCityField = new JTextField(upiSettings.getStoreCity(), 30);
		panel.add(storeCityField, gbc);

		// Store Address
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Store Address:"), gbc);
		gbc.gridx = 1;
		storeAddressField = new JTextField(upiSettings.getStoreAddress(), 30);
		panel.add(storeAddressField, gbc);

		// Default UPI ID
		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(new JLabel("Default UPI ID:"), gbc);
		gbc.gridx = 1;
		defaultUPICombo = new JComboBox<>();
		updateDefaultUPICombo();
		defaultUPICombo.setSelectedItem(upiSettings.getDefaultUPIId());
		panel.add(defaultUPICombo, gbc);

		return panel;
	}

	private JPanel createPaymentSettingsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Enable UPI Payments
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Enable UPI Payments:"), gbc);
		gbc.gridx = 1;
		enableUPICheckBox = new JCheckBox("Enable", upiSettings.isEnableUPIQR());
		panel.add(enableUPICheckBox, gbc);

		// Auto Verify Payment
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Auto Verify Payment:"), gbc);
		gbc.gridx = 1;
		autoVerifyCheckBox = new JCheckBox("Auto-verify after payment", upiSettings.isAutoVerifyPayment());
		panel.add(autoVerifyCheckBox, gbc);

		// UPI Surcharge
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("UPI Surcharge (%):"), gbc);
		gbc.gridx = 1;
		surchargeSpinner = new JSpinner(new SpinnerNumberModel(upiSettings.getUpiSurchargePercent(), 0.0, 10.0, 0.1));
		panel.add(surchargeSpinner, gbc);

		// Info panel
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Payment Information"));
		infoPanel.setLayout(new GridLayout(3, 1, 5, 5));
		infoPanel.add(new JLabel("• UPI QR codes are compatible with Google Pay, PhonePe, Paytm"));
		infoPanel.add(new JLabel("• Surcharge will be added to customer bill if enabled"));
		infoPanel.add(new JLabel("• Auto-verify will mark payment as PAID automatically"));

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		panel.add(infoPanel, gbc);

		return panel;
	}

	private JPanel createQRSettingsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// QR Code Size
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("QR Code Size:"), gbc);
		gbc.gridx = 1;
		JComboBox<String> qrSizeCombo = new JComboBox<>(
				new String[] { "Small (200px)", "Medium (300px)", "Large (400px)" });
		panel.add(qrSizeCombo, gbc);

		// QR Code Color
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("QR Code Color:"), gbc);
		gbc.gridx = 1;
		JComboBox<String> qrColorCombo = new JComboBox<>(new String[] { "Black & White", "Color", "Brand Color" });
		panel.add(qrColorCombo, gbc);

		// Preview Panel
		JPanel previewPanel = new JPanel();
		previewPanel.setBorder(BorderFactory.createTitledBorder("QR Code Preview"));
		previewPanel.setBackground(Color.WHITE);
		previewPanel.setPreferredSize(new Dimension(300, 300));

		// Sample QR code preview (static for now)
		JLabel previewLabel = new JLabel("QR Preview will appear here", SwingConstants.CENTER);
		previewPanel.add(previewLabel);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		panel.add(previewPanel, gbc);

		return panel;
	}

	private void loadUPIAccounts() {
		tableModel.setRowCount(0);

		for (UPISettings.UPIAccount account : upiSettings.getUpiAccounts().values()) {
			Object[] row = { account.getUpiId(), account.getBankName(), account.getAccountHolderName(),
					account.isActive(), "Edit/Delete" };
			tableModel.addRow(row);
		}
	}

	private void updateDefaultUPICombo() {
		defaultUPICombo.removeAllItems();
		for (UPISettings.UPIAccount account : upiSettings.getUpiAccounts().values()) {
			if (account.isActive()) {
				defaultUPICombo.addItem(account.getUpiId());
			}
		}
	}

	private void saveAllSettings() {
		// Save Store Information
		upiSettings.setStoreName(storeNameField.getText().trim());
		upiSettings.setStoreCity(storeCityField.getText().trim());
		upiSettings.setStoreAddress(storeAddressField.getText().trim());

		// Save Default UPI ID
		String selectedUPI = (String) defaultUPICombo.getSelectedItem();
		if (selectedUPI != null) {
			upiSettings.setDefaultUPIId(selectedUPI);
		}

		// Save Payment Settings
		upiSettings.setEnableUPIQR(enableUPICheckBox.isSelected());
		upiSettings.setAutoVerifyPayment(autoVerifyCheckBox.isSelected());
		upiSettings.setUpiSurchargePercent((Double) surchargeSpinner.getValue());

		// Update account active status from table
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			String upiId = (String) tableModel.getValueAt(i, 0);
			boolean isActive = (boolean) tableModel.getValueAt(i, 3);

			UPISettings.UPIAccount account = upiSettings.getUPIAccount(upiId);
			if (account != null) {
				account.setActive(isActive);
			}
		}

		// Save settings
		settingsService.saveUPISettings(upiSettings);

		statusLabel.setText("Settings saved successfully!");

		// Update timer to clear status
		Timer timer = new Timer(3000, e -> statusLabel.setText(" "));
		timer.setRepeats(false);
		timer.start();

		JOptionPane.showMessageDialog(this, "UPI Settings saved successfully!");
	}

	// Button Renderer for Actions column
	class ButtonRenderer extends JButton implements TableCellRenderer {
		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setText((value == null) ? "Actions" : value.toString());
			setBackground(new Color(52, 152, 219));
			setForeground(Color.WHITE);
			return this;
		}
	}

	// Button Editor for Actions column
	class ButtonEditor extends DefaultCellEditor {
		protected JButton button;
		private String label;
		private boolean isPushed;
		private int currentRow;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(e -> {
				fireEditingStopped();
				handleAction(currentRow);
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			currentRow = row;
			label = (value == null) ? "Actions" : value.toString();
			button.setText(label);
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			isPushed = false;
			return label;
		}

		private void handleAction(int row) {
			String upiId = (String) tableModel.getValueAt(row, 0);

			String[] options = { "Edit", "Delete", "Set as Default", "Cancel" };
			int choice = JOptionPane.showOptionDialog(button, "Manage UPI Account: " + upiId, "UPI Account Actions",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);

			if (choice == 0) {
				// Edit
				editUPIAccount(row);
			} else if (choice == 1) {
				// Delete
				deleteUPIAccount(row);
			} else if (choice == 2) {
				// Set as Default
				defaultUPICombo.setSelectedItem(upiId);
				JOptionPane.showMessageDialog(button, "Default UPI ID set to: " + upiId);
			}
		}

		private void editUPIAccount(int row) {
			String upiId = (String) tableModel.getValueAt(row, 0);
			String bankName = (String) tableModel.getValueAt(row, 1);
			String holderName = (String) tableModel.getValueAt(row, 2);

			JDialog dialog = new JDialog();
			dialog.setTitle("Edit UPI Account");
			dialog.setModal(true);
			dialog.setSize(400, 300);
			dialog.setLocationRelativeTo(button);

			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			JTextField upiIdField = new JTextField(upiId, 20);
			upiIdField.setEditable(false);
			JTextField bankNameField = new JTextField(bankName, 20);
			JTextField holderNameField = new JTextField(holderName, 20);

			gbc.gridx = 0;
			gbc.gridy = 0;
			panel.add(new JLabel("UPI ID:"), gbc);
			gbc.gridx = 1;
			panel.add(upiIdField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			panel.add(new JLabel("Bank Name:"), gbc);
			gbc.gridx = 1;
			panel.add(bankNameField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			panel.add(new JLabel("Account Holder:"), gbc);
			gbc.gridx = 1;
			panel.add(holderNameField, gbc);

			JButton saveBtn = new JButton("Save Changes");
			saveBtn.addActionListener(e -> {
				UPISettings.UPIAccount account = upiSettings.getUPIAccount(upiId);
				if (account != null) {
					account.setBankName(bankNameField.getText().trim());
					account.setAccountHolderName(holderNameField.getText().trim());
					tableModel.setValueAt(account.getBankName(), row, 1);
					tableModel.setValueAt(account.getAccountHolderName(), row, 2);
					dialog.dispose();
					JOptionPane.showMessageDialog(button, "Account updated successfully!");
				}
			});

			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			panel.add(saveBtn, gbc);

			dialog.add(panel);
			dialog.setVisible(true);
		}

		private void deleteUPIAccount(int row) {
			String upiId = (String) tableModel.getValueAt(row, 0);

			if (upiId.equals(upiSettings.getDefaultUPIId())) {
				JOptionPane.showMessageDialog(button,
						"Cannot delete default UPI account!\nSet another account as default first.");
				return;
			}

			int confirm = JOptionPane.showConfirmDialog(button, "Delete UPI Account: " + upiId + "?", "Confirm Delete",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				upiSettings.removeUPIAccount(upiId);
				tableModel.removeRow(row);
				updateDefaultUPICombo();
				JOptionPane.showMessageDialog(button, "UPI Account deleted successfully!");
			}
		}
	}
}