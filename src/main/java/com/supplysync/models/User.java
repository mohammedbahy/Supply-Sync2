package com.supplysync.models;

public class User {
    private String id;
    private String email;
    private String password;
    private String name;
    private String role; // "ADMIN" or "MARKETER"
    /** Last checkout contact (marketer); pre-filled on next order. */
    private String prefCustomerName;
    private String prefCustomerPhone;
    private String prefCustomerCountry;
    private String prefShippingAddress;

    public User() {}

    public User(String id, String email, String password, String name, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPrefCustomerName() { return prefCustomerName; }
    public void setPrefCustomerName(String prefCustomerName) { this.prefCustomerName = prefCustomerName; }

    public String getPrefCustomerPhone() { return prefCustomerPhone; }
    public void setPrefCustomerPhone(String prefCustomerPhone) { this.prefCustomerPhone = prefCustomerPhone; }

    public String getPrefCustomerCountry() { return prefCustomerCountry; }
    public void setPrefCustomerCountry(String prefCustomerCountry) { this.prefCustomerCountry = prefCustomerCountry; }

    public String getPrefShippingAddress() { return prefShippingAddress; }
    public void setPrefShippingAddress(String prefShippingAddress) { this.prefShippingAddress = prefShippingAddress; }
}
