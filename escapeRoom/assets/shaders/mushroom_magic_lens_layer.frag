#ifdef GL_ES
precision mediump float;
#endif

// The next line is a special string that indicates to the AbstractShader to include the util.glsl file here.
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
uniform sampler2D u_magnifyingGlassTex;
uniform vec2 u_magnifyingGlassTexSize;
const float u_magnifyingGlassScale = 0.3;
const vec2 u_magnifyingGlassAnchor = vec2(0.525, 0.45);

uniform float u_lensRadius;
const float u_border = 0.005;
uniform bool u_active;

// ----- Custom functions -----


// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));

    if(!u_active) {
        color.a = 0.0;
        gl_FragColor = pma(color);
        return;
    }

    vec2 aspectUv = uv / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);
    vec2 aspectMouse = u_mouse / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);

    vec2 magnifyingGlassUv = (aspectUv - aspectMouse) / u_magnifyingGlassScale + u_magnifyingGlassAnchor;
    magnifyingGlassUv.y = 1.0 - magnifyingGlassUv.y;
    vec4 magnifyingGlassColor = texture2D(u_magnifyingGlassTex, magnifyingGlassUv);

    color = magnifyingGlassColor.g == 1.0 ? color : vec4(0.0);

    gl_FragColor = pma(color);
}
