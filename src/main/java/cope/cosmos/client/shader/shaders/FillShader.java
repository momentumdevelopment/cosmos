package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author linustouchtips, Surge
 * @since 12/22/2021
 */
public class FillShader extends Shader {
    public FillShader() {
        super("/assets/cosmos/shaders/glsl/fill.frag");
    }

    @Override
    public void setupConfiguration() {
        setupConfigurations("texture");
        setupConfigurations("texelSize");
        setupConfigurations("color");
        setupConfigurations("divider");
        setupConfigurations("radius");
        setupConfigurations("maxSample");
    }

    @Override
    public void updateConfiguration(int radius, Color color) {
        glUniform1i(getConfigurations("texture"), 0);
        glUniform2f(getConfigurations("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform4f(getConfigurations("color"), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        glUniform1f(getConfigurations("radius"), radius);
    }
}
