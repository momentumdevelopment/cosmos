package cope.cosmos.client.shader;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.util.Wrapper;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBShaderObjects;

import java.awt.*;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * http://web.archive.org/web/20200703200808/http://wiki.lwjgl.org/wiki/GLSL_Shaders_with_LWJGL.html, Creates an LWJGL Shader that can be attached to a {@link net.minecraft.client.shader.Framebuffer} Framebuffer
 * @author linustouchtips
 * @since 12/22/2021
 */
public abstract class Shader implements Wrapper {

    // the current program id
    private int program;

    // map of all uniforms (shader configs)
    private Map<String, Integer> configurationMap;

    /**
     * Sets up a shader that can be drawn on a new Framebuffer
     * @param fragmentShader The path to the fragment shader file
     */
    public Shader(String fragmentShader) {

        // program id's of the shaders
        int vertexShaderID = 0;
        int fragmentShaderID = 0;

        try {
            // get our vertex shader file
            InputStream vertexStream = getClass().getResourceAsStream("/assets/cosmos/shaders/glsl/vertex.vert");

            // create our vertex shader
            if (vertexStream != null) {

                // the vertex shader is used to orient each vertex being rendered to its correct position relative to the camera
                vertexShaderID = createShader(IOUtils.toString(vertexStream, Charset.defaultCharset()), 35633);
                IOUtils.closeQuietly(vertexStream);
            }

            // get our fragment shader file
            InputStream fragmentStream = getClass().getResourceAsStream(fragmentShader);

            // create our fragment shader
            if (fragmentStream != null) {

                // the vertex shader runs before the fragment shader, dividing up the camera vertices into fragments, the fragment shader runs upon every fragment and calculates its color, size, radius, etc.
                fragmentShaderID = createShader(IOUtils.toString(fragmentStream, Charset.defaultCharset()), 35632);
                IOUtils.closeQuietly(fragmentStream);
            }

        } catch (Exception exception) {

            // print exception info
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }

            return;
        }

        // if the shader's exist then we can start to attach our program to the GPU, 0 state indicates that the program is null
        if (vertexShaderID != 0 && fragmentShaderID != 0) {
            program = ARBShaderObjects.glCreateProgramObjectARB();

            // make sure the program exists before attaching
            if (program != 0) {
                ARBShaderObjects.glAttachObjectARB(program, vertexShaderID);
                ARBShaderObjects.glAttachObjectARB(program, fragmentShaderID);
                ARBShaderObjects.glLinkProgramARB(program);
                ARBShaderObjects.glValidateProgramARB(program);
            }
        }
    }

    /**
     * Starts the shader program
     */
    public void startShader(int radius, Color color) {

        // use the program
        glUseProgram(program);

        // setup the default fragment configuration
        if (configurationMap == null) {
            configurationMap = new HashMap<>();
            setupConfiguration();
        }

        // update the configuration to our custom values
        updateConfiguration(radius, color);
    }

    /**
     * Sets up the configurations (uniforms) to the default values
     */
    public abstract void setupConfiguration();

    /**
     * Updates the configurations (uniforms) to custom values
     */
    public abstract void updateConfiguration(int radius, Color color);

    /**
     * Attempts to create a shader program from the specified fragment and type
     * @param shaderSource The fragment shader
     * @param shaderType The shader type
     * @return The created shader (if we were able to create one)
     */
    public int createShader(String shaderSource, int shaderType) {
        int shader = 0;

        try {
            // create a shader from the shader type
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            // if the shader exists
            if (shader != 0) {

                // compile it to the GPU
                ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
                ARBShaderObjects.glCompileShaderARB(shader);

                // check if it could be created
                if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0) {
                    throw new RuntimeException("Error creating shader: " + ARBShaderObjects.glGetInfoLogARB(shader, ARBShaderObjects.glGetObjectParameteriARB(shader, 35716)));
                }

                // return the created shader
                return shader;
            }

            else {
                return 0;
            }

        } catch (Exception exception) {

            // delete the shader object, we weren't able to create it
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exception;
        }
    }

    /**
     * Adds the configuration to the map
     * @param configurationName The name of the configuration (as specified by the fragment uniform)
     */
    public void setupConfigurations(String configurationName) {
        configurationMap.put(configurationName, glGetUniformLocation(program, configurationName));
    }

    /**
     * Gets the configuration value from the map
     * @param configurationName The name of the configuration (as specified by the fragment uniform)
     * @return The configuration value from the map
     */
    public int getConfigurations(String configurationName) {
        return configurationMap.get(configurationName);
    }
}