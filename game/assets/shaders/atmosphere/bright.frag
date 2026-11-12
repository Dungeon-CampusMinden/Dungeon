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
uniform sampler2D u_bases;     // base of the upright entity under each scene pixel
uniform vec2 u_baseRange;      // lowest base and the range the 16 bits of u_bases span
uniform vec4 u_view;           // world rect of the scene texture: x, y, width, height
uniform vec4 u_lightArea;      // world rect of the light texture and of this pass: x, y, width, height
uniform vec3 u_ambient;

// Where the light falls at p, as in composite.frag: upright entities in their lowest art pixel row.
vec2 litPoint(vec2 p, vec2 uv) {
    vec4 b = texture2D(u_bases, uv);
    if (b.b < 0.5) return p;
    float feet = u_baseRange.x + (b.r * 65280.0 + b.g * 255.0) / 65535.0 * u_baseRange.y;
    return vec2(p.x, min(p.y, (floor(feet * 16.0 + 0.5) + 0.5) / 16.0));
}

void main() {
    // This pass covers the light buffer's area; the scene is looked up at the same place. Outside
    // the scene, like the strip below the view the light buffer covers for upright entities, nothing
    // glows.
    vec2 world = u_lightArea.xy + v_uv * u_lightArea.zw;
    vec2 uv = (world - u_view.xy) / u_view.zw;
    if (uv.x < 0.0 || uv.y < 0.0 || uv.x > 1.0 || uv.y > 1.0) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }
    vec3 scene = texture2D(u_texture, uv).rgb;
    vec2 lit = litPoint(world, uv);
    vec3 light = u_ambient + texture2D(u_light, (lit - u_lightArea.xy) / u_lightArea.zw).rgb * 2.0;
    gl_FragColor = vec4(scene * max(light - 1.0, 0.0), 1.0);
}
