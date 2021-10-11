package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocialManager extends Manager {
    public SocialManager() {
        super("SocialManager", "Manages the client's social system", 13);
    }

    private final Map<String, Relationship> socials = new ConcurrentHashMap<>();

    public void addSocial(String socialName, Relationship social) {
        socials.put(socialName, social);
    }

    public void removeSocial(String socialName) {
        socials.remove(socialName);
    }

    public Map<String, Relationship> getSocials() {
        return this.socials;
    }

    public Relationship getSocial(String socialName) {
        return socials.getOrDefault(socialName, Relationship.NONE);
    }

    public enum Relationship {
        FRIEND, ENEMY, NONE
    }
}
