package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bill implements Serializable {
	private static final long serialVersionUID = 1L;

	private String billNumber;
	private LocalDateTime billDate;
	private List<CartItem> items;
	private double subtotal;
	private double totalDiscount;
	private double totalGst;
	private double grandTotal;
	private double roundOff;
	private double paidAmount;
	private double balanceAmount;
	private String paymentMethod;
	private String customerName;
	private String customerPhone;
	private String customerEmail;
	private String soldBy;
	private String notes;
	private String upiTransactionId;
	private String paymentStatus; // PAID, PENDING, FAILED

	// New fields for deletion management
	private boolean isDeleted;
	private LocalDateTime deletedDate;
	private String deletedBy;
	private String deleteReason;
	private LocalDateTime restoredDate;
	private String restoredBy;

	public Bill() {
		this.billNumber = generateBillNumber();
		this.billDate = LocalDateTime.now();
		this.items = new ArrayList<>();
		this.paymentMethod = "CASH";
		this.customerName = "Walk-in Customer";
		this.paymentStatus = "PAID";
		this.isDeleted = false;
	}

	private String generateBillNumber() {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		return "INV-" + date + "-" + random;
	}

	public void addItem(CartItem item) {
		items.add(item);
		calculateTotals();
	}

	public void removeItem(int index) {
		items.remove(index);
		calculateTotals();
	}

	public void calculateTotals() {
		subtotal = 0;
		totalDiscount = 0;
		totalGst = 0;

		for (CartItem item : items) {
			subtotal += item.getSubtotal() + item.getDiscountAmount();
			totalDiscount += item.getDiscountAmount();
			totalGst += item.getGst();
		}

		grandTotal = subtotal - totalDiscount + totalGst;
		roundOff = Math.round(grandTotal) - grandTotal;
		grandTotal = Math.round(grandTotal);
	}

	public void softDelete(String deletedBy, String reason) {
		this.isDeleted = true;
		this.deletedDate = LocalDateTime.now();
		this.deletedBy = deletedBy;
		this.deleteReason = reason;
	}

	public void restore(String restoredBy) {
		this.isDeleted = false;
		this.restoredDate = LocalDateTime.now();
		this.restoredBy = restoredBy;
	}

	public String getFormattedBill() {
		if (isDeleted) {
			return "[DELETED] " + getFormattedBillContent();
		}
		return getFormattedBillContent();
	}

	private String getFormattedBillContent() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n").append("=".repeat(60)).append("\n");
		sb.append(String.format("%40s\n", "ABC STORE"));
		sb.append(String.format("%38s\n", "GST BILL"));
		sb.append("=".repeat(60)).append("\n");
		sb.append(String.format("Bill No: %s\n", billNumber));
		sb.append(String.format("Date: %s\n", billDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
		sb.append(String.format("Customer: %s\n", customerName));
		if (customerPhone != null && !customerPhone.isEmpty()) {
			sb.append(String.format("Phone: %s\n", customerPhone));
		}
		sb.append(String.format("Cashier: %s\n", soldBy));
		sb.append("-".repeat(60)).append("\n");
		sb.append(String.format("%-4s %-25s %8s %10s %10s\n", "Sl", "Item", "Qty", "Price", "Total"));
		sb.append("-".repeat(60)).append("\n");

		int sl = 1;
		for (CartItem item : items) {
			sb.append(String.format("%-4d %-25s %8d %10.2f %10.2f\n", sl++, truncate(item.getProduct().getName(), 25),
					item.getQuantity(), item.getSellingPrice(), item.getSubtotal() + item.getDiscountAmount()));

			if (item.getDiscountPercent() > 0) {
				sb.append(String.format("%-4s    Disc: %.0f%% (₹%.2f)\n", "", item.getDiscountPercent(),
						item.getDiscountAmount()));
			}
		}

		sb.append("-".repeat(60)).append("\n");
		sb.append(String.format("%-44s %10.2f\n", "Subtotal:", subtotal));
		if (totalDiscount > 0) {
			sb.append(String.format("%-44s %10.2f\n", "Discount:", -totalDiscount));
		}
		sb.append(String.format("%-44s %10.2f\n", "GST (18%):", totalGst));
		if (roundOff != 0) {
			sb.append(String.format("%-44s %10.2f\n", "Round Off:", roundOff));
		}
		sb.append("=".repeat(60)).append("\n");
		sb.append(String.format("%-44s %10.2f\n", "GRAND TOTAL:", grandTotal));
		sb.append(String.format("%-44s %10.2f\n", "Paid:", paidAmount));
		sb.append(String.format("%-44s %10.2f\n", "Balance:", balanceAmount));
		sb.append(String.format("Payment: %s\n", paymentMethod));
		if (upiTransactionId != null && !upiTransactionId.isEmpty()) {
			sb.append(String.format("UPI TXN ID: %s\n", upiTransactionId));
		}
		sb.append(String.format("Status: %s\n", paymentStatus));

		if (isDeleted) {
			sb.append("-".repeat(60)).append("\n");
			sb.append(String.format("DELETED ON: %s\n",
					deletedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
			sb.append(String.format("DELETED BY: %s\n", deletedBy));
			sb.append(String.format("REASON: %s\n", deleteReason));
		}

		sb.append("=".repeat(60)).append("\n");
		sb.append(String.format("%40s\n", "Thank you! Visit Again!"));
		sb.append(String.format("%38s\n", "** GST Bill **"));
		sb.append("=".repeat(60)).append("\n");

		return sb.toString();
	}

	private String truncate(String str, int length) {
		if (str.length() > length) {
			return str.substring(0, length - 3) + "...";
		}
		return str;
	}

	// Getters and Setters
	public String getBillNumber() {
		return billNumber;
	}

	public LocalDateTime getBillDate() {
		return billDate;
	}

	public List<CartItem> getItems() {
		return items;
	}

	public double getSubtotal() {
		return subtotal;
	}

	public double getTotalDiscount() {
		return totalDiscount;
	}

	public double getTotalGst() {
		return totalGst;
	}

	public double getGrandTotal() {
		return grandTotal;
	}

	public double getRoundOff() {
		return roundOff;
	}

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
		this.balanceAmount = paidAmount - grandTotal;
		if (balanceAmount >= 0) {
			this.paymentStatus = "PAID";
		}
	}

	public double getBalanceAmount() {
		return balanceAmount;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerPhone() {
		return customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getSoldBy() {
		return soldBy;
	}

	public void setSoldBy(String soldBy) {
		this.soldBy = soldBy;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getUpiTransactionId() {
		return upiTransactionId;
	}

	public void setUpiTransactionId(String upiTransactionId) {
		this.upiTransactionId = upiTransactionId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	// Delete-related getters
	public boolean isDeleted() {
		return isDeleted;
	}

	public LocalDateTime getDeletedDate() {
		return deletedDate;
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	public LocalDateTime getRestoredDate() {
		return restoredDate;
	}

	public String getRestoredBy() {
		return restoredBy;
	}
}