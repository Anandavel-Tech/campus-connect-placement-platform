package service;

import model.*;
import java.util.*;

public class CareerGuidanceEngine {
private JobService jobService;

public CareerGuidanceEngine(JobService jobService) {
    this.jobService = jobService;
}

public List<String> analyzeSkillGap(Student student) {
    List<String> missingSkills = new ArrayList<>();
    String studentSkills = student.getSkills().toLowerCase();
    
    for (Job job : jobService.getAllJobs()) {
        String[] requiredSkills = job.getRequiredSkills().toLowerCase().split(",");
        for (String skill : requiredSkills) {
            skill = skill.trim();
            if (!studentSkills.contains(skill) && !missingSkills.contains(skill)) {
                missingSkills.add(skill);
            }
        }
    }
    
    return missingSkills;
}

public List<Job> getRecommendedJobs(Student student) {
    List<Job> recommendedJobs = new ArrayList<>();
    String studentSkills = student.getSkills().toLowerCase();
    
    for (Job job : jobService.getActiveJobs()) {
        int matchScore = calculateMatchScore(studentSkills, job.getRequiredSkills().toLowerCase());
        if (matchScore > 0) {
            recommendedJobs.add(job);
        }
    }
    
    // Sort by match score (higher first)
    recommendedJobs.sort((j1, j2) -> {
        int score1 = calculateMatchScore(studentSkills, j1.getRequiredSkills().toLowerCase());
        int score2 = calculateMatchScore(studentSkills, j2.getRequiredSkills().toLowerCase());
        return Integer.compare(score2, score1);
    });
    
    return recommendedJobs;
}

public int calculateMatchScore(String studentSkills, String jobSkills) {
    String[] jobSkillArray = jobSkills.split(",");
    int matchCount = 0;
    
    for (String skill : jobSkillArray) {
        if (studentSkills.contains(skill.trim())) {
            matchCount++;
        }
    }
    
    return jobSkillArray.length > 0 ? (matchCount * 100) / jobSkillArray.length : 0;
}

public Map<String, Object> getCareerAdvice(Student student) {
    Map<String, Object> advice = new HashMap<>();
    List<String> missingSkills = analyzeSkillGap(student);
    List<Job> recommendations = getRecommendedJobs(student);
    
    advice.put("missingSkills", missingSkills);
    advice.put("recommendedJobs", recommendations);
    advice.put("skillGapPercentage", missingSkills.size());
    
    if (student.getCgpa() < 6.0) {
        advice.put("academicAdvice", "Focus on improving your CGPA. Many companies require minimum 6.0 CGPA.");
    } else if (student.getCgpa() < 7.5) {
        advice.put("academicAdvice", "Good CGPA! Focus on developing technical skills to complement your academics.");
    } else {
        advice.put("academicAdvice", "Excellent academic record! Focus on building a strong portfolio and preparing for interviews.");
    }
    
    if (student.getSkills().isEmpty()) {
        advice.put("skillAdvice", "Add skills to your profile to get better job recommendations.");
    } else if (missingSkills.size() > 5) {
        advice.put("skillAdvice", "Consider learning these in-demand skills: " + 
                   String.join(", ", missingSkills.subList(0, Math.min(5, missingSkills.size()))));
    } else {
        advice.put("skillAdvice", "Your skills align well with current job requirements. Keep learning and updating your skills!");
    }
    
    return advice;
}
}

