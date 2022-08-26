package cope.cosmos.client.features.setting;

import cope.cosmos.util.Wrapper;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * @author Surge
 * @since 09/06/22
 */
public class Bind implements Wrapper {

    // The button index
    private final int buttonCode;

    // Input device
    private final Device device;

    // Prevent spam
    private boolean alreadyPressed;

    public Bind(int buttonCode, Device device) {
        this.buttonCode = buttonCode;
        this.device = device;
    }

    /**
     * Checks if the bind has been pressed
     * @return Whether the bind has been pressed
     */
    public boolean isPressed() {

        if (buttonCode <= 1 || mc.currentScreen != null) {
            return false;
        }

        // Our bind is pressed
        boolean pressed = device.equals(Device.KEYBOARD) && Keyboard.isKeyDown(buttonCode) || device.equals(Device.MOUSE) && Mouse.isButtonDown(buttonCode);

        if (pressed) {

            // We haven't already pressed the key
            if (!alreadyPressed) {
                alreadyPressed = true;
                return true;
            }

            else {
                return false;
            }
        }

        else {
            alreadyPressed = false;
            return false;
        }
    }

    public enum Device {

        /**
         * A key on the keyboard
         */
        KEYBOARD,

        /**
         * A mouse button
         */
        MOUSE
    }

    /**
     * Gets the button name for the GUI
     * @return The button name
     */
    public String getButtonName() {

        // Invalid button
        if (buttonCode < 1) {
            return "None";
        }

        else if (device.equals(Device.KEYBOARD)) {
            return Keyboard.getKeyName(buttonCode);
        }

        else if (device.equals(Device.MOUSE)) {
            return Mouse.getButtonName(buttonCode);
        }

        return "Unrecognized";
    }

    /**
     * Gets the button code
     * @return The button code
     */
    public int getButtonCode() {
        return buttonCode;
    }

    /**
     * Gets the input device
     * @return The input device
     */
    public Device getDevice() {
        return device;
    }

}
