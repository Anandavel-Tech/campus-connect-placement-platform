package model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String email;
    private String password;
    private String role;
    private boolean isActive;
    
    public User(String id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // Abstract method for dashboard
    public abstract String getDashboard();
    
    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s|%b", id, name, email, password, role, isActive);
    }
    
    public static User fromString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;
        
        String id = parts[0];
        String name = parts[1];
        String email = parts[2];
        String password = parts[3];
        String role = parts[4];
        boolean isActive = Boolean.parseBoolean(parts[5]);
        
        User user = null;
        switch (role) {
            case "student":
                user = new Student(id, name, email, password, "", "", 0.0, "", "");
                break;
            case "recruiter":
                user = new Recruiter(id, name, email, password, "", "");
                break;
            case "admin":
                user = new Admin(id, name, email, password);
                break;
        }
        
        if (user != null) {
            user.setActive(isActive);
        }
        return user;
    }
}
