package service;

import model.*;
import storage.FileStorageManager;
import java.util.*;
import interfaces.Searchable;

public class JobService implements Searchable<Job> {
private Map<String, Job> jobs;
private FileStorageManager storage;

public JobService() {
    this.jobs = new HashMap<>();
    this.storage = new FileStorageManager();
    loadJobs();
    createDemoJobs();
}

private void createDemoJobs() {
    if (!jobs.isEmpty()) return;

    postJob(new Job("JOB_DEMO_1", "Software Engineer - Java",
        "Build enterprise applications using Java and Spring Boot.",
        "Java, Spring Boot, SQL, REST APIs", 9.0, "CGPA 7.0+",
        "DEMO_RECRUITER", "Tata Consultancy Services"));
    postJob(new Job("JOB_DEMO_2", "Data Analyst",
        "Analyze large datasets and create clear business dashboards.",
        "Python, SQL, Pandas, Power BI", 7.5, "CGPA 7.5+",
        "DEMO_RECRUITER", "Infosys"));
    postJob(new Job("JOB_DEMO_3", "Machine Learning Engineer",
        "Develop and evaluate machine learning services for production.",
        "Python, TensorFlow, PyTorch, MLOps", 13.0, "CGPA 8.0+",
        "DEMO_RECRUITER", "Infosys"));
    postJob(new Job("JOB_DEMO_4", "DevOps Engineer",
        "Automate cloud infrastructure and delivery pipelines.",
        "Docker, Kubernetes, Jenkins, AWS, Linux", 11.0, "CGPA 7.5+",
        "DEMO_RECRUITER", "Tata Consultancy Services"));
}

private void loadJobs() {
    List<String> jobLines = storage.loadFromFile("jobs.txt");
    for (String line : jobLines) {
        Job job = Job.fromString(line);
        if (job != null) {
            jobs.put(job.getJobId(), job);
        }
    }
}

public void postJob(Job job) {
    jobs.put(job.getJobId(), job);
    storage.appendToFile("jobs.txt", job.toString());
}

public List<Job> getAllJobs() {
    return new ArrayList<>(jobs.values());
}

public List<Job> getActiveJobs() {
    List<Job> activeJobs = new ArrayList<>();
    for (Job job : jobs.values()) {
        if (job.isActive()) {
            activeJobs.add(job);
        }
    }
    return activeJobs;
}

public Job getJobById(String jobId) {
    return jobs.get(jobId);
}

public List<Job> getJobsByRecruiter(String recruiterId) {
    List<Job> recruiterJobs = new ArrayList<>();
    for (Job job : jobs.values()) {
        if (job.getRecruiterId().equals(recruiterId)) {
            recruiterJobs.add(job);
        }
    }
    return recruiterJobs;
}

public void updateJob(Job job) {
    jobs.put(job.getJobId(), job);
    saveAllJobs();
}

private void saveAllJobs() {
    storage.saveToFile("jobs.txt", new ArrayList<>(jobs.values()));
}

@Override
public List<Job> searchByKeyword(String keyword) {
    List<Job> results = new ArrayList<>();
    String lowerKeyword = keyword.toLowerCase();
    
    for (Job job : jobs.values()) {
        if (job.getTitle().toLowerCase().contains(lowerKeyword) ||
            job.getDescription().toLowerCase().contains(lowerKeyword) ||
            job.getRequiredSkills().toLowerCase().contains(lowerKeyword) ||
            job.getCompanyName().toLowerCase().contains(lowerKeyword)) {
            results.add(job);
        }
    }
    return results;
}

@Override
public List<Job> filterByCriteria(String criteria, Object value) {
    List<Job> results = new ArrayList<>();
    
    switch (criteria.toLowerCase()) {
        case "skill":
            String skill = ((String) value).toLowerCase();
            for (Job job : jobs.values()) {
                if (job.getRequiredSkills().toLowerCase().contains(skill)) {
                    results.add(job);
                }
            }
            break;
        case "salary":
            double minSalary = (double) value;
            for (Job job : jobs.values()) {
                if (job.getSalary() >= minSalary) {
                    results.add(job);
                }
            }
            break;
        case "company":
            String company = ((String) value).toLowerCase();
            for (Job job : jobs.values()) {
                if (job.getCompanyName().toLowerCase().equals(company)) {
                    results.add(job);
                }
            }
            break;
    }
    return results;
}
}

