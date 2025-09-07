package blog.sammi.lab.notes.application.port.out;

import java.util.UUID;

/**
 * Outbound Port - Defines what the application needs from external systems
 * This interface represents the contract for notification services
 * that the application depends on (push notifications, SMS, etc.)
 */
public interface NotificationPort {
    
    /**
     * Send push notification to user
     */
    void sendPushNotification(UUID userId, String title, String message);
    
    /**
     * Send email notification
     */
    void sendEmailNotification(String email, String subject, String content);
    
    /**
     * Send SMS notification
     */
    void sendSmsNotification(String phoneNumber, String message);
    
    /**
     * Send in-app notification
     */
    void sendInAppNotification(UUID userId, String message, String type);
    
    /**
     * Check if user has notification preferences enabled
     */
    boolean isNotificationEnabled(UUID userId, String notificationType);
}
