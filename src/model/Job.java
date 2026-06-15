package model;

import java.io.Serializable;

public class Job implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String jobId;
    private String title;
    private String description;
    private String requiredSkills;
    private double salary;
    private String eligibility;
    private String recruiterId;
    private String companyName;
    private boolean isActive;
    
    public Job(String jobId, String title, String description, String requiredSkills,
               double salary, String eligibility, String recruiterId, String companyName) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.salary = salary;
        this.eligibility = eligibility;
        this.recruiterId = recruiterId;
        this.companyName = companyName;
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    
    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }
    
    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%.2f|%s|%s|%s|%b",
                jobId, title, description, requiredSkills, salary,
                eligibility, recruiterId, companyName, isActive);
    }
    
    public static Job fromString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) return null;
        
        Job job = new Job(
            parts[0], parts[1], parts[2], parts[3],
            Double.parseDouble(parts[4]), parts[5], parts[6], parts[7]
        );
        job.setActive(Boolean.parseBoolean(parts[8]));
        return job;
    }
}
