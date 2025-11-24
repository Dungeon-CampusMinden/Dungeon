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
uniform float u_hue;
uniform float u_saturationMult;
uniform float u_valueMult;
uniform float u_transitionSize;

// ----- Custom functions -----


// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));

    vec2 dist = u_worldRegion.zw * 0.5;
    vec2 centerPos = u_worldRegion.xy + dist;

    float sdf = sdBox(worldPos - centerPos, dist);
    if (sdf > u_transitionSize) {
        gl_FragColor = pma(color);
        return;
    }

    vec4 newColor = color;
    vec3 hsv = rgb2hsv(newColor.rgb);
    hsv.x = u_hue < 0.0 ? hsv.x : mod(u_hue, 1.0);
    hsv.y *= u_saturationMult;
    hsv.z *= u_valueMult;
    newColor.rgb = hsv2rgb(hsv);

    // Smooth transition
    if (u_transitionSize > 0.0){
        float t = max(0.0, sdf) / u_transitionSize;
        t = t * t * t;
        color = mix(newColor, color, t);
    }

    gl_FragColor = pma(color);
}
