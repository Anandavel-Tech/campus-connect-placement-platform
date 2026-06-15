package model;

import java.util.*;

public class Admin extends User {
    private List<String> activities;
    
    public Admin(String id, String name, String email, String password) {
        super(id, name, email, password, "admin");
        this.activities = new ArrayList<>();
    }
    
    public void logActivity(String activity) {
        activities.add(activity);
    }
    
    public List<String> getActivities() {
        return activities;
    }
    
    @Override
    public String getDashboard() {
        return "Admin Dashboard - Welcome " + getName() +
               "\nTotal Activities: " + activities.size();
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    public static Admin fromString(String line) {
        User user = User.fromString(line);
        if (user == null || !(user instanceof Admin)) return null;
        return new Admin(user.getId(), user.getName(), user.getEmail(), user.getPassword());
    }
}
