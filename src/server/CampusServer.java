package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.*;
import model.*;
import exceptions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CampusServer {
private HttpServer server;
private AuthService authService;
private JobService jobService;
private ApplicationService applicationService;
private NotificationService notificationService;
private CareerGuidanceEngine guidanceEngine;
private Gson gson;

public CampusServer() throws IOException {
    this.server = HttpServer.create(new InetSocketAddress(8080), 0);
    this.authService = new AuthService();
    this.jobService = new JobService();
    this.applicationService = new ApplicationService();
    this.notificationService = new NotificationService();
    this.guidanceEngine = new CareerGuidanceEngine(jobService);
    this.gson = new Gson();

    setupEndpoints();
    server.setExecutor(null);
}

private void setupEndpoints() {
    // Authentication endpoints
    server.createContext("/api/login", new LoginHandler());
    server.createContext("/api/register", new RegisterHandler());
    server.createContext("/api/logout", new LogoutHandler());

    // Student endpoints
    server.createContext("/api/student/profile", new StudentProfileHandler());
    server.createContext("/api/student/jobs", new StudentJobsHandler());
    server.createContext("/api/student/apply", new ApplyJobHandler());
    server.createContext("/api/student/applications", new StudentApplicationsHandler());
    server.createContext("/api/student/guidance", new CareerGuidanceHandler());

    // Recruiter endpoints
    server.createContext("/api/recruiter/jobs", new RecruiterJobsHandler());
    server.createContext("/api/recruiter/postjob", new PostJobHandler());
    server.createContext("/api/recruiter/applicants", getApplicantsHandler());

    // Admin endpoints
    server.createContext("/api/admin/statistics", new AdminStatisticsHandler());
    server.createContext("/api/admin/users", new AdminUsersHandler());
    server.createContext("/api/admin/broadcast", new BroadcastHandler());

    // Notification endpoints
    server.createContext("/api/notifications", new NotificationHandler());

    // Static file serving
    server.createContext("/", new StaticFileHandler());
}

private HttpHandler getApplicantsHandler() {
    return new ApplicantsHandler();
}

public void start() {
    server.start();
    System.out.println("Server started on port 8080");
    System.out.println("Access the application at: http://localhost:8080");
}

private class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String email = json.get("email").getAsString();
            String password = json.get("password").getAsString();
            
            try {
                User user = authService.login(email, password);
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("role", user.getRole());
                response.addProperty("name", user.getName());
                response.addProperty("userId", user.getId());
                
                String responseStr = gson.toJson(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseStr.length());
                OutputStream os = exchange.getResponseBody();
                os.write(responseStr.getBytes());
                os.close();
            } catch (InvalidLoginException e) {
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", e.getMessage());
                
                String responseStr = gson.toJson(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(401, responseStr.length());
                OutputStream os = exchange.getResponseBody();
                os.write(responseStr.getBytes());
                os.close();
            }
        }
    }
}

private class RegisterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String role = json.get("role").getAsString();
            String name = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            String password = json.get("password").getAsString();
            
            String userId = "U" + System.currentTimeMillis();
            JsonObject response = new JsonObject();
            
            if (authService.emailExists(email)) {
                response.addProperty("success", false);
                response.addProperty("message", "An account with this email already exists");
            } else if ("student".equals(role)) {
                Student student = new Student(userId, name, email, password, 
                    json.has("rollNumber") ? json.get("rollNumber").getAsString() : "",
                    json.has("department") ? json.get("department").getAsString() : "",
                    json.has("cgpa") ? json.get("cgpa").getAsDouble() : 0.0,
                    json.has("skills") ? json.get("skills").getAsString() : "",
                    json.has("resume") ? json.get("resume").getAsString() : "");
                authService.registerStudent(student);
                response.addProperty("success", true);
                response.addProperty("message", "Student registered successfully");
            } else if ("recruiter".equals(role)) {
                Recruiter recruiter = new Recruiter(userId, name, email, password,
                    json.has("company") ? json.get("company").getAsString() : "",
                    json.has("companyDesc") ? json.get("companyDesc").getAsString() : "");
                authService.registerRecruiter(recruiter);
                response.addProperty("success", true);
                response.addProperty("message", "Recruiter registered successfully. Waiting for admin approval.");
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid role");
            }
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        authService.logout();
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        
        String responseStr = gson.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseStr.length());
        OutputStream os = exchange.getResponseBody();
        os.write(responseStr.getBytes());
        os.close();
    }
}

private class StudentProfileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Student)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            Student student = (Student) currentUser;
            JsonObject response = new JsonObject();
            response.addProperty("name", student.getName());
            response.addProperty("email", student.getEmail());
            response.addProperty("rollNumber", student.getRollNumber());
            response.addProperty("department", student.getDepartment());
            response.addProperty("cgpa", student.getCgpa());
            response.addProperty("skills", student.getSkills());
            response.addProperty("resumeSummary", student.getResumeSummary());
            response.addProperty("year", student.getYear());
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            Student student = (Student) currentUser;
            
            if (json.has("skills")) student.setSkills(json.get("skills").getAsString());
            if (json.has("cgpa")) student.setCgpa(json.get("cgpa").getAsDouble());
            if (json.has("resumeSummary")) student.setResumeSummary(json.get("resumeSummary").getAsString());
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Profile updated successfully");
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class StudentJobsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Job> jobs = jobService.getActiveJobs();
            List<JsonObject> jobList = new ArrayList<>();
            
            for (Job job : jobs) {
                JsonObject jobJson = new JsonObject();
                jobJson.addProperty("jobId", job.getJobId());
                jobJson.addProperty("title", job.getTitle());
                jobJson.addProperty("company", job.getCompanyName());
                jobJson.addProperty("salary", job.getSalary());
                jobJson.addProperty("skills", job.getRequiredSkills());
                jobJson.addProperty("description", job.getDescription());
                jobList.add(jobJson);
            }
            
            String responseStr = gson.toJson(jobList);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class ApplyJobHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Student)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String jobId = json.get("jobId").getAsString();
            
            try {
                applicationService.applyForJob(currentUser.getId(), jobId);
                notificationService.sendNotification(currentUser.getId(), 
                    "You have successfully applied for job " + jobId, "application");
                
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("message", "Application submitted successfully");
                
                String responseStr = gson.toJson(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseStr.length());
                OutputStream os = exchange.getResponseBody();
                os.write(responseStr.getBytes());
                os.close();
            } catch (DuplicateApplicationException e) {
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", e.getMessage());
                
                String responseStr = gson.toJson(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, responseStr.length());
                OutputStream os = exchange.getResponseBody();
                os.write(responseStr.getBytes());
                os.close();
            }
        }
    }
}

private class StudentApplicationsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Student)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Application> apps = applicationService.getApplicationsByStudent(currentUser.getId());
            List<JsonObject> appList = new ArrayList<>();
            
            for (Application app : apps) {
                Job job = jobService.getJobById(app.getJobId());
                JsonObject appJson = new JsonObject();
                appJson.addProperty("applicationId", app.getApplicationId());
                appJson.addProperty("jobId", app.getJobId());
                appJson.addProperty("jobTitle", job != null ? job.getTitle() : "Unknown");
                appJson.addProperty("company", job != null ? job.getCompanyName() : "Unknown");
                appJson.addProperty("status", app.getStatus());
                appJson.addProperty("appliedDate", app.getAppliedDate().getTime());
                appJson.addProperty("comments", app.getComments());
                appList.add(appJson);
            }
            
            String responseStr = gson.toJson(appList);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class CareerGuidanceHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Student)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            Student student = (Student) currentUser;
            Map<String, Object> advice = guidanceEngine.getCareerAdvice(student);
            
            JsonObject response = new JsonObject();
            response.addProperty("missingSkills", advice.get("missingSkills").toString());
            response.addProperty("skillAdvice", (String) advice.get("skillAdvice"));
            response.addProperty("academicAdvice", (String) advice.get("academicAdvice"));
            
            List<JsonObject> recommendedJobs = new ArrayList<>();
            List<Job> jobs = guidanceEngine.getRecommendedJobs(student);
            for (Job job : jobs) {
                JsonObject jobJson = new JsonObject();
                jobJson.addProperty("jobId", job.getJobId());
                jobJson.addProperty("title", job.getTitle());
                jobJson.addProperty("company", job.getCompanyName());
                jobJson.addProperty("matchScore", guidanceEngine.calculateMatchScore(
                    student.getSkills().toLowerCase(), 
                    job.getRequiredSkills().toLowerCase()));
                recommendedJobs.add(jobJson);
            }
            response.add("recommendedJobs", gson.toJsonTree(recommendedJobs));
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class RecruiterJobsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Recruiter)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Job> jobs = jobService.getJobsByRecruiter(currentUser.getId());
            List<JsonObject> jobList = new ArrayList<>();
            
            for (Job job : jobs) {
                JsonObject jobJson = new JsonObject();
                jobJson.addProperty("jobId", job.getJobId());
                jobJson.addProperty("title", job.getTitle());
                jobJson.addProperty("salary", job.getSalary());
                jobJson.addProperty("skills", job.getRequiredSkills());
                jobJson.addProperty("active", job.isActive());
                jobList.add(jobJson);
            }
            
            String responseStr = gson.toJson(jobList);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class PostJobHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Recruiter)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            Recruiter recruiter = (Recruiter) currentUser;
            
            String jobId = "JOB" + System.currentTimeMillis();
            Job job = new Job(jobId,
                json.get("title").getAsString(),
                json.get("description").getAsString(),
                json.get("skills").getAsString(),
                json.get("salary").getAsDouble(),
                json.get("eligibility").getAsString(),
                recruiter.getId(),
                recruiter.getCompanyName());
            
            jobService.postJob(job);
            recruiter.postJob(jobId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Job posted successfully");
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class ApplicantsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Recruiter)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String jobId = null;
            if (query != null) {
                String[] params = query.split("=");
                if (params.length == 2 && params[0].equals("jobId")) {
                    jobId = params[1];
                }
            }
            
            List<Application> apps = applicationService.getApplicationsByJob(jobId);
            List<JsonObject> applicantList = new ArrayList<>();
            
            for (Application app : apps) {
                Student student = null;
                for (User user : authService.getAllUsers().values()) {
                    if (user instanceof Student && user.getId().equals(app.getStudentId())) {
                        student = (Student) user;
                        break;
                    }
                }
                
                if (student != null) {
                    JsonObject applicant = new JsonObject();
                    applicant.addProperty("applicationId", app.getApplicationId());
                    applicant.addProperty("name", student.getName());
                    applicant.addProperty("email", student.getEmail());
                    applicant.addProperty("cgpa", student.getCgpa());
                    applicant.addProperty("skills", student.getSkills());
                    applicant.addProperty("status", app.getStatus());
                    applicant.addProperty("appliedDate", app.getAppliedDate().getTime());
                    applicantList.add(applicant);
                }
            }
            
            String responseStr = gson.toJson(applicantList);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String applicationId = json.get("applicationId").getAsString();
            String status = json.get("status").getAsString();
            String comments = json.has("comments") ? json.get("comments").getAsString() : "";
            
            applicationService.updateApplicationStatus(applicationId, status, comments);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Application status updated");
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class AdminStatisticsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Admin)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Student> students = authService.getAllStudents();
            List<Recruiter> recruiters = authService.getAllRecruiters();
            List<Job> jobs = jobService.getAllJobs();
            List<Application> allApplications = new ArrayList<>();
            
            for (Student student : students) {
                allApplications.addAll(applicationService.getApplicationsByStudent(student.getId()));
            }
            
            long placedCount = allApplications.stream()
                .filter(app -> "Selected".equals(app.getStatus()))
                .count();
            
            double avgSalary = jobs.stream()
                .filter(Job::isActive)
                .mapToDouble(Job::getSalary)
                .average()
                .orElse(0);
            
            JsonObject stats = new JsonObject();
            stats.addProperty("totalStudents", students.size());
            stats.addProperty("totalRecruiters", recruiters.size());
            stats.addProperty("totalJobs", jobs.size());
            stats.addProperty("totalApplications", allApplications.size());
            stats.addProperty("placedStudents", placedCount);
            stats.addProperty("placementRate", students.size() > 0 ? (placedCount * 100.0 / students.size()) : 0);
            stats.addProperty("averageCTC", avgSalary);
            
            String responseStr = gson.toJson(stats);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class AdminUsersHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Admin)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<JsonObject> userList = new ArrayList<>();
            
            for (User user : authService.getAllUsers().values()) {
                JsonObject userJson = new JsonObject();
                userJson.addProperty("id", user.getId());
                userJson.addProperty("name", user.getName());
                userJson.addProperty("email", user.getEmail());
                userJson.addProperty("role", user.getRole());
                userJson.addProperty("active", user.isActive());
                
                if (user instanceof Recruiter) {
                    userJson.addProperty("approved", ((Recruiter) user).isApproved());
                }
                
                userList.add(userJson);
            }
            
            String responseStr = gson.toJson(userList);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class BroadcastHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Admin)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String message = json.get("message").getAsString();
            
            List<String> allUserIds = new ArrayList<>();
            for (User user : authService.getAllUsers().values()) {
                allUserIds.add(user.getId());
            }
            
            notificationService.broadcastNotification(message, "broadcast", allUserIds);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Broadcast sent to all users");
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class NotificationHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Notification> notifs = notificationService.getUserNotifications(currentUser.getId());
            List<JsonObject> notifList = new ArrayList<>();
            
            for (Notification notif : notifs) {
                JsonObject notifJson = new JsonObject();
                notifJson.addProperty("id", notif.getNotificationId());
                notifJson.addProperty("message", notif.getMessage());
                notifJson.addProperty("type", notif.getType());
                notifJson.addProperty("date", notif.getCreatedAt().getTime());
                notifJson.addProperty("read", notif.isRead());
                notifList.add(notifJson);
            }
            
            String responseStr = gson.toJson(notifList);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String notificationId = json.get("notificationId").getAsString();
            notificationService.markAsRead(notificationId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            
            String responseStr = gson.toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseStr.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseStr.getBytes());
            os.close();
        }
    }
}

private class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        
        String filePath = "frontend" + path;
        File file = new File(filePath);
        
        if (file.exists() && !file.isDirectory()) {
            String mimeType = "text/html";
            if (path.endsWith(".css")) mimeType = "text/css";
            if (path.endsWith(".js")) mimeType = "application/javascript";
            if (path.endsWith(".png")) mimeType = "image/png";
            if (path.endsWith(".jpg")) mimeType = "image/jpeg";
            
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length());
            
            OutputStream os = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = fs.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        } else {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

private void sendError(HttpExchange exchange, int code, String message) throws IOException {
    JsonObject response = new JsonObject();
    response.addProperty("error", message);
    String responseStr = gson.toJson(response);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(code, responseStr.length());
    OutputStream os = exchange.getResponseBody();
    os.write(responseStr.getBytes());
    os.close();
}

}
