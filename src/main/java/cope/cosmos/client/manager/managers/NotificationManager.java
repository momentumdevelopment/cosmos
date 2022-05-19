package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.client.ui.util.animation.Animation;
import cope.cosmos.util.math.Timer;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class NotificationManager extends Manager {

    // list of notifications
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManager() {
        super("NotificationManager", "Handles sending client notifications");
    }

    /**
     * Sends a notification to the user
     * @param notification The notification to send
     */
    public void addNotification(Notification notification) {
        if (!notifications.contains(notification)) {
            notifications.add(notification);

            // animation
            notification.getAnimation().setState(true);
            notification.getTimer().resetTime();
        }
    }

    /**
     * Gets the list of notifications
     * @return List of notifications
     */
    @SuppressWarnings("unused")
    public List<Notification> getNotifications() {
        return notifications;
    }

    public enum Type {

        /**
         * Client information
         */
        INFO(new ResourceLocation("cosmos", "textures/icons/info.png")),

        /**
         * Client safety warning (potential error)
         */
        SAFETY(new ResourceLocation("cosmos", "textures/icons/warning.png")),

        /**
         * Client error
         */
        WARNING(new ResourceLocation("cosmos", "textures/icons/warning.png"));

        // notification type icon
        private final ResourceLocation resourceLocation;

        Type(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
        }

        /**
         * Gets the notification icon
         * @return The notification icon
         */
        public ResourceLocation getResourceLocation() {
            return resourceLocation;
        }
    }

    public static class Notification {

        // notification info
        private final String message;
        private final Type type;

        // animation
        private final Animation animation = new Animation(300, false);
        private final Timer timer = new Timer();

        public Notification(String message, Type type) {
            this.message = message;
            this.type = type;
        }

        /**
         * Gets the notification message
         * @return The notification message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the notification type
         * @return The notification type
         */
        public Type getType() {
            return type;
        }

        /**
         * Gets the notification animation
         * @return The notification animation
         */
        public Animation getAnimation() {
            return animation;
        }

        /**
         * Gets the notification time
         * @return The notification time
         */
        public Timer getTimer() {
            return timer;
        }
    }
}
