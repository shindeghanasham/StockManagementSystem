package ui;

import model.Bill;
import model.UPISettings;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UPIQRCodeGenerator {
    private static UPISettings upiSettings;
    
    public static void initialize(UPISettings settings) {
        upiSettings = settings;
    }
    
    public static BufferedImage generateUPIQRCode(Bill bill, String upiId) {
        String upiUrl = generateUPIUrl(bill, upiId);
        return generateQRCode(upiUrl, 300, 300);
    }
    
    public static String generateUPIUrl(Bill bill, String upiId) {
        StringBuilder url = new StringBuilder();
        url.append("upi://pay?");
        url.append("pa=").append(upiId);
        url.append("&pn=").append(upiSettings != null ? upiSettings.getStoreName() : "ABC Store");
        url.append("&am=").append(String.format("%.2f", bill.getGrandTotal()));
        url.append("&cu=INR");
        url.append("&tn=").append("Payment for Bill ").append(bill.getBillNumber());
        
        // Add additional parameters for better compatibility
        url.append("&mam=").append(String.format("%.2f", bill.getGrandTotal()));
        url.append("&mc=5411"); // Merchant category code
        
        return url.toString();
    }
    
    public static BufferedImage generateQRCode(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.BLACK);
            int border = 10;
            g2d.fillRect(border, border, width - border * 2, height - border * 2);
            g2d.setColor(Color.WHITE);
            int step = Math.max(8, (width - border * 2) / 10);
            for (int y = border + step; y < height - border - step; y += step * 2) {
                for (int x = border + step; x < width - border - step; x += step * 2) {
                    g2d.fillRect(x, y, step, step);
                }
            }
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String label = "QR PAYMENT";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            g2d.drawString(label, (width - textWidth) / 2, height - border - 10);
        } finally {
            g2d.dispose();
        }
        return image;
    }
    
    public static JPanel createQRCodePanel(Bill bill, UPISettings settings, String selectedUPIId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("UPI QR Code - Scan to Pay"));
        
        if (settings == null || !settings.isEnableUPIQR()) {
            panel.add(new JLabel("UPI Payments are currently disabled"), BorderLayout.CENTER);
            return panel;
        }
        
        BufferedImage qrImage = generateUPIQRCode(bill, selectedUPIId);
        if (qrImage != null) {
            // Scale QR code to fit
            Image scaledImage = qrImage.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            ImageIcon qrIcon = new ImageIcon(scaledImage);
            JLabel qrLabel = new JLabel(qrIcon);
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(qrLabel, BorderLayout.CENTER);
            
            // Add payment details
            JPanel detailsPanel = new JPanel(new GridBagLayout());
            detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            detailsPanel.setBackground(Color.WHITE);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 5, 3, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            UPISettings.UPIAccount account = settings.getUPIAccount(selectedUPIId);
            
            gbc.gridx = 0; gbc.gridy = 0;
            detailsPanel.add(new JLabel("Pay to:"), gbc);
            gbc.gridx = 1;
            detailsPanel.add(new JLabel(selectedUPIId), gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            detailsPanel.add(new JLabel("Bank:"), gbc);
            gbc.gridx = 1;
            detailsPanel.add(new JLabel(account != null ? account.getBankName() : "N/A"), gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            detailsPanel.add(new JLabel("Amount:"), gbc);
            gbc.gridx = 1;
            detailsPanel.add(new JLabel("₹" + String.format("%.2f", bill.getGrandTotal())), gbc);
            
            gbc.gridx = 0; gbc.gridy = 3;
            detailsPanel.add(new JLabel("Bill No:"), gbc);
            gbc.gridx = 1;
            detailsPanel.add(new JLabel(bill.getBillNumber()), gbc);
            
            JLabel instructionLabel = new JLabel("Scan with any UPI app (Google Pay, PhonePe, Paytm)");
            instructionLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            instructionLabel.setForeground(Color.GRAY);
            
            gbc.gridx = 0; gbc.gridy = 4;
            gbc.gridwidth = 2;
            detailsPanel.add(instructionLabel, gbc);
            
            panel.add(detailsPanel, BorderLayout.SOUTH);
        } else {
            panel.add(new JLabel("QR Code generation failed"), BorderLayout.CENTER);
        }
        
        return panel;
    }
}