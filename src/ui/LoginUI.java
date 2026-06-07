package ui;

import service.AuthService;
import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AuthService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginUI() {
        authService = new AuthService();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Stock Management System - Login");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(70, 130, 180));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Stock Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginBtn = createButton("Login", new Color(46, 204, 113));
        JButton exitBtn = createButton("Exit", new Color(231, 76, 60));
        
        loginBtn.addActionListener(e -> performLogin());
        exitBtn.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(exitBtn);
        
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        
        // Demo credentials label
        JLabel demoLabel = new JLabel("Demo Credentials: admin/admin123 or user/user123");
        demoLabel.setForeground(Color.YELLOW);
        demoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        gbc.gridy = 4;
        mainPanel.add(demoLabel, gbc);
        
        add(mainPanel);
        
        // Enter key to login
        getRootPane().setDefaultButton(loginBtn);
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }
        
        if (authService.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Welcome " + authService.getCurrentUser().getFullName() + "!");
            
            // Create StockManagementUI and set current user
            StockManagementUI stockUI = new StockManagementUI(authService);
            stockUI.setCurrentUser(username); // Add this method to StockManagementUI
            stockUI.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password!", 
                                        "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}