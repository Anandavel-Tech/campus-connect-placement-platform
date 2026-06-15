package interfaces;

import java.util.Date;

public interface Schedulable {
    boolean scheduleInterview(String studentId, String jobId, Date interviewDate);
    boolean cancelInterview(String studentId, String jobId);
    String getInterviewStatus(String studentId, String jobId);
}