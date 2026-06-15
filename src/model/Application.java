package model;

import java.io.Serializable;
import java.util.Date;

public class Application implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String applicationId;
    private String studentId;
    private String jobId;
    private Date appliedDate;
    private String status; // Applied, Shortlisted, Rejected, Selected
    private String comments;
    
    public Application(String applicationId, String studentId, String jobId) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.jobId = jobId;
        this.appliedDate = new Date();
        this.status = "Applied";
        this.comments = "";
    }
    
    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public Date getAppliedDate() { return appliedDate; }
    public void setAppliedDate(Date appliedDate) { this.appliedDate = appliedDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    @Override
    public String toString() {
        return String.format("%s|%s|%s|%d|%s|%s",
                applicationId, studentId, jobId, appliedDate.getTime(),
                status, comments);
    }
    
    public static Application fromString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;
        
        Application app = new Application(parts[0], parts[1], parts[2]);
        app.setAppliedDate(new Date(Long.parseLong(parts[3])));
        app.setStatus(parts[4]);
        app.setComments(parts[5]);
        return app;
    }
}
