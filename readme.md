# Stock Management System - Complete Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [System Requirements](#system-requirements)
4. [Installation Guide](#installation-guide)
5. [Project Structure](#project-structure)
6. [How to Use](#how-to-use)
7. [Database Schema](#database-schema)
8. [API Documentation](#api-documentation)
9. [Troubleshooting](#troubleshooting)
10. [License](#license)

## Overview

The **Stock Management System** is a comprehensive, industry-level desktop application built with Java Swing. It provides complete inventory management, POS billing, GST invoicing, user authentication, and advanced reporting features. The system follows MVC architecture and includes persistent storage using file-based serialization.

### Key Highlights
- ✅ Complete Stock Management (CRUD Operations)
- ✅ POS Billing System with GST (18%)
- ✅ UPI QR Code Integration for Digital Payments
- ✅ Excel Import/Export (Bulk Product Upload)
- ✅ User Authentication & Role Management
- ✅ Real-time Dashboard & Analytics
- ✅ Audit Trail for All Transactions
- ✅ Invoice Management with Soft Delete
- ✅ Low Stock Alerts & Notifications

## Features

### 1. Stock Management
- **Add Product**: Complete product details with barcode, brand, supplier
- **Edit Product**: Update all product information
- **Delete Product**: Soft delete and permanent delete options
- **Bulk Import**: Import multiple products via Excel template
- **Stock Transfer**: Transfer stock between products
- **Low Stock Alerts**: Visual indicators and notifications

### 2. Billing System (POS)
- **Shopping Cart**: Add/remove items with quantity adjustment
- **Discount Management**: Percentage-based discounts per item
- **GST Calculation**: Automatic 18% GST calculation
- **Multiple Payment Methods**: Cash, Card, UPI, Credit
- **UPI QR Code**: Dynamic QR generation for digital payments
- **Bill Printing**: Professional invoice printing

### 3. User Management
- **Role-Based Access**: Admin and User roles
- **Authentication**: Secure login system
- **User Administration**: Add/edit/delete users (Admin only)

### 4. Reporting & Analytics
- **Sales Reports**: Daily, weekly, monthly sales analysis
- **Product Statistics**: Total value, profit margins, stock levels
- **Audit Logs**: Complete transaction history
- **Export Options**: Excel and CSV export

### 5. Invoice Management
- **View All Invoices**: Search and filter invoices
- **Soft Delete**: Archive invoices with reason
- **Permanent Delete**: Complete removal
- **Restore Option**: Recover soft-deleted invoices
- **Bulk Operations**: Delete multiple invoices by date range

### 6. Dashboard Features
- **KPI Cards**: Real-time metrics display
- **Stock Health Monitor**: Visual progress bar
- **Sales Charts**: Graphical representation
- **Top Products**: Best-selling items list
- **Recent Activities**: Live activity feed

## System Requirements

### Minimum Requirements
- **OS**: Windows 10/11, macOS, or Linux
- **RAM**: 4GB minimum (8GB recommended)
- **Storage**: 500MB free space
- **Java**: JDK 11 or higher
- **Display**: 1366x768 resolution

### Required Libraries
- **Apache POI** (5.2.3) - Excel processing
- **ZXing Core** (3.5.1) - QR code generation
- **ZXing JavaSE** (3.5.1) - QR code rendering

## Installation Guide

### Step 1: Install Java JDK
```bash
# Check if Java is installed
java -version

# If not installed, download from:
# https://www.oracle.com/java/technologies/downloads/
```

### Step 2: Download Required Libraries

Create a `lib` folder in the project root and download these JARs:

```bash
# Apache POI (Excel Support)
poi-5.2.3.jar
poi-ooxml-5.2.3.jar
poi-ooxml-schemas-4.1.2.jar
xmlbeans-5.1.1.jar
commons-collections4-4.4.jar
commons-compress-1.23.jar

# ZXing (QR Code Support)
core-3.5.1.jar
javase-3.5.1.jar
```

### Step 3: Clone or Download Project
```bash
git clone https://github.com/your-repo/stock-management-system.git
cd stock-management-system
```

### Step 4: Compile the Project

#### Windows (compile.bat)
```batch
@echo off
mkdir bin 2>nul
javac -cp "lib/*;src" -d bin src/model/*.java src/service/*.java src/persistence/*.java src/ui/*.java src/Main.java
if %errorlevel% == 0 (
    echo Compilation Successful!
    java -cp "lib/*;bin" Main
)
```

#### Linux/Mac (compile.sh)
```bash
#!/bin/bash
mkdir -p bin
javac -cp "lib/*:src" -d bin src/model/*.java src/service/*.java src/persistence/*.java src/ui/*.java src/Main.java
if [ $? -eq 0 ]; then
    echo "Compilation Successful!"
    java -cp "lib/*:bin" Main
fi
```

### Step 5: Run the Application
```bash
java -cp "lib/*;bin" Main
```

## Project Structure

```
StockManagementSystem/
│
├── src/
│   ├── model/                 # Data Models
│   │   ├── Product.java       # Product entity
│   │   ├── User.java          # User entity
│   │   ├── Category.java      # Category entity
│   │   ├── Bill.java          # Bill entity
│   │   ├── CartItem.java      # Cart item entity
│   │   └── UPISettings.java   # UPI settings entity
│   │
│   ├── service/               # Business Logic
│   │   ├── StockService.java  # Stock operations
│   │   ├── AuthService.java   # Authentication
│   │   ├── AuditService.java  # Audit logging
│   │   ├── ReportService.java # Reporting
│   │   ├── BulkImportService.java # Excel import
│   │   └── SettingsService.java # System settings
│   │
│   ├── persistence/           # Data Storage
│   │   └── DataStorage.java   # File I/O operations
│   │
│   └── ui/                    # User Interface
│       ├── LoginUI.java       # Login screen
│       ├── StockManagementUI.java # Main dashboard
│       ├── BillingUI.java     # POS billing
│       ├── InvoiceViewerUI.java # Invoice management
│       ├── StatisticsDashboard.java # Analytics
│       ├── AdminDashboard.java # Admin panel
│       ├── BulkImportDialog.java # Bulk import
│       ├── UPISettingsPanel.java # UPI settings
│       ├── ProductCRUDDialog.java # Product management
│       └── UPIQRCodeGenerator.java # QR generation
│
├── lib/                       # External libraries
│   ├── poi-5.2.3.jar
│   ├── core-3.5.1.jar
│   └── javase-3.5.1.jar
│
├── data/                      # Data storage (auto-generated)
│   ├── products.dat           # Product data
│   ├── users.dat              # User data
│   ├── categories.dat         # Category data
│   ├── bills.dat              # Bill data
│   ├── sales.dat              # Sales records
│   ├── audit_log.txt          # Audit logs
│   └── settings.dat           # System settings
│
├── compile.bat                # Windows compilation script
├── compile.sh                 # Linux/Mac compilation script
├── README.md                  # Documentation
└── Main.java                  # Entry point
```

## How to Use

### First Time Login

**Default Credentials:**
```
Admin Access:
Username: admin
Password: admin123

User Access:
Username: user
Password: user123
```

### Main Dashboard Features

#### 1. Product Management
- **Add Product**: Click "Add Product" → Fill details → Save
- **Edit Product**: Select product → Click "Edit Product" → Modify → Update
- **Delete Product**: Select product → Click "Delete Product" → Confirm
- **View Details**: Select product → Click "View Details"
- **Bulk Import**: Admin Panel → Bulk Import → Upload Excel file

#### 2. POS Billing
- Click "POS Billing" button
- Search products and add to cart
- Adjust quantities and apply discounts
- Enter customer details (optional)
- Select payment method (Cash/Card/UPI)
- For UPI: Scan QR code and verify payment
- Generate and print bill

#### 3. Invoice Management
- Click "View Invoices" button
- Use filters to search invoices
- Select invoices using checkboxes
- Choose action: Soft Delete, Permanent Delete, or Restore
- View and print individual invoices

#### 4. Reports & Analytics
- **Statistics Dashboard**: View KPI cards and charts
- **Sales Report**: Menu → Reports → Sales Report
- **Low Stock Report**: Menu → Reports → Low Stock Report
- **Audit Report**: Menu → Reports → Audit Report

#### 5. Admin Functions
- **User Management**: Add/edit/delete users
- **UPI Settings**: Configure UPI accounts for payments
- **Bulk Import**: Mass upload products via Excel
- **System Settings**: Configure application parameters

### Excel Import Template Format

Create an Excel file with these columns:

| Column | Required | Description |
|--------|----------|-------------|
| Product ID* | Yes | Unique identifier |
| Barcode | No | Product barcode |
| Product Name* | Yes | Name of product |
| Category* | Yes | Category name |
| Quantity* | Yes | Initial stock |
| Min Stock Level | No | Alert threshold (default: 10) |
| Max Stock Level | No | Maximum stock (default: Min*10) |
| Cost Price | No | Purchase price |
| Sell Price* | Yes | Selling price |
| MRP | No | Maximum retail price |
| Brand | No | Product brand |
| Supplier | No | Supplier name |
| Location | No | Storage location |
| Unit | No | Unit of measure (default: Pcs) |
| Weight (kg) | No | Product weight |
| Description | No | Product description |

### Sample Excel Row:
```
PROD001,123456789,Samsung TV,Electronics,50,5,200,45000,55000,65000,Samsung,Samsung Corp,Warehouse A,Pcs,15.5,4K Smart TV
```

## Database Schema

### File-based Storage Structure

#### products.dat (Map<String, Product>)
```java
{
  "PROD001": {
    "id": "PROD001",
    "barcode": "123456789",
    "name": "Samsung TV",
    "categoryId": "CAT001",
    "quantity": 50,
    "minStockLevel": 5,
    "maxStockLevel": 200,
    "costPrice": 45000.0,
    "sellPrice": 55000.0,
    "mrp": 65000.0,
    "brand": "Samsung",
    "supplier": "Samsung Corp",
    "location": "Warehouse A",
    "unit": "Pcs",
    "weight": 15.5,
    "description": "4K Smart TV",
    "active": true
  }
}
```

#### users.dat (Map<String, User>)
```java
{
  "admin": {
    "username": "admin",
    "password": "admin123",
    "role": "ADMIN",
    "fullName": "System Administrator"
  }
}
```

#### bills.dat (List<Bill>)
```java
[{
  "billNumber": "INV-20240603-ABC123",
  "billDate": "2024-06-03T15:30:00",
  "items": [...],
  "grandTotal": 1000.00,
  "paymentMethod": "UPI",
  "paymentStatus": "PAID",
  "isDeleted": false
}]
```

## API Documentation

### StockService Methods

```java
// Product Operations
boolean addProduct(Product product)
boolean updateProduct(Product product)
boolean deleteProduct(String productId)
Product getProduct(String id)
List<Product> getAllProducts()
List<Product> searchProducts(String keyword)

// Stock Operations
boolean updateQuantity(String productId, int newQuantity)
boolean addStock(String productId, int quantity, String reason)
boolean removeStock(String productId, int quantity, String reason)
boolean transferStock(String fromProductId, String toProductId, int quantity)

// Sales Operations
boolean sellProduct(String productId, int quantity, String username)
void saveBill(Bill bill)
List<Bill> getBills()
double getTodaySales()
```

### AuthService Methods

```java
boolean login(String username, String password)
void logout()
User getCurrentUser()
boolean isAdmin()
boolean addUser(User user)
boolean deleteUser(String username)
boolean changePassword(String username, String oldPassword, String newPassword)
```

### ReportService Methods

```java
void exportToExcel(String filename, JTable table)
void generateSalesReport()
void generateLowStockReport()
void generateProductStatistics()
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Compilation Errors - Missing Libraries
**Error**: `package org.apache.poi does not exist`
**Solution**: Download Apache POI JARs and add to classpath
```bash
# Download from: https://poi.apache.org/download.html
# Place in lib/ folder
```

#### 2. ClassCastException in Invoice Viewer
**Error**: `JTextField cannot be cast to JPanel`
**Solution**: Use updated InvoiceViewerUI.java with fixed updateStats() method

#### 3. QR Code Generation Fails
**Error**: `NoClassDefFoundError: com/google/zxing/BarcodeFormat`
**Solution**: Add ZXing libraries to classpath
```bash
# Download core-3.5.1.jar and javase-3.5.1.jar
# Add to lib/ folder
```

#### 4. Excel Import Fails
**Error**: `Invalid Excel format`
**Solution**: 
- Download template from application
- Ensure headers match exactly
- Check data types in each column

#### 5. Login Fails
**Error**: `Invalid username or password`
**Solution**:
- Use default credentials: admin/admin123
- Check if users.dat file exists in data/ folder
- Reset by deleting data/users.dat file

#### 6. Memory Issues
**Error**: `OutOfMemoryError`
**Solution**:
```bash
# Increase heap size
java -Xmx1024m -cp "lib/*;bin" Main
```

### Data Recovery

If data becomes corrupted:

1. **Backup current data**:
```bash
copy data\*.* backup\
```

2. **Reset to default**:
```bash
# Delete data files
del data\*.dat

# Application will recreate with default data
```

3. **Restore from backup**:
```bash
copy backup\*.* data\
```

## Performance Optimization

### Tips for Better Performance

1. **Regular Data Cleanup**
   - Archive old bills (90+ days)
   - Delete unnecessary audit logs
   - Remove inactive products

2. **Memory Management**
   - Increase JVM heap size for large datasets
   - Use batch operations for bulk imports

3. **Database Optimization**
   - Index frequently searched fields
   - Regular data backup and compaction

## Security Best Practices

1. **Change Default Passwords**
   ```java
   // Login as admin, then change password
   // Admin Panel → User Management → Edit User
   ```

2. **Regular Backups**
   ```bash
   # Automated backup script
   # Copy data folder to secure location
   ```

3. **Audit Logs**
   - Review audit_log.txt regularly
   - Monitor suspicious activities
   - Enable logging for all transactions

## License

This Stock Management System is released under the MIT License.

```
MIT License

Copyright (c) 2024 Stock Management System

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Support & Contact

For issues, feature requests, or contributions:
- **GitHub Issues**: Create an issue in the repository
- **Email**: support@stockmanagement.com
- **Documentation**: https://docs.stockmanagement.com

## Version History

### v1.0.0 (Current)
- Initial release
- Complete stock management
- POS billing with GST
- UPI QR integration
- Excel import/export
- User authentication
- Invoice management
- Dashboard analytics

### Planned Features (v2.0)
- Cloud sync capabilities
- Mobile app integration
- Barcode scanner support
- Multi-branch support
- Email notifications
- Advanced analytics dashboard

---

## Quick Start Guide

### 5-Minute Setup

1. **Install Java JDK 11+**
2. **Download required JARs** (Apache POI, ZXing)
3. **Place JARs in lib/ folder**
4. **Run compile.bat (Windows) or compile.sh (Linux/Mac)**
5. **Login with admin/admin123**
6. **Start adding products and billing!**

### First Bill in 2 Minutes

1. Click **POS Billing**
2. Search for product
3. Double-click to add to cart
4. Enter quantity
5. Click **Generate Bill**
6. Select payment method
7. Complete payment
8. Print bill

Congratulations! You're now running a professional Stock Management System! 🎉