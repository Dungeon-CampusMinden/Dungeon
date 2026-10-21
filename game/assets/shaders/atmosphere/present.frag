// Copies the screen's part of the lit scene to the screen, one texel per pixel, and adds vignette and
// film grain.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;

uniform sampler2D u_texture;   // lit scene
uniform vec2 u_screen;         // screen size in pixels
uniform float u_time;
uniform float u_vignette;      // vignette strength, 0 for none
uniform float u_grain;         // 1 adds film grain, 0 leaves it out
uniform float u_brightness;    // player's brightness, 1 for normal

void main() {
    vec3 col = texture2D(u_texture, v_uv).rgb;
    // Brightness as a gamma curve: it lifts or lowers the dark and middle tones while black stays
    // black and full brightness stays full.
    col = pow(col, vec3(1.0 / u_brightness));

    vec2 s = gl_FragCoord.xy / u_screen;
    vec2 q = (s - 0.5) * vec2(u_screen.x / u_screen.y, 1.0);
    float vignette = 1.0 - smoothstep(0.32, 1.25, length(q));
    col *= 1.0 - 0.58 * u_vignette * (1.0 - vignette);

    // Hash on wrapped coordinates keeps the sine argument small enough for low precision.
    vec2 g = mod(gl_FragCoord.xy, 256.0) + fract(u_time * 7.13) * 91.0;
    float grain = fract(sin(dot(g, vec2(0.12989, 0.78233))) * 437.585453);
    col += (grain - 0.5) * 0.035 * u_grain;

    gl_FragColor = vec4(col, 1.0);
}
