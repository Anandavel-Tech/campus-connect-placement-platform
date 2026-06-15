package interfaces;

public interface Notifiable {
    void sendNotification(String userId, String message, String type);
    void markAsRead(String notificationId);
}