package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import net.minecraft.util.math.Vec2f;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;

/**
 * @author Gopro336
 */
public class LightsShader extends Shader {

    public LightsShader() {
        super("/assets/cosmos/shaders/glsl/lights.frag");
    }

    @Override
    public void setupConfiguration() {
        setupConfigurations("time");
        setupConfigurations("resolution");
    }

    @Override
    public void updateConfiguration() {
        glUniform1f(getConfigurations("time"), time);
        glUniform2f(getConfigurations("resolution"), mc.displayHeight, mc.displayHeight);
    }
}
