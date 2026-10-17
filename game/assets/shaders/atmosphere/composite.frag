// Lights the composed scene with the ambient and point lights, adds drifting fog, grades the
// result and adds the bloom. With white ambient and tints, no fog and no lights the scene passes
// through unchanged. The result stays premultiplied with the scene's coverage, widened where bloom
// glows over the void, so shaders running on the lit scene still see the void.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;

uniform sampler2D u_texture;   // composed scene, premultiplied
uniform sampler2D u_light;     // light buffer (half intensity)
uniform sampler2D u_bloomNear;
uniform sampler2D u_bloomFar;
uniform vec3 u_ambient;
uniform vec3 u_shadowTint;     // grade of dark areas
uniform vec3 u_lightTint;      // grade of bright areas
uniform vec4 u_view;           // world rect of the scene texture: x, y, width, height
uniform float u_fog;           // fog strength, 0 turns it off
uniform float u_time;

float hash(vec2 p) {
    // Wrapping keeps the sine argument small enough for low-precision GPUs.
    p = mod(p, 289.0);
    return fract(sin(dot(p, vec2(0.1271, 0.3117))) * 437.585453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec2(17.0, 9.0);
        a *= 0.5;
    }
    return v;
}

// Light above full brightness saturates softly towards 1.6 instead of growing without bound.
vec3 shoulder(vec3 light) {
    vec3 over = max(light - 1.0, 0.0);
    return min(light, vec3(1.0)) + 0.6 * (1.0 - exp(-over / 0.6));
}

void main() {
    vec4 scene = texture2D(u_texture, v_uv);
    vec3 raw = texture2D(u_light, v_uv).rgb * 2.0;

    // Fog in art-pixel steps, drifting slowly; it glows where lights reach it.
    vec2 world = u_view.xy + v_uv * u_view.zw;
    vec2 wp = floor(world * 16.0) / 16.0;
    float n = fbm(wp * 0.42 + vec2(u_time * 0.07, u_time * 0.03));
    // Only over the drawn level: the void stays black. Pure black counts as void, so the black
    // inside of walls merges with the darkness around them.
    float solid = scene.a * step(1.0 / 255.0, max(scene.r, max(scene.g, scene.b)));
    float fog = smoothstep(0.38, 0.9, n) * solid;
    vec3 col = scene.rgb * shoulder(u_ambient + raw);
    col += fog * (u_ambient * 1.3 + raw * 0.7) * 0.18 * u_fog;

    // Split toning between the shadow and light tints.
    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col *= mix(u_shadowTint, u_lightTint, smoothstep(0.05, 0.7, luma));

    col = max(col, 0.0);
    col += texture2D(u_bloomNear, v_uv).rgb * 0.4;
    col += texture2D(u_bloomFar, v_uv).rgb * 0.5;
    float alpha = max(scene.a, min(max(col.r, max(col.g, col.b)), 1.0));
    gl_FragColor = vec4(min(col, vec3(alpha)), alpha);
}
