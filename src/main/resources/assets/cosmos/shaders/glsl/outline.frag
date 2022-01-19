#version 120

// texture based on the entity, uv (1 / resolution)
uniform sampler2D texture;
uniform vec2 texelSize;

// color and width
uniform vec4 color;
uniform float radius;

void main(void) {
    // color for the entity model, each vertex is assigned a color
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    // if a color is already assigned (alpha value is greater than 0), we don't want to overwrite it with our shader (i.e. shader should only apply to the outline of the entity)
    if (centerCol.a > 0) {
        gl_FragColor = vec4(0, 0, 0, 0);
    }

    else {
        // closest radius distance
        float closest = radius * 2.0F + 2.0F;

        // radius determines the width of the shader
        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {

                // the current color of the fragment based on the texture of the entity, we'll overwrite this color
                vec4 currentColor = texture2D(texture, gl_TexCoord[0].xy + vec2(texelSize.x * x, texelSize.y * y));

                // gl_FragColor is the current color of the fragment, we'll update it according to our uniform (custom value)
                if (currentColor.a > 0) {
                    float currentDist = sqrt(x * x + y * y);

                    if (currentDist < closest) {
                        closest = currentDist;
                    }
                }
            }
        }

        // color the area
        gl_FragColor = vec4(color.x, color.y, color.z, max(0, (radius - (closest - 1)) / radius));;
    }
}