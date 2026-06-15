package service;

import model.*;
import exceptions.DuplicateApplicationException;
import storage.FileStorageManager;
import java.util.*;
import interfaces.Schedulable;

public class ApplicationService implements Schedulable {
private Map<String, Application> applications;
private Map<String, List<Interview>> interviews;
private FileStorageManager storage;

public ApplicationService() {
    this.applications = new HashMap<>();
    this.interviews = new HashMap<>();
    this.storage = new FileStorageManager();
    loadApplications();
}

private void loadApplications() {
    List<String> appLines = storage.loadFromFile("applications.txt");
    for (String line : appLines) {
        Application app = Application.fromString(line);
        if (app != null) {
            applications.put(app.getApplicationId(), app);
        }
    }
}

public void applyForJob(String studentId, String jobId) throws DuplicateApplicationException {
    // Check for duplicate application
    for (Application app : applications.values()) {
        if (app.getStudentId().equals(studentId) && app.getJobId().equals(jobId)) {
            throw new DuplicateApplicationException("You have already applied for this job");
        }
    }
    
    String applicationId = "APP" + System.currentTimeMillis();
    Application application = new Application(applicationId, studentId, jobId);
    applications.put(applicationId, application);
    storage.appendToFile("applications.txt", application.toString());
}

public List<Application> getApplicationsByStudent(String studentId) {
    List<Application> studentApps = new ArrayList<>();
    for (Application app : applications.values()) {
        if (app.getStudentId().equals(studentId)) {
            studentApps.add(app);
        }
    }
    return studentApps;
}

public List<Application> getApplicationsByJob(String jobId) {
    List<Application> jobApps = new ArrayList<>();
    for (Application app : applications.values()) {
        if (app.getJobId().equals(jobId)) {
            jobApps.add(app);
        }
    }
    return jobApps;
}

public void updateApplicationStatus(String applicationId, String status, String comments) {
    Application app = applications.get(applicationId);
    if (app != null) {
        app.setStatus(status);
        app.setComments(comments);
        saveAllApplications();
    }
}

private void saveAllApplications() {
    storage.saveToFile("applications.txt", new ArrayList<>(applications.values()));
}

@Override
public boolean scheduleInterview(String studentId, String jobId, Date interviewDate) {
    String interviewId = "INT" + System.currentTimeMillis();
    Interview interview = new Interview(interviewId, studentId, jobId, interviewDate);
    
    if (!interviews.containsKey(studentId)) {
        interviews.put(studentId, new ArrayList<>());
    }
    interviews.get(studentId).add(interview);
    return true;
}

@Override
public boolean cancelInterview(String studentId, String jobId) {
    if (interviews.containsKey(studentId)) {
        List<Interview> studentInterviews = interviews.get(studentId);
        return studentInterviews.removeIf(i -> i.getJobId().equals(jobId));
    }
    return false;
}

@Override
public String getInterviewStatus(String studentId, String jobId) {
    if (interviews.containsKey(studentId)) {
        for (Interview interview : interviews.get(studentId)) {
            if (interview.getJobId().equals(jobId)) {
                return interview.getStatus();
            }
        }
    }
    return "No interview scheduled";
}

// Inner class for Interview
private class Interview {
    private String interviewId;
    private String studentId;
    private String jobId;
    private Date interviewDate;
    private String status;
    
    public Interview(String interviewId, String studentId, String jobId, Date interviewDate) {
        this.interviewId = interviewId;
        this.studentId = studentId;
        this.jobId = jobId;
        this.interviewDate = interviewDate;
        this.status = "Scheduled";
    }
    
    public String getJobId() { return jobId; }
    public String getStatus() { return status; }
}

}