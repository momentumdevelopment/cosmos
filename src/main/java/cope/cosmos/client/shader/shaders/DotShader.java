package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import cope.cosmos.util.string.ColorUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1f;

/**
 * @author linustouchtips
 * @since 12/23/2021
 */
public class DotShader extends Shader {
    public DotShader() {
        super("/assets/cosmos/shaders/glsl/dot.frag");
    }

    @Override
    public void setupConfiguration() {
        setupConfigurations("texture");
        setupConfigurations("texelSize");
        setupConfigurations("colorDot");
        setupConfigurations("colorFilled");
        setupConfigurations("divider");
        setupConfigurations("radius");
        setupConfigurations("maxSample");
    }

    @Override
    public void updateConfiguration(int radius, Color color) {
        glUniform1i(getConfigurations("texture"), 0);
        glUniform2f(getConfigurations("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform4f(getConfigurations("colorFilled"), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 100 / 255F);
        glUniform4f(getConfigurations("colorDot"), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        glUniform1f(getConfigurations("radius"), radius);
    }
}
