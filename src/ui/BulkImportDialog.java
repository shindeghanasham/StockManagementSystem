package ui;

import service.BulkImportService;
import service.StockService;
import service.AuthService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class BulkImportDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StockService stockService;
	private AuthService authService;
	private BulkImportService bulkImportService;
	private JLabel fileLabel;
	private JTextArea logArea;
	private JProgressBar progressBar;
	private JButton importButton;
	private JButton browseButton;
	private JButton templateButton;
	private File selectedFile;
	private JLabel statusLabel;

	public BulkImportDialog(JFrame parent, StockService stockService, AuthService authService) {
		super(parent, "Bulk Product Import", true);
		this.stockService = stockService;
		this.authService = authService;
		this.bulkImportService = new BulkImportService(stockService, authService);

		initializeUI();
		setSize(800, 600);
		setLocationRelativeTo(parent);
	}

	private void initializeUI() {
		setLayout(new BorderLayout());

		// Header Panel
		JPanel headerPanel = createHeaderPanel();

		// File Selection Panel
		JPanel filePanel = createFileSelectionPanel();

		// Progress Panel
		JPanel progressPanel = createProgressPanel();

		// Log Panel
		JPanel logPanel = createLogPanel();

		// Button Panel
		JPanel buttonPanel = createButtonPanel();

		add(headerPanel, BorderLayout.NORTH);
		add(filePanel, BorderLayout.CENTER);
		add(progressPanel, BorderLayout.SOUTH);
		add(logPanel, BorderLayout.EAST);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(52, 152, 219));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

		JLabel titleLabel = new JLabel("Bulk Product Import");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
		titleLabel.setForeground(Color.WHITE);

		JLabel subtitleLabel = new JLabel("Import multiple products from CSV file");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		subtitleLabel.setForeground(Color.WHITE);

		JPanel textPanel = new JPanel(new GridLayout(2, 1));
		textPanel.setOpaque(false);
		textPanel.add(titleLabel);
		textPanel.add(subtitleLabel);

		panel.add(textPanel, BorderLayout.WEST);

		return panel;
	}

	private JPanel createFileSelectionPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Select CSV File"));
		panel.setBackground(Color.WHITE);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// File selection
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("CSV File:"), gbc);

		gbc.gridx = 1;
		fileLabel = new JLabel("No file selected");
		fileLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		fileLabel.setPreferredSize(new Dimension(300, 25));
		panel.add(fileLabel, gbc);

		gbc.gridx = 2;
		browseButton = new JButton("Browse");
		browseButton.setBackground(new Color(52, 152, 219));
		browseButton.setForeground(Color.WHITE);
		browseButton.addActionListener(e -> browseFile());
		panel.add(browseButton, gbc);

		// Template download
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Need template?"), gbc);

		gbc.gridx = 1;
		templateButton = new JButton("Download CSV Template");
		templateButton.setBackground(new Color(46, 204, 113));
		templateButton.setForeground(Color.WHITE);
		templateButton.addActionListener(e -> downloadTemplate());
		panel.add(templateButton, gbc);

		// File info
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("File Requirements"));
		infoPanel.setLayout(new GridLayout(4, 1, 5, 5));
		infoPanel.add(new JLabel("• Supported formats: .csv"));
		infoPanel.add(new JLabel("• Max file size: 10MB"));
		infoPanel.add(new JLabel("• First row must contain headers"));
		infoPanel.add(new JLabel("• Required fields: Product ID, Name, Category, Quantity, Sell Price"));

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		panel.add(infoPanel, gbc);

		return panel;
	}

	private JPanel createProgressPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Import Progress"));
		panel.setPreferredSize(new Dimension(400, 150));

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setForeground(new Color(46, 204, 113));

		statusLabel = new JLabel("Ready to import");
		statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));

		panel.add(progressBar, BorderLayout.CENTER);
		panel.add(statusLabel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createLogPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Import Log"));
		panel.setPreferredSize(new Dimension(400, 300));

		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

		JScrollPane scrollPane = new JScrollPane(logArea);
		panel.add(scrollPane, BorderLayout.CENTER);

		JButton clearLogBtn = new JButton("Clear Log");
		clearLogBtn.addActionListener(e -> logArea.setText(""));
		panel.add(clearLogBtn, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		importButton = new JButton("Start Import");
		importButton.setBackground(new Color(46, 204, 113));
		importButton.setForeground(Color.WHITE);
		importButton.setFont(new Font("Arial", Font.BOLD, 14));
		importButton.setEnabled(false);
		importButton.addActionListener(e -> startImport());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBackground(new Color(231, 76, 60));
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
		cancelButton.addActionListener(e -> dispose());

		panel.add(importButton);
		panel.add(cancelButton);

		return panel;
	}

	private void browseFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select CSV File");
		fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();

			// Check file size (max 10MB)
			if (selectedFile.length() > 10 * 1024 * 1024) {
				JOptionPane.showMessageDialog(this, "File size exceeds 10MB limit!");
				selectedFile = null;
				fileLabel.setText("No file selected");
				importButton.setEnabled(false);
				return;
			}

			fileLabel.setText(selectedFile.getName());
			importButton.setEnabled(true);
			logArea.append("File selected: " + selectedFile.getAbsolutePath() + "\n");
		}
	}

	private void downloadTemplate() {
		SwingWorker<File, Void> worker = new SwingWorker<File, Void>() {
			@Override
			protected File doInBackground() {
				statusLabel.setText("Generating template...");
				return bulkImportService.createImportTemplate();
			}

			@Override
			protected void done() {
				try {
					File template = get();
					if (template != null) {
						JFileChooser fileChooser = new JFileChooser();
					fileChooser.setSelectedFile(new File("product_import_template.csv"));
					int result = fileChooser.showSaveDialog(BulkImportDialog.this);
						if (result == JFileChooser.APPROVE_OPTION) {
							File destFile = fileChooser.getSelectedFile();
							java.nio.file.Files.copy(template.toPath(), destFile.toPath(),
									java.nio.file.StandardCopyOption.REPLACE_EXISTING);
							JOptionPane.showMessageDialog(BulkImportDialog.this,
									"Template downloaded successfully!\nLocation: " + destFile.getAbsolutePath());
						}
						template.delete();
					} else {
						JOptionPane.showMessageDialog(BulkImportDialog.this, "Failed to generate template!");
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(BulkImportDialog.this, "Error: " + e.getMessage());
				}
				statusLabel.setText("Ready to import");
			}
		};
		worker.execute();
	}

	private void startImport() {
		if (selectedFile == null) {
			JOptionPane.showMessageDialog(this, "Please select a file first!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Import products from " + selectedFile.getName() + "?\nThis may take a few moments.", "Confirm Import",
				JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION)
			return;

		SwingWorker<Map<String, Object>, String> worker = new SwingWorker<Map<String, Object>, String>() {
			@Override
			protected Map<String, Object> doInBackground() {
				publish("Starting import...\n");
				progressBar.setIndeterminate(true);
				statusLabel.setText("Importing... Please wait");
				importButton.setEnabled(false);
				browseButton.setEnabled(false);

Map<String, Object> result = bulkImportService.importProductsFromCsv(selectedFile);

				progressBar.setIndeterminate(false);
				return result;
			}

			@Override
			protected void process(List<String> chunks) {
				for (String chunk : chunks) {
					logArea.append(chunk);
				}
			}

			@Override
			protected void done() {
				try {
					Map<String, Object> result = get();
					boolean success = (boolean) result.get("success");
					String message = (String) result.get("message");

					if (success) {
						int successCount = (int) result.get("successCount");
						int failCount = (int) result.get("failCount");
						java.util.List<String> errors = (java.util.List<String>) result.get("errors");

						logArea.append("\n" + "=".repeat(60) + "\n");
						logArea.append("IMPORT SUMMARY\n");
						logArea.append("=".repeat(60) + "\n");
						logArea.append(String.format("Successful: %d\n", successCount));
						logArea.append(String.format("Failed: %d\n", failCount));
						logArea.append(String.format("Total: %d\n", successCount + failCount));

						if (!errors.isEmpty()) {
							logArea.append("\nERRORS:\n");
							logArea.append("-".repeat(60) + "\n");
							for (String error : errors) {
								logArea.append(error + "\n");
							}
						}

						progressBar.setValue(100);

						// Ask to refresh product list
						int refresh = JOptionPane.showConfirmDialog(BulkImportDialog.this,
								message + "\n\nRefresh product list?", "Import Complete", JOptionPane.YES_NO_OPTION);

						if (refresh == JOptionPane.YES_OPTION) {
							// Refresh the parent frame's product table
							Component parent = getParent();
							while (parent != null && !(parent instanceof StockManagementUI)) {
								parent = parent.getParent();
							}
							if (parent instanceof StockManagementUI) {
								((StockManagementUI) parent).loadProductsToTable();
							}
						}

						statusLabel.setText("Import completed!");

					} else {
						JOptionPane.showMessageDialog(BulkImportDialog.this, message, "Import Failed",
								JOptionPane.ERROR_MESSAGE);
						statusLabel.setText("Import failed!");
					}

				} catch (Exception e) {
					JOptionPane.showMessageDialog(BulkImportDialog.this, "Error during import: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					logArea.append("ERROR: " + e.getMessage() + "\n");
					statusLabel.setText("Import failed!");
				} finally {
					importButton.setEnabled(true);
					browseButton.setEnabled(true);
				}
			}
		};

		worker.execute();
	}
}