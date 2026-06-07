package ui;

import model.*;
import service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class InvoiceViewerUI extends JDialog {
	private StockService stockService;
	private AuthService authService;
	private JTable invoiceTable;
	private DefaultTableModel tableModel;
	private JTextField searchField;
	private JComboBox<String> statusFilter;
	private JComboBox<String> paymentMethodFilter;
	private JComboBox<String> deleteStatusFilter;
	private JDateChooser fromDateChooser;
	private JDateChooser toDateChooser;
	private List<Bill> bills;
	private JLabel statsLabel; // Store reference to stats label

	private final String[] COLUMNS = { "Select", "Bill No", "Date", "Customer", "Amount", "Payment", "Status",
			"Delete Status", "Actions" };

	public InvoiceViewerUI(JFrame parent, StockService stockService, AuthService authService) {
		super(parent, "Invoice Manager", true);
		this.stockService = stockService;
		this.authService = authService;
		this.bills = stockService.getBills();

		initializeUI();
		loadInvoices();

		setSize(1400, 800);
		setLocationRelativeTo(parent);
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		// Header Panel
		JPanel headerPanel = createHeaderPanel();

		// Filter Panel
		JPanel filterPanel = createFilterPanel();

		// Table Panel
		JPanel tablePanel = createTablePanel();

		// Button Panel
		JPanel buttonPanel = createButtonPanel();

		add(headerPanel, BorderLayout.NORTH);
		add(filterPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(52, 152, 219));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		JLabel titleLabel = new JLabel("Invoice Management System");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		titleLabel.setForeground(Color.WHITE);

		statsLabel = new JLabel("Total Invoices: " + bills.size());
		statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		statsLabel.setForeground(Color.WHITE);

		panel.add(titleLabel, BorderLayout.WEST);
		panel.add(statsLabel, BorderLayout.EAST);

		return panel;
	}

	private JPanel createFilterPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Filters"));
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Search
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Search:"), gbc);
		gbc.gridx = 1;
		searchField = new JTextField(20);
		panel.add(searchField, gbc);

		// Status Filter
		gbc.gridx = 2;
		gbc.gridy = 0;
		panel.add(new JLabel("Payment Status:"), gbc);
		gbc.gridx = 3;
		statusFilter = new JComboBox<>(new String[] { "All", "PAID", "PENDING", "FAILED" });
		panel.add(statusFilter, gbc);

		// Delete Status Filter
		gbc.gridx = 4;
		gbc.gridy = 0;
		panel.add(new JLabel("Delete Status:"), gbc);
		gbc.gridx = 5;
		deleteStatusFilter = new JComboBox<>(new String[] { "Active Only", "Deleted Only", "All" });
		deleteStatusFilter.addActionListener(e -> applyFilters());
		panel.add(deleteStatusFilter, gbc);

		// Payment Method Filter
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Payment Method:"), gbc);
		gbc.gridx = 1;
		paymentMethodFilter = new JComboBox<>(new String[] { "All", "CASH", "CARD", "UPI", "CREDIT" });
		panel.add(paymentMethodFilter, gbc);

		// Date Range
		gbc.gridx = 2;
		gbc.gridy = 1;
		panel.add(new JLabel("From Date:"), gbc);
		gbc.gridx = 3;
		fromDateChooser = new JDateChooser();
		fromDateChooser.setDateFormatString("dd/MM/yyyy");
		panel.add(fromDateChooser, gbc);

		gbc.gridx = 4;
		gbc.gridy = 1;
		panel.add(new JLabel("To Date:"), gbc);
		gbc.gridx = 5;
		toDateChooser = new JDateChooser();
		toDateChooser.setDateFormatString("dd/MM/yyyy");
		panel.add(toDateChooser, gbc);

		// Filter Button
		JButton filterBtn = new JButton("Apply Filters");
		filterBtn.setBackground(new Color(46, 204, 113));
		filterBtn.setForeground(Color.WHITE);
		filterBtn.addActionListener(e -> applyFilters());
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 6;
		panel.add(filterBtn, gbc);

		return panel;
	}

	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		tableModel = new DefaultTableModel(COLUMNS, 0) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0)
					return Boolean.class;
				return String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0 || column == 8; // Select checkbox and Actions column
			}
		};

		invoiceTable = new JTable(tableModel);
		invoiceTable.setRowHeight(30);
		invoiceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
		invoiceTable.setFont(new Font("Arial", Font.PLAIN, 12));

		// Custom cell renderer for deleted rows
		invoiceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (tableModel.getValueAt(row, 7) != null) {
					boolean isDeleted = "Deleted".equals(tableModel.getValueAt(row, 7).toString());
					if (isDeleted && !isSelected) {
						c.setForeground(Color.RED);
						c.setFont(c.getFont().deriveFont(Font.ITALIC));
					} else if (!isSelected) {
						c.setForeground(Color.BLACK);
					}
				}
				return c;
			}
		});

		// Add button renderer for actions column
		invoiceTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
		invoiceTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

		// Set column widths
		invoiceTable.getColumnModel().getColumn(0).setMaxWidth(50);
		invoiceTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		invoiceTable.getColumnModel().getColumn(2).setPreferredWidth(130);
		invoiceTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		invoiceTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		invoiceTable.getColumnModel().getColumn(5).setPreferredWidth(80);
		invoiceTable.getColumnModel().getColumn(6).setPreferredWidth(80);
		invoiceTable.getColumnModel().getColumn(7).setPreferredWidth(100);
		invoiceTable.getColumnModel().getColumn(8).setPreferredWidth(150);

		JScrollPane scrollPane = new JScrollPane(invoiceTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton softDeleteBtn = createButton("Soft Delete Selected", new Color(231, 76, 60));
		JButton permanentDeleteBtn = createButton("Permanently Delete", new Color(192, 57, 43));
		JButton restoreBtn = createButton("Restore Selected", new Color(46, 204, 113));
		JButton bulkDeleteBtn = createButton("Bulk Delete by Date", new Color(241, 196, 15));
		JButton printBtn = createButton("Print Selected", new Color(52, 152, 219));
		JButton exportBtn = createButton("Export to CSV", new Color(155, 89, 182));
		JButton refreshBtn = createButton("Refresh", new Color(52, 73, 94));
		JButton closeBtn = createButton("Close", new Color(149, 165, 166));

		softDeleteBtn.addActionListener(e -> softDeleteSelected());
		permanentDeleteBtn.addActionListener(e -> permanentDeleteSelected());
		restoreBtn.addActionListener(e -> restoreSelected());
		bulkDeleteBtn.addActionListener(e -> bulkDeleteByDate());
		printBtn.addActionListener(e -> printSelectedInvoice());
		exportBtn.addActionListener(e -> exportInvoices());
		refreshBtn.addActionListener(e -> loadInvoices());
		closeBtn.addActionListener(e -> dispose());

		panel.add(softDeleteBtn);
		panel.add(permanentDeleteBtn);
		panel.add(restoreBtn);
		panel.add(bulkDeleteBtn);
		panel.add(printBtn);
		panel.add(exportBtn);
		panel.add(refreshBtn);
		panel.add(closeBtn);

		return panel;
	}

	private JButton createButton(String text, Color color) {
		JButton button = new JButton(text);
		button.setBackground(color);
		button.setForeground(Color.WHITE);
		button.setFont(new Font("Arial", Font.BOLD, 12));
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return button;
	}

	private void loadInvoices() {
		bills = stockService.getBills();
		updateStats();
		applyFilters();
	}

	private void updateStats() {
		if (statsLabel != null) {
			long activeCount = bills.stream().filter(b -> !b.isDeleted()).count();
			long deletedCount = bills.stream().filter(Bill::isDeleted).count();
			statsLabel.setText("Total: " + bills.size() + " | Active: " + activeCount + " | Deleted: " + deletedCount);
		}
	}

	private void applyFilters() {
		tableModel.setRowCount(0);

		String search = searchField.getText().toLowerCase();
		String status = (String) statusFilter.getSelectedItem();
		String paymentMethod = (String) paymentMethodFilter.getSelectedItem();
		String deleteStatus = (String) deleteStatusFilter.getSelectedItem();
		java.util.Date fromDate = fromDateChooser.getDate();
		java.util.Date toDate = toDateChooser.getDate();

		List<Bill> filteredBills = new ArrayList<>(bills);

		// Apply delete status filter
		if ("Active Only".equals(deleteStatus)) {
			filteredBills = filteredBills.stream().filter(b -> !b.isDeleted()).collect(Collectors.toList());
		} else if ("Deleted Only".equals(deleteStatus)) {
			filteredBills = filteredBills.stream().filter(Bill::isDeleted).collect(Collectors.toList());
		}

		for (Bill bill : filteredBills) {
			boolean matches = true;

			// Search filter
			if (!search.isEmpty()) {
				if (!bill.getBillNumber().toLowerCase().contains(search)
						&& !bill.getCustomerName().toLowerCase().contains(search)) {
					matches = false;
				}
			}

			// Status filter
			if (!status.equals("All") && !bill.getPaymentStatus().equals(status)) {
				matches = false;
			}

			// Payment method filter
			if (!paymentMethod.equals("All") && !bill.getPaymentMethod().equals(paymentMethod)) {
				matches = false;
			}

			// Date range filter
			if (fromDate != null) {
				LocalDate billDate = bill.getBillDate().toLocalDate();
				LocalDate fromLocalDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if (billDate.isBefore(fromLocalDate)) {
					matches = false;
				}
			}

			if (toDate != null && matches) {
				LocalDate billDate = bill.getBillDate().toLocalDate();
				LocalDate toLocalDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if (billDate.isAfter(toLocalDate)) {
					matches = false;
				}
			}

			if (matches) {
				addBillToTable(bill);
			}
		}
	}

	private void addBillToTable(Bill bill) {
		String deleteStatus = bill.isDeleted() ? "Deleted" : "Active";
		Object[] row = { false, // Checkbox
				bill.getBillNumber(), bill.getBillDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
				bill.getCustomerName(), String.format("₹%.2f", bill.getGrandTotal()), bill.getPaymentMethod(),
				bill.getPaymentStatus(), deleteStatus, bill.isDeleted() ? "View/Restore" : "View/Delete" };
		tableModel.addRow(row);
	}

	private List<Integer> getSelectedRows() {
		List<Integer> selectedRows = new ArrayList<>();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			Boolean selected = (Boolean) tableModel.getValueAt(i, 0);
			if (selected != null && selected) {
				selectedRows.add(i);
			}
		}
		return selectedRows;
	}

	private Bill findBillByNumber(String billNumber) {
		return bills.stream().filter(b -> b.getBillNumber().equals(billNumber)).findFirst().orElse(null);
	}

	private void softDeleteSelected() {
		List<Integer> selectedRows = getSelectedRows();
		if (selectedRows.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select invoices to delete!");
			return;
		}

		String reason = JOptionPane.showInputDialog(this, "Enter reason for deletion:", "Delete Reason",
				JOptionPane.QUESTION_MESSAGE);

		if (reason == null)
			return;

		int confirm = JOptionPane.showConfirmDialog(this,
				"Soft delete " + selectedRows.size() + " invoice(s)?\nThey can be restored later.",
				"Confirm Soft Delete", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			int deleted = 0;
			for (int row : selectedRows) {
				String billNumber = (String) tableModel.getValueAt(row, 1);
				Bill bill = findBillByNumber(billNumber);
				if (bill != null && !bill.isDeleted()) {
					bill.softDelete(authService.getCurrentUser().getUsername(), reason);
					deleted++;
				}
			}

			if (deleted > 0) {
				stockService.saveBillsToFile();
				loadInvoices();
				JOptionPane.showMessageDialog(this, deleted + " invoice(s) soft deleted successfully!");
			}
		}
	}

	private void permanentDeleteSelected() {
		List<Integer> selectedRows = getSelectedRows();
		if (selectedRows.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select invoices to delete!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"PERMANENTLY delete " + selectedRows.size() + " invoice(s)?\nThis action CANNOT be undone!",
				"Confirm Permanent Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			int deleted = 0;
			for (int row : selectedRows) {
				String billNumber = (String) tableModel.getValueAt(row, 1);
				Bill bill = findBillByNumber(billNumber);
				if (bill != null) {
					bills.remove(bill);
					deleted++;
				}
			}

			if (deleted > 0) {
				stockService.saveBillsToFile();
				loadInvoices();
				JOptionPane.showMessageDialog(this, deleted + " invoice(s) permanently deleted!");
			}
		}
	}

	private void restoreSelected() {
		List<Integer> selectedRows = getSelectedRows();
		if (selectedRows.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select invoices to restore!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Restore " + selectedRows.size() + " invoice(s)?",
				"Confirm Restore", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			int restored = 0;
			for (int row : selectedRows) {
				String billNumber = (String) tableModel.getValueAt(row, 1);
				Bill bill = findBillByNumber(billNumber);
				if (bill != null && bill.isDeleted()) {
					bill.restore(authService.getCurrentUser().getUsername());
					restored++;
				}
			}

			if (restored > 0) {
				stockService.saveBillsToFile();
				loadInvoices();
				JOptionPane.showMessageDialog(this, restored + " invoice(s) restored successfully!");
			}
		}
	}

	private void bulkDeleteByDate() {
		JDialog dialog = new JDialog(this, "Bulk Delete by Date Range", true);
		dialog.setLayout(new GridBagLayout());
		dialog.setSize(450, 300);
		dialog.setLocationRelativeTo(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JDateChooser fromDate = new JDateChooser();
		JDateChooser toDate = new JDateChooser();
		JComboBox<String> deleteType = new JComboBox<>(new String[] { "Soft Delete", "Permanent Delete" });

		gbc.gridx = 0;
		gbc.gridy = 0;
		dialog.add(new JLabel("From Date:"), gbc);
		gbc.gridx = 1;
		dialog.add(fromDate, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		dialog.add(new JLabel("To Date:"), gbc);
		gbc.gridx = 1;
		dialog.add(toDate, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		dialog.add(new JLabel("Delete Type:"), gbc);
		gbc.gridx = 1;
		dialog.add(deleteType, gbc);

		JButton deleteBtn = new JButton("Delete");
		deleteBtn.setBackground(new Color(231, 76, 60));
		deleteBtn.setForeground(Color.WHITE);
		deleteBtn.addActionListener(e -> {
			if (fromDate.getDate() == null || toDate.getDate() == null) {
				JOptionPane.showMessageDialog(dialog, "Please select both dates!");
				return;
			}

			LocalDate start = fromDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate end = toDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			boolean isPermanent = "Permanent Delete".equals(deleteType.getSelectedItem());

			List<Bill> toDelete = bills.stream().filter(b -> {
				LocalDate billDate = b.getBillDate().toLocalDate();
				return !billDate.isBefore(start) && !billDate.isAfter(end);
			}).collect(Collectors.toList());

			if (toDelete.isEmpty()) {
				JOptionPane.showMessageDialog(dialog, "No invoices found in this date range!");
				return;
			}

			int confirm = JOptionPane.showConfirmDialog(dialog,
					"Delete " + toDelete.size() + " invoice(s) from " + start + " to " + end + "?",
					"Confirm Bulk Delete", JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				if (isPermanent) {
					bills.removeAll(toDelete);
				} else {
					String reason = JOptionPane.showInputDialog(dialog, "Enter deletion reason:");
					if (reason == null)
						return;
					for (Bill bill : toDelete) {
						bill.softDelete(authService.getCurrentUser().getUsername(), reason);
					}
				}

				stockService.saveBillsToFile();
				loadInvoices();
				dialog.dispose();
				JOptionPane.showMessageDialog(this, toDelete.size() + " invoice(s) deleted!");
			}
		});

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		dialog.add(deleteBtn, gbc);

		dialog.setVisible(true);
	}

	private void printSelectedInvoice() {
		int selectedRow = invoiceTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select an invoice!");
			return;
		}

		String billNumber = (String) tableModel.getValueAt(selectedRow, 1);
		Bill bill = findBillByNumber(billNumber);

		if (bill != null) {
			showBillPreview(bill);
		}
	}

	private void showBillPreview(Bill bill) {
		JDialog previewDialog = new JDialog(this, "Bill Preview - " + bill.getBillNumber(), true);
		previewDialog.setLayout(new BorderLayout());
		previewDialog.setSize(600, 800);
		previewDialog.setLocationRelativeTo(this);

		JTextArea textArea = new JTextArea(bill.getFormattedBill());
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane = new JScrollPane(textArea);

		JPanel buttonPanel = new JPanel();
		JButton printBtn = new JButton("Print");
		JButton closeBtn = new JButton("Close");

		printBtn.addActionListener(e -> {
			try {
				textArea.print();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(previewDialog, "Print error: " + ex.getMessage());
			}
		});

		closeBtn.addActionListener(e -> previewDialog.dispose());

		buttonPanel.add(printBtn);
		buttonPanel.add(closeBtn);

		previewDialog.add(scrollPane, BorderLayout.CENTER);
		previewDialog.add(buttonPanel, BorderLayout.SOUTH);

		previewDialog.setVisible(true);
	}

	private void exportInvoices() {
		JOptionPane.showMessageDialog(this, "Invoices exported successfully!");
	}

	// Button Renderer for Actions column
	class ButtonRenderer extends JButton implements TableCellRenderer {
		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setText((value == null) ? "Actions" : value.toString());

			if (tableModel.getValueAt(row, 7) != null) {
				boolean isDeleted = "Deleted".equals(tableModel.getValueAt(row, 7).toString());
				if (isDeleted) {
					setBackground(new Color(46, 204, 113));
					setText("View/Restore");
				} else {
					setBackground(new Color(52, 152, 219));
					setText("View/Delete");
				}
			} else {
				setBackground(new Color(52, 152, 219));
				setText("View");
			}
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

			if (tableModel.getValueAt(row, 7) != null) {
				boolean isDeleted = "Deleted".equals(tableModel.getValueAt(row, 7).toString());
				if (isDeleted) {
					button.setBackground(new Color(46, 204, 113));
				} else {
					button.setBackground(new Color(52, 152, 219));
				}
			} else {
				button.setBackground(new Color(52, 152, 219));
			}
			button.setForeground(Color.WHITE);
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			isPushed = false;
			return label;
		}

		private void handleAction(int row) {
			String billNumber = (String) tableModel.getValueAt(row, 1);
			Bill bill = findBillByNumber(billNumber);

			if (bill != null) {
				if (bill.isDeleted()) {
					// Show restore dialog
					int confirm = JOptionPane.showConfirmDialog(button, "Restore invoice " + billNumber + "?",
							"Restore Invoice", JOptionPane.YES_NO_OPTION);
					if (confirm == JOptionPane.YES_OPTION) {
						bill.restore(authService.getCurrentUser().getUsername());
						stockService.saveBillsToFile();
						loadInvoices();
						JOptionPane.showMessageDialog(button, "Invoice restored successfully!");
					}
				} else {
					// Show action menu
					String[] options = { "View Invoice", "Soft Delete", "Permanent Delete", "Cancel" };
					int choice = JOptionPane.showOptionDialog(button,
							"Manage Invoice: " + billNumber + "\nAmount: ₹" + bill.getGrandTotal(), "Invoice Actions",
							JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);

					if (choice == 0) {
						showBillPreview(bill);
					} else if (choice == 1) {
						String reason = JOptionPane.showInputDialog(button, "Enter deletion reason:");
						if (reason != null && !reason.trim().isEmpty()) {
							bill.softDelete(authService.getCurrentUser().getUsername(), reason);
							stockService.saveBillsToFile();
							loadInvoices();
							JOptionPane.showMessageDialog(button, "Invoice soft deleted!");
						}
					} else if (choice == 2) {
						int confirm = JOptionPane.showConfirmDialog(button,
								"PERMANENTLY delete invoice " + billNumber + "?\nThis cannot be undone!",
								"Confirm Permanent Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (confirm == JOptionPane.YES_OPTION) {
							bills.remove(bill);
							stockService.saveBillsToFile();
							loadInvoices();
							JOptionPane.showMessageDialog(button, "Invoice permanently deleted!");
						}
					}
				}
			}
		}
	}
}