package cope.cosmos.font;

import org.lwjgl.opengl.GL11;

/**
 * @author LiquidBounce Development, linustouchitps
 * @since 05/25/2021
 */
public class FontCache {

    // cache our display list and last usage
    private int displayList;
    private long lastUsage;

    // whether the font cache is deleted, this is useful later when we are collecting garbage
    private boolean deleted;

    public FontCache(int displayList, long lastUsage) {
        this.displayList = displayList;
        this.lastUsage = lastUsage;
    }

    /**
     * Deletes display lists
     */
    public void finalize() {
        if (!deleted) {
            GL11.glDeleteLists(displayList, 1);
        }
    }

    /**
     * Sets the display list
     * @param in The display list
     */
    public void setDisplayList(int in) {
        displayList = in;
    }

    /**
     * Gets the display list
     * @return The display list
     */
    public int getDisplayList() {
        return displayList;
    }

    /**
     * Sets the last usage
     * @param in The last usage
     */
    public void setLastUsage(long in) {
        lastUsage = in;
    }

    /**
     * Gets the last usage
     * @return The last usage
     */
    public long getLastUsage() {
        return lastUsage;
    }

    /**
     * Marks this cache as deleted
     */
    public void delete() {
        deleted = true;
    }

    /**
     * Checks if this cache has been deleted
     * @return Whether this cache has been deleted
     */
    public boolean isDeleted() {
        return deleted;
    }
}
