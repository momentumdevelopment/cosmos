package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import cope.cosmos.util.string.ColorUtil;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author linustouchtips
 * @since 12/22/2021
 */
public class OutlineShader extends Shader {
    public OutlineShader() {
        super("/assets/cosmos/shaders/glsl/outline.frag");
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
    public void updateConfiguration() {
        glUniform1i(getConfigurations("texture"), 0);
        glUniform2f(getConfigurations("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform4f(getConfigurations("color"), ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, ColorUtil.getPrimaryColor().getAlpha() / 255F);
        glUniform1f(getConfigurations("radius"), 1);
    }
}
