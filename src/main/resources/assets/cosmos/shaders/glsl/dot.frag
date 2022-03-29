#version 120

// texture info
uniform sampler2D texture;
uniform vec2 texelSize;

// colors and width
uniform vec4 colorDot;
uniform vec4 colorFilled;
uniform float radius;

void main(void) {
    // Color for the entity model
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    // If a color is already assigned (alpha value is greater than 0), we want to overwrite it
    if (centerCol.a > 0) {
        // Dots have no alpha, so use dot color every 8 vertices. The '0' can be changed to a higher number to make the dots larger
        if (int(gl_FragCoord.x) - (8 * int(gl_FragCoord.x / 8)) == 0 && int(gl_FragCoord.y) - (8 * int(gl_FragCoord.y / 8)) == 0) {
            gl_FragColor = colorDot;
        }
        // Else we'll use our fill color
        else {
            gl_FragColor = colorFilled;
        }
    } else {
        // Closest radius distance
        float closest = radius * 2.0F + 2.0F;

        // Radius determines the width of the shader
        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {

                // The current color of the fragment based on the texture of the entity, we'll overwrite this color
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

        // Color the area
        gl_FragColor = vec4(colorDot.x, colorDot.y, colorDot.z, max(0, (radius - (closest - 1)) / radius));;
    }
}