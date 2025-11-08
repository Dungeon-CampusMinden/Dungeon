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
// uniform float u_customUniform1;

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


// Main
void main() {
    vec4 tex = unPma(texture2D(u_texture, uv));
    gl_FragColor = pma(tex);
    //vec4 tex = texture2D(u_texture, uv);
    //gl_FragColor = tex.rgaa;
}
