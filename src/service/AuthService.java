package service;

import exceptions.InvalidLoginException;
import storage.FileStorageManager;
import java.util.*;

import model.User;
import model.Student;
import model.Recruiter;
import model.Admin;

public class AuthService {
private Map<String, User> users;
private FileStorageManager storage;
private User currentUser;

public AuthService() {
    this.users = new HashMap<>();
    this.storage = new FileStorageManager();
    loadUsers();
    createDefaultAccounts();
}

private void createDefaultAccounts() {
    if (!users.containsKey("admin@campusconnect.com")) {
        Admin admin = new Admin("ADMIN001", "Campus Admin", "admin@campusconnect.com", "admin123");
        users.put(admin.getEmail(), admin);
        storage.appendToFile("admins.txt", admin.toString());
    }

    if (!users.containsKey("student@campusconnect.com")) {
        Student student = new Student("DEMO_STUDENT", "Arjun Sharma", "student@campusconnect.com",
            "student123", "CC2026001", "Computer Science", 8.7,
            "Java, Python, React, SQL, Spring Boot", "Final-year student focused on full-stack development.");
        users.put(student.getEmail(), student);
        storage.appendToFile("students.txt", student.toString());
    }

    if (!users.containsKey("recruiter@campusconnect.com")) {
        Recruiter recruiter = new Recruiter("DEMO_RECRUITER", "Deepa Krishnan",
            "recruiter@campusconnect.com", "recruiter123", "Tata Consultancy Services",
            "Technology consulting and services");
        recruiter.setApproved(true);
        users.put(recruiter.getEmail(), recruiter);
        storage.appendToFile("recruiters.txt", recruiter.toString());
    }
}

private void loadUsers() {
    List<String> userLines = storage.loadFromFile("students.txt");
    for (String line : userLines) {
        Student student = Student.fromString(line);
        if (student != null) {
            users.put(student.getEmail(), student);
        }
    }
    
    userLines = storage.loadFromFile("recruiters.txt");
    for (String line : userLines) {
        Recruiter recruiter = Recruiter.fromString(line);
        if (recruiter != null) {
            users.put(recruiter.getEmail(), recruiter);
        }
    }
    
    userLines = storage.loadFromFile("admins.txt");
    for (String line : userLines) {
        Admin admin = Admin.fromString(line);
        if (admin != null) {
            users.put(admin.getEmail(), admin);
        }
    }
}

public User login(String email, String password) throws InvalidLoginException {
    User user = users.get(email);
    if (user == null) {
        throw new InvalidLoginException("User not found with email: " + email);
    }
    
    if (!user.getPassword().equals(password)) {
        throw new InvalidLoginException("Invalid password");
    }
    
    if (!user.isActive()) {
        throw new InvalidLoginException("Account is deactivated. Please contact admin.");
    }
    
    currentUser = user;
    return user;
}

public void registerStudent(Student student) {
    users.put(student.getEmail(), student);
    storage.appendToFile("students.txt", student.toString());
}

public void registerRecruiter(Recruiter recruiter) {
    users.put(recruiter.getEmail(), recruiter);
    storage.appendToFile("recruiters.txt", recruiter.toString());
}

public void logout() {
    currentUser = null;
}

public User getCurrentUser() {
    return currentUser;
}

public Map<String, User> getAllUsers() {
    return users;
}

public List<Student> getAllStudents() {
    List<Student> students = new ArrayList<>();
    for (User user : users.values()) {
        if (user instanceof Student) {
            students.add((Student) user);
        }
    }
    return students;
}

public List<Recruiter> getAllRecruiters() {
    List<Recruiter> recruiters = new ArrayList<>();
    for (User user : users.values()) {
        if (user instanceof Recruiter) {
            recruiters.add((Recruiter) user);
        }
    }
    return recruiters;
}

public boolean emailExists(String email) {
    return users.containsKey(email);
}

}
