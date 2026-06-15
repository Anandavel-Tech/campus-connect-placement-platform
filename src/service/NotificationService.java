package service;

import model.*;
import interfaces.Notifiable;
import storage.FileStorageManager;
import java.util.*;

public class NotificationService implements Notifiable {
private Map<String, List<Notification>> notifications;
private FileStorageManager storage;

public NotificationService() {
    this.notifications = new HashMap<>();
    this.storage = new FileStorageManager();
    loadNotifications();
}

private void loadNotifications() {
    List<String> notifLines = storage.loadFromFile("notifications.txt");
    for (String line : notifLines) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 6) {
            Notification notif = new Notification(
                parts[0], parts[1], parts[2], parts[5]
            );
            notif.setRead(Boolean.parseBoolean(parts[4]));
            
            if (!notifications.containsKey(notif.getUserId())) {
                notifications.put(notif.getUserId(), new ArrayList<>());
            }
            notifications.get(notif.getUserId()).add(notif);
        }
    }
}

@Override
public void sendNotification(String userId, String message, String type) {
    String notificationId = "NOTIF" + System.currentTimeMillis();
    Notification notification = new Notification(notificationId, userId, message, type);
    
    if (!notifications.containsKey(userId)) {
        notifications.put(userId, new ArrayList<>());
    }
    notifications.get(userId).add(notification);
    storage.appendToFile("notifications.txt", notification.toString());
}

@Override
public void markAsRead(String notificationId) {
    for (List<Notification> userNotifs : notifications.values()) {
        for (Notification notif : userNotifs) {
            if (notif.getNotificationId().equals(notificationId)) {
                notif.setRead(true);
                saveAllNotifications();
                return;
            }
        }
    }
}

public List<Notification> getUserNotifications(String userId) {
    return notifications.getOrDefault(userId, new ArrayList<>());
}

public List<Notification> getUnreadNotifications(String userId) {
    List<Notification> unread = new ArrayList<>();
    for (Notification notif : getUserNotifications(userId)) {
        if (!notif.isRead()) {
            unread.add(notif);
        }
    }
    return unread;
}

public void broadcastNotification(String message, String type, List<String> userIds) {
    for (String userId : userIds) {
        sendNotification(userId, message, type);
    }
}

private void saveAllNotifications() {
    List<Notification> allNotifs = new ArrayList<>();
    for (List<Notification> notifList : notifications.values()) {
        allNotifs.addAll(notifList);
    }
    storage.saveToFile("notifications.txt", allNotifs);
}

}
