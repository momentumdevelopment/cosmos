#version 120

uniform sampler2D texture;
uniform vec2 texelSize;

uniform vec4 color;
uniform float radius;

uniform vec2 rainbowStrength;
uniform float rainbowSpeed;
uniform float saturation;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c){
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main(void) {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

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

        // rainbow
        if (rainbowSpeed > -1.0) {
            vec2 coords = vec2(gl_FragCoord.xy * rainbowStrength);
            vec3 rainbowColor = vec3(clamp ((abs(((fract((vec3((float(mod (((coords.x + coords.y) + rainbowSpeed), 1.0)))) + vec3(1.0, 0.6666667, 0.3333333))) * 6.0) - vec3(3.0, 3.0, 3.0))) - vec3(1.0, 1.0, 1.0)), 0.0, 1.0));
            vec3 hsv = vec3(rgb2hsv(rainbowColor).xyz);
            hsv.y = saturation;
            vec3 finalColor = vec3(hsv2rgb(hsv).xyz);
            gl_FragColor = vec4(finalColor.x, finalColor.y, finalColor.z, max(0, (radius - (closest - 1)) / radius));;
        }

        else {
            // color the area
            gl_FragColor = vec4(color.x, color.y, color.z, max(0, (radius - (closest - 1)) / radius));;
        }
    }
}