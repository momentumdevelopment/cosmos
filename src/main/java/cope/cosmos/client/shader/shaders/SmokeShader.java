package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import cope.cosmos.util.client.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author Gopro336
 */
public class SmokeShader extends Shader {

    public SmokeShader() {
        super("/assets/cosmos/shaders/glsl/smoke.frag");
    }

    @Override
    public void setupConfiguration() {
        setupConfigurations("resolution");
        setupConfigurations("time");
    }

    @Override
    public void updateConfiguration() {
        glUniform2f(getConfigurations("resolution"), mc.displayHeight, mc.displayHeight);
        glUniform1f(getConfigurations("time"), this.time);
    }
}
