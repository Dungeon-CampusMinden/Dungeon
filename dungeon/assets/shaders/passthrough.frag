#ifdef GL_ES
precision mediump float;
#endif

// From vertex shader
varying vec2 uv;

// From LibGDX
uniform sampler2D u_texture;

// DungeonEngine common uniforms set by DrawSystem
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// Custom uniforms
// uniform float u_customUniform1;

void main() {
    vec4 tex = texture2D(u_texture, uv);
    gl_FragColor = tex;
}
