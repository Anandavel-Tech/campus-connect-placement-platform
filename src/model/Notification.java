package model;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String notificationId;
    private String userId;
    private String message;
    private Date createdAt;
    private boolean isRead;
    private String type;
    
    public Notification(String notificationId, String userId, String message, String type) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.createdAt = new Date();
        this.isRead = false;
        this.type = type;
    }
    
    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    @Override
    public String toString() {
        return String.format("%s|%s|%s|%d|%b|%s",
                notificationId, userId, message, createdAt.getTime(), isRead, type);
    }
}
