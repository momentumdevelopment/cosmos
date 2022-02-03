package cope.cosmos.client.ui.clickgui.feature;

import cope.cosmos.client.ui.util.InterfaceUtil;
import cope.cosmos.util.Wrapper;

/**
 * @author linustouchtips
 * @since 01/29/2022
 */
public abstract class DrawableFeature implements InterfaceUtil, Wrapper {

    /**
     * Draws the feature
     */
    public abstract void drawFeature();

    /**
     * Runs when the feature is clicked
     * @param in The type of click
     */
    public abstract void onClick(ClickType in);

    /**
     * Runs when a key on the keyboard is typed
     * @param in The key that was typed
     */
    public abstract void onType(int in);

    /**
     * Runs when the mouse wheel is scrolled
     * @param in The scroll length
     */
    public abstract void onScroll(int in);
}
