package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UPISettings implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, UPIAccount> upiAccounts;
	private String defaultUPIId;
	private String storeName;
	private String storeCity;
	private String storeAddress;
	private boolean enableUPIQR;
	private boolean autoVerifyPayment;
	private double upiSurchargePercent;

	public UPISettings() {
		this.upiAccounts = new HashMap<>();
		this.defaultUPIId = "";
		this.storeName = "ABC Store";
		this.storeCity = "Mumbai";
		this.storeAddress = "123 Business Park, Mumbai - 400001";
		this.enableUPIQR = true;
		this.autoVerifyPayment = false;
		this.upiSurchargePercent = 0.0;

		// Add default UPI accounts
		addDefaultUPIAccounts();
	}

	private void addDefaultUPIAccounts() {
		upiAccounts.put("store@icici", new UPIAccount("store@icici", "ICICI Bank", "ABC Store", true));
		upiAccounts.put("store@hdfc", new UPIAccount("store@hdfc", "HDFC Bank", "ABC Store", false));
		upiAccounts.put("store@sbi", new UPIAccount("store@sbi", "SBI", "ABC Store", false));
		upiAccounts.put("abcstore@paytm", new UPIAccount("abcstore@paytm", "Paytm Payments Bank", "ABC Store", false));
		upiAccounts.put("abcstore@okhdfcbank", new UPIAccount("abcstore@okhdfcbank", "HDFC Bank", "ABC Store", false));
		defaultUPIId = "store@icici";
	}

	public void addUPIAccount(UPIAccount account) {
		upiAccounts.put(account.getUpiId(), account);
	}

	public void removeUPIAccount(String upiId) {
		if (!upiId.equals(defaultUPIId)) {
			upiAccounts.remove(upiId);
		}
	}

	public UPIAccount getUPIAccount(String upiId) {
		return upiAccounts.get(upiId);
	}

	public Map<String, UPIAccount> getUpiAccounts() {
		return upiAccounts;
	}

	public String getDefaultUPIId() {
		return defaultUPIId;
	}

	public void setDefaultUPIId(String defaultUPIId) {
		if (upiAccounts.containsKey(defaultUPIId)) {
			this.defaultUPIId = defaultUPIId;
		}
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreCity() {
		return storeCity;
	}

	public void setStoreCity(String storeCity) {
		this.storeCity = storeCity;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	public void setStoreAddress(String storeAddress) {
		this.storeAddress = storeAddress;
	}

	public boolean isEnableUPIQR() {
		return enableUPIQR;
	}

	public void setEnableUPIQR(boolean enableUPIQR) {
		this.enableUPIQR = enableUPIQR;
	}

	public boolean isAutoVerifyPayment() {
		return autoVerifyPayment;
	}

	public void setAutoVerifyPayment(boolean autoVerifyPayment) {
		this.autoVerifyPayment = autoVerifyPayment;
	}

	public double getUpiSurchargePercent() {
		return upiSurchargePercent;
	}

	public void setUpiSurchargePercent(double upiSurchargePercent) {
		this.upiSurchargePercent = upiSurchargePercent;
	}

	public static class UPIAccount implements Serializable {
		private static final long serialVersionUID = 1L;

		private String upiId;
		private String bankName;
		private String accountHolderName;
		private boolean isActive;
		private String qrCodePath;

		public UPIAccount(String upiId, String bankName, String accountHolderName, boolean isActive) {
			this.upiId = upiId;
			this.bankName = bankName;
			this.accountHolderName = accountHolderName;
			this.isActive = isActive;
		}

		// Getters and Setters
		public String getUpiId() {
			return upiId;
		}

		public void setUpiId(String upiId) {
			this.upiId = upiId;
		}

		public String getBankName() {
			return bankName;
		}

		public void setBankName(String bankName) {
			this.bankName = bankName;
		}

		public String getAccountHolderName() {
			return accountHolderName;
		}

		public void setAccountHolderName(String accountHolderName) {
			this.accountHolderName = accountHolderName;
		}

		public boolean isActive() {
			return isActive;
		}

		public void setActive(boolean active) {
			isActive = active;
		}

		public String getQrCodePath() {
			return qrCodePath;
		}

		public void setQrCodePath(String qrCodePath) {
			this.qrCodePath = qrCodePath;
		}

		@Override
		public String toString() {
			return upiId + " (" + bankName + ")";
		}
	}
}