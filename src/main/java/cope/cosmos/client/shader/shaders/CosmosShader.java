package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import org.lwjgl.opengl.GL20;

import java.awt.*;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;

/**
 * @author Gopro336
 */
public class CosmosShader extends Shader {

    Color color = new Color(255,255,255,255);

    public CosmosShader() {
        super("/assets/cosmos/shaders/glsl/cosmos.frag");
    }

    @Override
    public void setupConfiguration() {
        setupConfigurations("time");
        setupConfigurations("resolution");
        setupConfigurations("texture");
        setupConfigurations("color");
    }

    @Override
    public void updateConfiguration() {
        glUniform1f(getConfigurations("time"), time);
        glUniform2f(getConfigurations("resolution"), mc.displayWidth, mc.displayHeight);
        glUniform1f(getConfigurations("texture"), 0);
        GL20.glUniform4f(getConfigurations("color"), color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f );
    }

}
