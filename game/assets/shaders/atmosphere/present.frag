// Scales the low-resolution scene to the screen. Sharp bilinear filtering keeps art pixels crisp
// at any scale while blending only their one-screen-pixel border, which also lets the camera move
// smoothly between art pixels. Adds bloom, vignette and film grain at screen resolution.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;

uniform sampler2D u_texture;   // graded scene, linear filtering
uniform sampler2D u_bloomNear;
uniform sampler2D u_bloomFar;
uniform vec2 u_texSize;        // scene texture size in texels
uniform vec2 u_scale;          // screen pixels per scene texel
uniform vec2 u_screen;         // screen size in pixels
uniform float u_time;
uniform float u_vignette;      // vignette strength, 0 for none

void main() {
    vec2 texel = v_uv * u_texSize;
    vec2 base = floor(texel);
    vec2 center = fract(texel) - 0.5;
    vec2 range = 0.5 - 0.5 / max(u_scale, vec2(1.0));
    vec2 f = (center - clamp(center, -range, range)) * max(u_scale, vec2(1.0)) + 0.5;
    vec3 col = texture2D(u_texture, (base + f) / u_texSize).rgb;

    col += texture2D(u_bloomNear, v_uv).rgb * 0.4;
    col += texture2D(u_bloomFar, v_uv).rgb * 0.5;

    vec2 s = gl_FragCoord.xy / u_screen;
    vec2 q = (s - 0.5) * vec2(u_screen.x / u_screen.y, 1.0);
    float vignette = 1.0 - smoothstep(0.32, 1.25, length(q));
    col *= 1.0 - 0.58 * u_vignette * (1.0 - vignette);

    // Hash on wrapped coordinates keeps the sine argument small enough for low precision.
    vec2 g = mod(gl_FragCoord.xy, 256.0) + fract(u_time * 7.13) * 91.0;
    float grain = fract(sin(dot(g, vec2(0.12989, 0.78233))) * 437.585453);
    col += (grain - 0.5) * 0.035;

    gl_FragColor = vec4(col, 1.0);
}
