package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Product implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String barcode;
	private String name;
	private String categoryId;
	private int quantity;
	private int minStockLevel;
	private int maxStockLevel;
	private double costPrice;
	double sellPrice;
	private double mrp;
	private String brand;
	private String supplier;
	private String location; // Warehouse location
	private String unit; // Piece, kg, liter, etc.
	private double weight;
	private String description;
	private LocalDateTime addedDate;
	private LocalDateTime lastUpdated;
	private boolean active;
	private String addedBy;
	private String updatedBy;

	public Product(String id, String barcode, String name, String categoryId, int quantity, int minStockLevel,
			int maxStockLevel, double costPrice, double sellPrice, double mrp, String brand, String supplier,
			String location, String unit, double weight, String description, String addedBy) {
		this.id = id;
		this.barcode = barcode;
		this.name = name;
		this.categoryId = categoryId;
		this.quantity = quantity;
		this.minStockLevel = minStockLevel;
		this.maxStockLevel = maxStockLevel;
		this.costPrice = costPrice;
		this.sellPrice = sellPrice;
		this.mrp = mrp;
		this.brand = brand;
		this.supplier = supplier;
		this.location = location;
		this.unit = unit;
		this.weight = weight;
		this.description = description;
		this.addedDate = LocalDateTime.now();
		this.lastUpdated = LocalDateTime.now();
		this.active = true;
		this.addedBy = addedBy;
		this.updatedBy = addedBy;
	}

	public void setAddedDate(LocalDateTime addedDate) {
		this.addedDate = addedDate;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	// Constructor for backward compatibility
	public Product(String id, String barcode, String name, String categoryId, int quantity, int minStockLevel,
			double costPrice, double sellPrice, double mrp, String brand, String supplier) {
		this(id, barcode, name, categoryId, quantity, minStockLevel, minStockLevel * 10, costPrice, sellPrice, mrp,
				brand, supplier, "Default", "Pcs", 0.0, "", "System");
	}

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
		this.lastUpdated = LocalDateTime.now();
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		this.lastUpdated = LocalDateTime.now();
	}

	public int getMinStockLevel() {
		return minStockLevel;
	}

	public void setMinStockLevel(int minStockLevel) {
		this.minStockLevel = minStockLevel;
		this.lastUpdated = LocalDateTime.now();
	}

	public int getMaxStockLevel() {
		return maxStockLevel;
	}

	public void setMaxStockLevel(int maxStockLevel) {
		this.maxStockLevel = maxStockLevel;
		this.lastUpdated = LocalDateTime.now();
	}

	public double getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(double costPrice) {
		this.costPrice = costPrice;
		this.lastUpdated = LocalDateTime.now();
	}

	public double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
		this.lastUpdated = LocalDateTime.now();
	}

	public double getMrp() {
		return mrp;
	}

	public void setMrp(double mrp) {
		this.mrp = mrp;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
		this.lastUpdated = LocalDateTime.now();
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.lastUpdated = LocalDateTime.now();
	}

	public LocalDateTime getAddedDate() {
		return addedDate;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getAddedBy() {
		return addedBy;
	}

	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
		this.lastUpdated = LocalDateTime.now();
	}

	public double getTotalValue() {
		return quantity * costPrice;
	}

	public double getProfit() {
		return (sellPrice - costPrice) * quantity;
	}

	public double getProfitMargin() {
		if (costPrice > 0) {
			return ((sellPrice - costPrice) / costPrice) * 100;
		}
		return 0;
	}

	public boolean isLowStock() {
		return quantity <= minStockLevel;
	}

	public boolean isOverStock() {
		return quantity >= maxStockLevel;
	}

	public double getDiscountPercent() {
		if (mrp > 0) {
			return ((mrp - sellPrice) / mrp) * 100;
		}
		return 0;
	}

	public String getFormattedAddedDate() {
		if (addedDate == null) {
			return "N/A";
		}
		return addedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}

	public String getFormattedLastUpdated() {
		if (lastUpdated == null) {
			return "N/A";
		}

		return lastUpdated.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}

	@Override
	public String toString() {
		return String.format("%s - %s (Stock: %d%s, Price: ₹%.2f)", barcode, name, quantity, unit, sellPrice);
	}
}