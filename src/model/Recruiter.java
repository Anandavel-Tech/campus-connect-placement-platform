package model;

import java.util.*;

public class Recruiter extends User {
    private String companyName;
    private String companyDescription;
    private List<String> postedJobs;
    private boolean isApproved;
    
    public Recruiter(String id, String name, String email, String password,
                     String companyName, String companyDescription) {
        super(id, name, email, password, "recruiter");
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.postedJobs = new ArrayList<>();
        this.isApproved = false;
    }
    
    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { 
        this.companyDescription = companyDescription; 
    }
    
    public List<String> getPostedJobs() { return postedJobs; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }
    
    public void postJob(String jobId) {
        postedJobs.add(jobId);
    }
    
    @Override
    public String getDashboard() {
        return "Recruiter Dashboard - " + companyName +
               "\nPosted Jobs: " + postedJobs.size() +
               "\nStatus: " + (isApproved ? "Approved" : "Pending Approval");
    }
    
    @Override
    public String toString() {
        return super.toString() + String.format("|%s|%s|%b", 
                companyName, companyDescription, isApproved);
    }
    
    public static Recruiter fromString(String line) {
        User user = User.fromString(line);
        if (user == null || !(user instanceof Recruiter)) return null;
        
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) return null;
        
        Recruiter recruiter = new Recruiter(
            parts[0], parts[1], parts[2], parts[3],
            parts[6], parts[7]
        );
        recruiter.setApproved(Boolean.parseBoolean(parts[8]));
        return recruiter;
    }
}
