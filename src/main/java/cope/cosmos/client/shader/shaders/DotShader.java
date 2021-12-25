package cope.cosmos.client.shader.shaders;

import cope.cosmos.client.shader.Shader;
import cope.cosmos.util.client.ColorUtil;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1f;

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
    public void updateConfiguration() {
        glUniform1i(getConfigurations("texture"), 0);
        glUniform2f(getConfigurations("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
        glUniform4f(getConfigurations("colorFilled"), ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, 100 / 255F);
        glUniform4f(getConfigurations("colorDot"), ColorUtil.getPrimaryColor().getRed() / 255F, ColorUtil.getPrimaryColor().getGreen() / 255F, ColorUtil.getPrimaryColor().getBlue() / 255F, ColorUtil.getPrimaryColor().getAlpha() / 255F);
        glUniform1f(getConfigurations("radius"), 1);
    }
}
