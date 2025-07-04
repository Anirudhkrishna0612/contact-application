package com.project.contact;
 
import java.util.ArrayList;
import java.util.List;
 
// Corrected imports for Jakarta EE 9+ for Spring Boot 3.x
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank; // Corrected from javax.validation.constraints.NotBlank
import jakarta.validation.constraints.Size;   // Corrected from javax.validation.constraints.Size
 
@Entity
@Table(name="USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
 
    @NotBlank(message= "Name should not be blank!")
    @Size(min =2,max=20, message= "Name should be greater than 1 and less than 20")
    private String name;
 
    @Column(unique = true)
    private String email;
 
    private String password;
    private String role;
    private boolean enabled;
    private String imageUrl;
 
    @Column(length = 500)
    private String about;
 
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY, mappedBy = "user")
    private List<Contact> contacts = new ArrayList<>();
 
    public List<Contact> getContacts() {
        return contacts;
    }
    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
    public User() {
        super();
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
this.email = email;
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
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getAbout() {
        return about;
    }
    public void setAbout(String about) {
        this.about = about;
    }
    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", role=" + role
                + ", enabled=" + enabled + ", imageUrl=" + imageUrl + ", about=" + about + ", contacts=" + contacts
                + "]";
    }
}