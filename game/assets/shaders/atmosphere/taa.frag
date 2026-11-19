// TAA: blends the frame, rendered around a view shifted by a part of a pixel, into the average of
// the frames before it. The history is limited to the colours around the pixel in the current
// frame, so what moved or changed leaves little of a trail. Only the camera's movement is known:
// a sprite that moves across the scene still smears a little.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;

uniform sampler2D u_texture;   // lit scene of this frame
uniform sampler2D u_history;   // result of the last frame
uniform vec2 u_texel;          // size of a texel in uv
uniform vec2 u_offset;         // from a place in this frame to the same place in the history, in uv
uniform float u_blend;         // share of this frame; 1 without a history

void main() {
    vec3 current = texture2D(u_texture, v_uv).rgb;
    vec3 sum = vec3(0.0);
    vec3 squares = vec3(0.0);
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec3 c = texture2D(u_texture, v_uv + vec2(float(x), float(y)) * u_texel).rgb;
            sum += c;
            squares += c * c;
        }
    }
    // The colours around the pixel: their mean, give or take their spread.
    vec3 mean = sum / 9.0;
    vec3 spread = 1.25 * sqrt(max(squares / 9.0 - mean * mean, 0.0));

    vec2 uv = v_uv + u_offset;
    bool known = uv.x >= 0.0 && uv.y >= 0.0 && uv.x <= 1.0 && uv.y <= 1.0;
    vec3 remembered = texture2D(u_history, uv).rgb;
    vec3 history = clamp(remembered, mean - spread, mean + spread);
    // A history far off those colours shows something else, such as a sprite that walked on: the
    // frame replaces it at once.
    float stale = clamp(4.0 * length(remembered - history), 0.0, 1.0);
    gl_FragColor = vec4(mix(history, current, known ? mix(u_blend, 1.0, stale) : 1.0), 1.0);
}
