package service;

import model.UPISettings;
import java.io.*;

public class SettingsService {
	private UPISettings upiSettings;
	private static final String SETTINGS_FILE = "data/settings.dat";

	public SettingsService() {
		loadSettings();
	}

	private void loadSettings() {
		File file = new File(SETTINGS_FILE);
		if (file.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				upiSettings = (UPISettings) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				upiSettings = new UPISettings();
			}
		} else {
			upiSettings = new UPISettings();
			saveSettings();
		}
	}

	public void saveSettings() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILE))) {
			oos.writeObject(upiSettings);
		} catch (IOException e) {
			System.err.println("Error saving settings: " + e.getMessage());
		}
	}

	public UPISettings getUPISettings() {
		return upiSettings;
	}

	public void saveUPISettings(UPISettings settings) {
		this.upiSettings = settings;
		saveSettings();
	}

	public String getDefaultUPIId() {
		return upiSettings.getDefaultUPIId();
	}

	public boolean isUPIEnabled() {
		return upiSettings.isEnableUPIQR();
	}
}