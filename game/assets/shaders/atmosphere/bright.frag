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
uniform vec4 u_view;           // world rect of the scene texture: x, y, width, height
uniform vec4 u_lightArea;      // world rect of the light texture and of this pass: x, y, width, height
uniform vec3 u_ambient;

void main() {
    // This pass covers the light buffer's area; the scene is looked up at the same place.
    vec2 world = u_lightArea.xy + v_uv * u_lightArea.zw;
    vec3 scene = texture2D(u_texture, (world - u_view.xy) / u_view.zw).rgb;
    vec3 light = u_ambient + texture2D(u_light, v_uv).rgb * 2.0;
    gl_FragColor = vec4(scene * max(light - 1.0, 0.0), 1.0);
}
