// FXAA: finds edges by their contrast in brightness and blurs along them. Needs a linearly
// filtered source.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;

uniform sampler2D u_texture;   // lit scene
uniform vec2 u_texel;          // size of a texel in uv

const float REDUCE_MIN = 1.0 / 128.0;
const float REDUCE_MUL = 1.0 / 8.0;
const float SPAN_MAX = 8.0;
const vec3 LUMA = vec3(0.299, 0.587, 0.114);

void main() {
    vec4 middle = texture2D(u_texture, v_uv);
    float lumaNW = dot(texture2D(u_texture, v_uv + vec2(-1.0, -1.0) * u_texel).rgb, LUMA);
    float lumaNE = dot(texture2D(u_texture, v_uv + vec2(1.0, -1.0) * u_texel).rgb, LUMA);
    float lumaSW = dot(texture2D(u_texture, v_uv + vec2(-1.0, 1.0) * u_texel).rgb, LUMA);
    float lumaSE = dot(texture2D(u_texture, v_uv + vec2(1.0, 1.0) * u_texel).rgb, LUMA);
    float lumaM = dot(middle.rgb, LUMA);
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    // Along the edge: across the steepest change in brightness.
    vec2 dir = vec2(-((lumaNW + lumaNE) - (lumaSW + lumaSE)), (lumaNW + lumaSW) - (lumaNE + lumaSE));
    float reduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * 0.25 * REDUCE_MUL, REDUCE_MIN);
    dir = clamp(dir / (min(abs(dir.x), abs(dir.y)) + reduce), -SPAN_MAX, SPAN_MAX) * u_texel;

    vec3 near = 0.5 * (texture2D(u_texture, v_uv + dir * (1.0 / 3.0 - 0.5)).rgb
            + texture2D(u_texture, v_uv + dir * (2.0 / 3.0 - 0.5)).rgb);
    vec3 far = 0.5 * near + 0.25 * (texture2D(u_texture, v_uv - 0.5 * dir).rgb
            + texture2D(u_texture, v_uv + 0.5 * dir).rgb);
    // The wide blur reached past the edge if it left the range of the neighbourhood.
    float lumaFar = dot(far, LUMA);
    gl_FragColor = vec4(lumaFar < lumaMin || lumaFar > lumaMax ? near : far, middle.a);
}
