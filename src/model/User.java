package model;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private String role; // ADMIN, USER
	private String fullName;

	public User(String username, String password, String role, String fullName) {
		this.username = username;
		this.password = password;
		this.role = role;
		this.fullName = fullName;
	}

	// Getters and Setters
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public String toString() {
		return String.format("User{username='%s', role='%s', name='%s'}", username, role, fullName);
	}
}