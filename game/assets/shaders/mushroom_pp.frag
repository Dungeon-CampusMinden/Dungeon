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
uniform float u_viewDistance; // 0 to 1, where 1 means full screen is visible
const float u_viewFalloff = 0.25; // portion of viewDistance used for falloff
uniform vec4 u_homeBounds; // x, y, width, height. Home is always lit

// ----- Custom functions -----
bool isInHome(vec2 pos) {
    return pos.x >= u_homeBounds.x &&
           pos.x <= (u_homeBounds.x + u_homeBounds.z) &&
           pos.y >= u_homeBounds.y &&
           pos.y <= (u_homeBounds.y + u_homeBounds.w);
}

// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));

    vec2 homeCenter = u_homeBounds.xy + u_homeBounds.zw * 0.5;
    vec2 homeSize = u_homeBounds.zw * 0.5;
    float homeSdf = sdBox(worldPos - homeCenter, homeSize);

    // Illuminate slightly around home
    float homeT = smoothstep(0.0, 4.0, homeSdf);
    homeT = 1.0 - homeT;
    homeT = pow(homeT, 3.0);
    homeT *= 0.5;

    // Circular vignette based on view distance. Fully dark past the view distance
    // But unskew the circle to account for aspect ratio first
    vec2 aspectUv = uv / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);

    float sdf = saturate(sdCircle(aspectUv - vec2(0.5), u_viewDistance * 0.5));
    float t = smoothstep(0.0, u_viewFalloff, sdf);
    t = 1.0 - t;
    t = pow(t, 2.0);

    color.rgb *= max(t, homeT);

    gl_FragColor = pma(color);
//    test(t);
}
