// One point light, drawn as a quad around the light into the additive light buffer. Walls cast
// hard, tile-aligned shadows: the ray from the light to each art pixel is traced through the
// occlusion map (one texel per tile, red = blocks light). The buffer stores half the light so
// values up to 2 survive the 8-bit target.
#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

varying vec2 v_world;

uniform sampler2D u_occlusion;
uniform vec4 u_occRect;      // origin tile xy, size in tiles zw
uniform vec2 u_lightPos;
uniform float u_radius;
uniform vec3 u_color;        // colour * intensity

float blocked(vec2 tile) {
    vec2 uv = (tile - u_occRect.xy + 0.5) / u_occRect.zw;
    if (uv.x < 0.0 || uv.y < 0.0 || uv.x > 1.0 || uv.y > 1.0) return 1.0;
    return texture2D(u_occlusion, uv).r;
}

void main() {
    // Light per art pixel (16 per tile) keeps the falloff as crisp as the sprites.
    vec2 p = (floor(v_world * 16.0) + 0.5) / 16.0;
    vec2 toPixel = p - u_lightPos;
    float d = length(toPixel);
    if (d >= u_radius) discard;

    vec2 pTile = floor(p);
    vec2 lTile = floor(u_lightPos);
    bool inWall = blocked(pTile) > 0.5;
    // A wall face only faces south: it fades out as the light passes behind it, over a tile and a
    // half, so walking past a wall does not switch its light on and off. The face is lit where
    // light reaches the floor right in front of it, so the rest of its wall run does not shadow it.
    float facing = 1.0;
    vec2 target = p;
    vec2 targetTile = pTile;
    bool face = inWall && blocked(pTile - vec2(0.0, 1.0)) < 0.5;
    if (face) {
        facing = clamp((pTile.y + 1.5 - u_lightPos.y) / 1.5, 0.0, 1.0);
        if (facing <= 0.0) discard;
        targetTile = pTile - vec2(0.0, 1.0);
        target = vec2(p.x, pTile.y - 0.5 / 16.0);
    }
    vec2 toTarget = target - u_lightPos;

    // Walk every tile the ray crosses (Amanatides & Woo), from the light towards the target.
    vec2 cell = lTile;
    vec2 stepDir = vec2(toTarget.x >= 0.0 ? 1.0 : -1.0, toTarget.y >= 0.0 ? 1.0 : -1.0);
    vec2 tDelta = 1.0 / max(abs(toTarget), vec2(1e-5));
    vec2 tMax = vec2(
        (stepDir.x > 0.0 ? lTile.x + 1.0 - u_lightPos.x : u_lightPos.x - lTile.x) * tDelta.x,
        (stepDir.y > 0.0 ? lTile.y + 1.0 - u_lightPos.y : u_lightPos.y - lTile.y) * tDelta.y);
    for (int i = 0; i < 48; i++) {
        if (min(tMax.x, tMax.y) >= 1.0) break;
        if (tMax.x < tMax.y) {
            cell.x += stepDir.x;
            tMax.x += tDelta.x;
        } else {
            cell.y += stepDir.y;
            tMax.y += tDelta.y;
        }
        if (cell == targetTile) break;
        // Other walls are lit as a whole: the wall they belong to never shadows them, so the
        // light does not cut diagonal wedges into the brickwork.
        vec2 gap = abs(cell - pTile);
        bool ownWall = inWall && !face && max(gap.x, gap.y) <= 1.0;
        if (!ownWall && blocked(cell) > 0.5) discard;
    }

    float x = d / u_radius;
    float x2 = x * x;
    float att = (1.0 - x2) * (1.0 - x2) / (1.0 + 2.5 * x2);
    gl_FragColor = vec4(u_color * att * facing * 0.5, 1.0);
}
