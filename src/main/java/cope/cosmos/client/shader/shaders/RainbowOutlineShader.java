package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author linustouchtips
 * @since 12/22/2021
 */
public class RainbowOutlineShader extends Shader {
    public RainbowOutlineShader() {
        super("/assets/cosmos/shaders/glsl/rainbow_outline.frag");
    }

    @Override 
    public void setupConfiguration() {
        setupConfigurations("texture");
        setupConfigurations("texelSize");
        setupConfigurations("color");
        setupConfigurations("radius");
        setupConfigurations("rainbowStrength");
        setupConfigurations("rainbowSpeed");
        setupConfigurations("saturation");
    }

    @Override 
    public void updateConfiguration(int radius, Color color) {
        glUniform1i(getConfigurations("texture"), 0);
        glUniform2f(getConfigurations("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform4f(getConfigurations("color"), color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        glUniform1f(getConfigurations("radius"), radius);
        glUniform2f(getConfigurations("rainbowStrength"), -(1 / 300F), -(1 / 300F));
        glUniform1f(getConfigurations("rainbowSpeed"), 0.4F);
        glUniform1f(getConfigurations("saturation"), 0.5F);
    }
}
