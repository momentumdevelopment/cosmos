package cope.cosmos.client.managment.managers;

import cope.cosmos.client.managment.Manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocialManager extends Manager {

    private final Map<String, Relationship> socials = new ConcurrentHashMap<>();

    public SocialManager() {
        super("SocialManager", "Manages the client's social system");
    }

    public void addSocial(String socialName, Relationship social) {
        socials.put(socialName, social);
    }

    public void removeSocial(String socialName) {
        socials.remove(socialName);
    }

    public Map<String, Relationship> getSocials() {
        return socials;
    }

    public Relationship getSocial(String socialName) {
        return socials.getOrDefault(socialName, Relationship.NONE);
    }

    public enum Relationship {
        FRIEND, ENEMY, NONE
    }
}
