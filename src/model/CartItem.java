package model;

import java.io.Serializable;

public class CartItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Product product;
	private int quantity;
	private double sellingPrice;
	private double discountPercent;
	private double discountAmount;
	private double subtotal;
	private double gst;
	private double total;

	public CartItem(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
		this.sellingPrice = product.getSellPrice();
		this.discountPercent = 0;
		calculateTotals();
	}

	public void calculateTotals() {
		this.subtotal = sellingPrice * quantity;

		// Apply discount if any
		if (discountPercent > 0) {
			this.discountAmount = subtotal * (discountPercent / 100);
			this.subtotal = subtotal - discountAmount;
		}

		this.gst = subtotal * 0.18; // 18% GST
		this.total = subtotal + gst;
	}

	// Getters and Setters
	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		calculateTotals();
	}

	public double getSellingPrice() {
		return sellingPrice;
	}

	public void setSellingPrice(double sellingPrice) {
		this.sellingPrice = sellingPrice;
		calculateTotals();
	}

	public double getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(double discountPercent) {
		this.discountPercent = discountPercent;
		calculateTotals();
	}

	public double getSubtotal() {
		return subtotal;
	}

	public double getGst() {
		return gst;
	}

	public double getTotal() {
		return total;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}
}