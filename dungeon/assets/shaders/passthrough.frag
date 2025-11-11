#ifdef GL_ES
precision mediump float;
#endif

// The next line is a special string that indicates to the AbstractShader to include the util.glsl file here.
// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
varying vec2 worldPos; //Comment out if not needed for performance

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform bool u_debugPMA;
uniform bool u_debugWorldPos;

// ----- Custom functions -----


// ----- Main -----
void main() {
    vec4 color = texture2D(u_texture, uv); // PMA texture
    if(u_debugPMA) {
        float maxA = color.a + 1.0 / 255.0; // Account for rounding errors
        if (color.r > maxA || color.g > maxA || color.b > maxA) {
            vec2 pos = worldPos * 16.0;
            float checker = mod(floor(pos.x) + floor(pos.y), 2.0);
            color = vec4(color.r > maxA, color.g > maxA, color.b > maxA, 1.0);
            if (checker < 0.5) {
                color.rgb *= 0.0;
            }
        }
    } else if (u_debugWorldPos) {
        color = vec4(fract(worldPos), 0.0, 1.0);
    }
    gl_FragColor = color;

    // Your shader would look something like this (respecting PMA conversion):
    // vec4 color = unPma(texture2D(u_texture, uv));
    // color = <your shader code here>
    // gl_FragColor = pma(color);
}
