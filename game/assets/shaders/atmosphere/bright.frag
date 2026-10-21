// Bloom source: the part of the scene lit beyond full brightness, so only strong light glows and
// bright textures in fully lit rooms do not.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_uv;
uniform sampler2D u_texture;   // composed scene before lighting
uniform sampler2D u_light;     // light buffer (half intensity)
uniform vec3 u_ambient;

void main() {
    vec3 scene = texture2D(u_texture, v_uv).rgb;
    vec3 light = u_ambient + texture2D(u_light, v_uv).rgb * 2.0;
    gl_FragColor = vec4(scene * max(light - 1.0, 0.0), 1.0);
}
