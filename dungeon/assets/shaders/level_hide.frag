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
uniform vec4 u_worldRegion; // x,y = bottom-left corner, z,w = size
uniform float u_transitionSize;
uniform float u_startTime;
uniform bool u_hiding;
const float transitionTime = 0.6; //seconds

// ----- Custom functions -----


// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));
    vec4 newColor = color;

    vec2 dist = u_worldRegion.zw * 0.5;
    vec2 centerPos = u_worldRegion.xy + dist;

    float sdf = sdBox(worldPos - centerPos, dist);
    if (sdf > u_transitionSize) {
        gl_FragColor = pma(color);
        return;
    }

    float elapsed = u_time - u_startTime;
    float time = clamp(elapsed / transitionTime, 0.0, 1.0);
    if (u_hiding) time = 1.0 - time;

    // Smooth transition
    if (u_transitionSize > 0.0){
        float t = max(0.0, sdf / u_transitionSize);
        t = t * t * t;
        newColor = mix(BLACK, color, t);
    } else {
        newColor = BLACK;
    }

    // Apply time factor
    newColor = mix(newColor, color, time);

    gl_FragColor = pma(newColor);
}
