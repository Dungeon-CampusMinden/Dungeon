#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
varying vec2 worldPos;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform float u_startingHue;
uniform float u_targetHue;
uniform float u_tolerance;
uniform vec4 u_worldRegion; // x,y = bottom-left corner, z,w = size
uniform bool u_hasRegion; // false = apply everywhere

// ----- Custom functions -----
float hueDistance(float a, float b) {
    float d = abs(a - b);
    return min(d, 1.0 - d);
}

// ----- Main -----
void main() {
    vec4 tex = unPma(texture2D(u_texture, uv));
    if (u_hasRegion) {
        vec2 halfSize = u_worldRegion.zw * 0.5;
        if (sdBox(worldPos - (u_worldRegion.xy + halfSize), halfSize) > 0.0) {
            gl_FragColor = pma(tex);
            return;
        }
    }

    vec3 hsv = rgb2hsv(tex.rgb);

    // Optional hue remap
    if (hueDistance(hsv.x, u_startingHue) < u_tolerance) {
        hsv.x = u_targetHue;
    }

    vec3 rgb = hsv2rgb(hsv);
    gl_FragColor = pma(vec4(rgb, tex.a));
}
