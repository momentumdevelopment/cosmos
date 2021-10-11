package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.system.Timer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager extends Manager {
    public NotificationManager() {
        super("NotificationManager", "Handles sending client notifications", 6);
    }

    private final List<Notification> notifications = new ArrayList<>();

    public void pushNotification(Notification notification) {
        boolean unique = true;
        for (Notification uniqueNotification : notifications) {
            if (uniqueNotification.getMessage().equals(notification.getMessage())) {
                unique = false;
                break;
            }
        }

        if (unique) {
            notifications.add(notification);
            notification.getAnimation().setState(true);
            notification.getTimer().reset();
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public enum Type {
        INFO(new ResourceLocation("cosmos", "textures/icons/info.png")), SAFETY(new ResourceLocation("cosmos", "textures/icons/warning.png")), WARNING(new ResourceLocation("cosmos", "textures/icons/warning.png"));

        private final ResourceLocation resourceLocation;

        Type(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
        }

        public ResourceLocation getResourceLocation() {
            return this.resourceLocation;
        }
    }

    public static class Notification {

        private final String message;
        private final Type type;
        private final AnimationManager animation;
        private final Timer timer = new Timer();

        public Notification(String message, Type type) {
            this.message = message;
            this.type = type;
            this.animation = new AnimationManager(200, false);
        }

        public String getMessage() {
            return this.message;
        }

        public Type getType() {
            return this.type;
        }

        public AnimationManager getAnimation() {
            return this.animation;
        }

        public Timer getTimer() {
            return this.timer;
        }
    }
}
