package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class SocialManager extends Manager {

    // map of socials
    private final Map<String, Relationship> socials = new ConcurrentHashMap<>();

    public SocialManager() {
        super("SocialManager", "Manages the client's social system");
    }

    /**
     * Adds an entity to our socials list
     * @param socialName The name of the entity
     * @param social The relationship to the entity
     */
    public void addSocial(String socialName, Relationship social) {
        socials.put(socialName, social);
    }

    /**
     * Removes an entity from our socials list
     * @param socialName The name of the entity
     */
    public void removeSocial(String socialName) {
        socials.remove(socialName);
    }

    /**
     * Gets our socials list
     * @return The list of socials
     */
    public Map<String, Relationship> getSocials() {
        return socials;
    }

    /**
     * Gets the relationship of an entity
     * @param socialName The name of the entity
     * @return The relationship to the entity
     */
    public Relationship getSocial(String socialName) {
        return socials.getOrDefault(socialName, Relationship.NONE);
    }

    public enum Relationship {

        /**
         * This entity is our friend, don't target them :)
         */
        FRIEND,

        /**
         * This entity is our enemy, prioritize them in combat >:(
         */
        ENEMY,

        /**
         * We have no relationship to this entity
         */
        NONE
    }
}
