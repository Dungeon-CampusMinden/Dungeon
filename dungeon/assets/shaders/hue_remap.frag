#ifdef GL_ES
precision mediump float;
#endif

// ----- Defines -----
#define PI 3.1415926
#define TAU 6.2831852

// ----- From vertex shader -----
varying vec2 uv;

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

// ----- Helper functions for PMA conversion -----
// All shaders outputting transparency or calculating colors should unPma from texture, and pma before outputting
vec4 unPma(vec4 color) {
    if (color.a < 1e-5) {
        return vec4(0.0);
    }
    return vec4(color.rgb / color.a, color.a);
}

vec4 pma(vec4 color) {
    return vec4(color.rgb * color.a, color.a);
}

// Custom functions

vec3 rgb2hsv(vec3 c) {
    float maxC = max(max(c.r, c.g), c.b);
    float minC = min(min(c.r, c.g), c.b);
    float delta = maxC - minC;
    float h = 0.0;

    if (delta > 1e-5) {
        if (maxC == c.r)
            h = mod((c.g - c.b) / delta, 6.0);
        else if (maxC == c.g)
            h = (c.b - c.r) / delta + 2.0;
        else
            h = (c.r - c.g) / delta + 4.0;
        h /= 6.0;
    }

    float s = (maxC <= 1e-5) ? 0.0 : delta / maxC;
    float v = maxC;
    return vec3(h, s, v);
}

vec3 hsv2rgb(vec3 c) {
    float h = c.x * 6.0;
    float s = c.y;
    float v = c.z;

    float i = floor(h);
    float f = h - i;
    float p = v * (1.0 - s);
    float q = v * (1.0 - f * s);
    float t = v * (1.0 - (1.0 - f) * s);

    if (i == 0.0) return vec3(v, t, p);
    else if (i == 1.0) return vec3(q, v, p);
    else if (i == 2.0) return vec3(p, v, t);
    else if (i == 3.0) return vec3(p, q, v);
    else if (i == 4.0) return vec3(t, p, v);
    else return vec3(v, p, q);
}

float hueDistance(float a, float b) {
    float d = abs(a - b);
    return min(d, 1.0 - d);
}

// Main
void main() {
    vec4 tex = unPma(texture2D(u_texture, uv));
    vec3 hsv = rgb2hsv(tex.rgb);

    // Optional hue remap
    if (hueDistance(hsv.x, u_startingHue) < u_tolerance) {
        hsv.x = u_targetHue;
    }

    vec3 rgb = hsv2rgb(hsv);
    gl_FragColor = pma(vec4(rgb, tex.a));
}
