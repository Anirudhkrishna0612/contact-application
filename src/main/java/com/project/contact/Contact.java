package com.project.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email; // Import for @Email
import jakarta.validation.constraints.NotBlank; // Import for @NotBlank
import jakarta.validation.constraints.Pattern; // Import for @Pattern (optional, for phone)
import jakarta.validation.constraints.Size; // Import for @Size

import com.fasterxml.jackson.annotation.JsonIgnore; // Keep this if you're using it
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("cId")
    private int cId;

    @NotBlank(message = "Name field is required !!") // Ensures the name is not empty or just whitespace
    @Size(min = 3, max = 25, message = "Name must be between 3 and 25 characters !!") // Enforces name length
    private String name;

    private String secondName; // No validation on nick name, as it's optional

    @NotBlank(message = "Phone number is required !!") // Ensures phone is not empty
    @Pattern(regexp = "^[0-9]{10}$", message = "Please enter a valid 10-digit phone number !!") // Ensures 10 digits (adjust regex for other formats if needed)
    private String phone;

    @NotBlank(message = "Email field is required !!") // Ensures email is not empty
    @Email(message = "Please enter a valid email address !!") // Ensures it's a valid email format
    private String email;

    private String work; // No validation, as it's optional

    @Column(length = 500) // Specifies column length for description
    private String description;

    private String image; // Stores image file name

    @ManyToOne
    @JsonIgnore // Prevents infinite recursion if you're serializing User and Contact
    private User user;

    // --- Constructor ---
    public Contact() {
        super();
    }

    // --- Getters and Setters (Important: Ensure these are present and correct) ---
    public int getCId() {
        return cId;
    }

    public void setCId(int cId) {
        this.cId = cId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // You might also have a toString() method
    @Override
    public String toString() {
        return "Contact [cId=" + cId + ", name=" + name + ", secondName=" + secondName + ", phone=" + phone + ", email="
                + email + ", work=" + work + ", description=" + description + ", image=" + image + "]";
    }
}