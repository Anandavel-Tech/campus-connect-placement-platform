package model;

import java.util.*;

public class Student extends User {
    private String rollNumber;
    private String department;
    private double cgpa;
    private String skills;
    private String resumeSummary;
    private int year;
    private List<String> appliedJobs;
    private List<String> notifications;
    private Map<String, String> applicationStatus;
    
    public Student(String id, String name, String email, String password,
                   String rollNumber, String department, double cgpa, 
                   String skills, String resumeSummary) {
        super(id, name, email, password, "student");
        this.rollNumber = rollNumber;
        this.department = department;
        this.cgpa = cgpa;
        this.skills = skills;
        this.resumeSummary = resumeSummary;
        this.year = 2024;
        this.appliedJobs = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.applicationStatus = new HashMap<>();
    }
    
    // Getters and Setters
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    
    public String getResumeSummary() { return resumeSummary; }
    public void setResumeSummary(String resumeSummary) { this.resumeSummary = resumeSummary; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public List<String> getAppliedJobs() { return appliedJobs; }
    public Map<String, String> getApplicationStatus() { return applicationStatus; }
    public List<String> getNotifications() { return notifications; }
    
    public void addNotification(String notification) {
        notifications.add(notification);
    }
    
    public void applyForJob(String jobId) {
        if (!appliedJobs.contains(jobId)) {
            appliedJobs.add(jobId);
            applicationStatus.put(jobId, "Applied");
        }
    }
    
    public void updateApplicationStatus(String jobId, String status) {
        applicationStatus.put(jobId, status);
        addNotification("Application for job " + jobId + " is now " + status);
    }
    
    public boolean hasApplied(String jobId) {
        return appliedJobs.contains(jobId);
    }
    
    @Override
    public String getDashboard() {
        return "Student Dashboard - Welcome " + getName() + 
               "\nApplied Jobs: " + appliedJobs.size() +
               "\nNotifications: " + notifications.size();
    }
    
    @Override
    public String toString() {
        return super.toString() + String.format("|%s|%s|%.2f|%s|%s|%d", 
                rollNumber, department, cgpa, skills, resumeSummary, year);
    }
    
    public static Student fromString(String line) {
        User user = User.fromString(line);
        if (user == null || !(user instanceof Student)) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 12) return null;
        
        Student student = new Student(
            parts[0], parts[1], parts[2], parts[3],
            parts[6], parts[7], Double.parseDouble(parts[8]),
            parts[9], parts[10]
        );
        student.setYear(Integer.parseInt(parts[11]));
        return student;
    }
}
