// One point light, drawn as a quad around the light into the additive light buffer. Walls cast
// tile-aligned shadows: rays from the light to each art pixel are traced through the occlusion
// map. Each art pixel averages 2 x 2 points inside it, so a shadow edge that cuts through it gives
// it an intermediate brightness. With u_soft the light shines from a small disc instead of a point:
// the 2 x 2 points take rays from 16 points spread over the disc, so a shadow's edge fades over a
// few art pixels (penumbra). The distance falloff stays measured from the light's centre.
// The occlusion map holds one texel per tile: red = blocks light; for walls green, blue and alpha
// hold the floor visible left of, right of and below the wall's art, in sixteenths of a tile; for
// other tiles green marks open ground, as opposed to the void. The buffer stores half the light so
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
uniform float u_soft;        // 1: the light is a disc of SOFT_RADIUS, 0: a point
uniform float u_edge;        // 1: visibility at 2 x 2 points per art pixel, 0: at its centre

const float SOFT_RADIUS = 0.3;
// Offset of the 2 x 2 points from the art pixel's centre: a quarter art pixel.
const float SUB = 0.25 / 16.0;

const float EDGE = 0.5 / 16.0;

vec4 occlusion(vec2 tile) {
    vec2 uv = (tile - u_occRect.xy + 0.5) / u_occRect.zw;
    if (uv.x < 0.0 || uv.y < 0.0 || uv.x > 1.0 || uv.y > 1.0) return vec4(1.0, 0.0, 0.0, 0.0);
    return texture2D(u_occlusion, uv);
}

bool blocked(vec2 tile) {
    return occlusion(tile).r > 0.5;
}

// Ground a wall side can look onto; light passes over the void, but walls do not face it.
bool ground(vec2 tile) {
    vec4 occ = occlusion(tile);
    return occ.r < 0.5 && occ.g > 0.5;
}

float inset(float channel) {
    return floor(channel * 255.0 / 16.0 + 0.5) / 16.0;
}

// 1 if the ray from the light point to the target crosses no blocking tile before the target's
// tile.
float visible(vec2 light, vec2 target, vec2 targetTile) {
    vec2 toTarget = target - light;
    vec2 cell = floor(light);
    vec2 stepDir = vec2(toTarget.x >= 0.0 ? 1.0 : -1.0, toTarget.y >= 0.0 ? 1.0 : -1.0);
    vec2 tDelta = 1.0 / max(abs(toTarget), vec2(1e-5));
    vec2 tMax = vec2(
        (stepDir.x > 0.0 ? cell.x + 1.0 - light.x : light.x - cell.x) * tDelta.x,
        (stepDir.y > 0.0 ? cell.y + 1.0 - light.y : light.y - cell.y) * tDelta.y);
    // Walk every tile the ray crosses (Amanatides & Woo), from the light towards the target.
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
        if (blocked(cell)) return 0.0;
    }
    return 1.0;
}

// How far a light on this side of the tile reaches a wall: 1 beyond the tile, fading to 0 half a
// tile inside it.
float facing(float lightCoord, float tileCoord, float dir) {
    if (dir > 0.0) return clamp((lightCoord - tileCoord + 0.5) / 1.5, 0.0, 1.0);
    if (dir < 0.0) return clamp((tileCoord + 1.5 - lightCoord) / 1.5, 0.0, 1.0);
    return 1.0;
}

// Light a wall pixel takes through the side of its tile in direction dir (a side or a diagonal):
// where the light reaches the ground right outside that side.
float side(vec2 light, vec2 p, vec2 tile, vec2 dir) {
    float f = facing(light.x, tile.x, dir.x) * facing(light.y, tile.y, dir.y);
    if (f <= 0.0) return 0.0;
    vec2 target = vec2(
        dir.x > 0.0 ? tile.x + 1.0 + EDGE : dir.x < 0.0 ? tile.x - EDGE : p.x,
        dir.y > 0.0 ? tile.y + 1.0 + EDGE : dir.y < 0.0 ? tile.y - EDGE : p.y);
    return f * visible(light, target, tile + dir);
}

// Light that reaches point p from one light point: 0 or 1, on wall art a fade between them. Wall
// art traces from the same disc point as the floor, so it keeps agreeing with the floor in front.
float lightAt(vec2 light, vec2 p) {
    vec2 tile = floor(p);
    vec4 occ = occlusion(tile);
    if (occ.r < 0.5) return visible(light, p, tile);

    vec2 below = tile - vec2(0.0, 1.0);
    vec2 east = tile + vec2(1.0, 0.0);
    vec2 west = tile - vec2(1.0, 0.0);
    bool openS = ground(below);
    bool openE = ground(east);
    bool openW = ground(west);
    vec2 f = p - tile;
    float left = inset(occ.g);
    float right = inset(occ.b);
    float bottom = inset(occ.a);

    // Floor seen beside or below the wall's art belongs to the open tile on that side: it is lit
    // only from that side of the art.
    if (openW && f.x < left) {
        if (light.x > tile.x + left) return 0.0;
        return visible(light, vec2(tile.x - EDGE, p.y), west);
    }
    if (openE && f.x > 1.0 - right) {
        if (light.x < tile.x + 1.0 - right) return 0.0;
        return visible(light, vec2(tile.x + 1.0 + EDGE, p.y), east);
    }
    if (openS && f.y < bottom) {
        if (light.y > tile.y + bottom) return 0.0;
        return visible(light, vec2(p.x, tile.y - EDGE), below);
    }

    // The art takes light through each side that looks onto open ground, where light reaches the
    // ground right in front of it; a wall without such a side, like the corner of a room, through
    // the ground touching it diagonally. Each side fades out over a tile and a half as the light
    // passes behind it, so walking past a wall does not switch its light on and off.
    bool openN = ground(tile + vec2(0.0, 1.0));
    float lit = 0.0;
    if (openS) lit = max(lit, side(light, p, tile, vec2(0.0, -1.0)));
    if (openN) lit = max(lit, side(light, p, tile, vec2(0.0, 1.0)));
    if (openE) lit = max(lit, side(light, p, tile, vec2(1.0, 0.0)));
    if (openW) lit = max(lit, side(light, p, tile, vec2(-1.0, 0.0)));
    if (openS || openN || openE || openW) return lit;
    for (int i = 0; i < 4; i++) {
        vec2 dir = vec2(i < 2 ? 1.0 : -1.0, mod(float(i), 2.0) < 0.5 ? 1.0 : -1.0);
        if (ground(tile + dir)) lit = max(lit, side(light, p, tile, dir));
    }
    return lit;
}

// Point i of 16 on the light's disc; a golden-angle spiral spreads them evenly. A point in a wall
// tile other than the light's own, or behind a wall corner, falls back to the centre, so no light
// starts behind a wall.
vec2 discPoint(int i) {
    float k = float(i) + 0.5;
    float a = k * 2.39996;
    vec2 q = u_lightPos + SOFT_RADIUS * sqrt(k / 16.0) * vec2(cos(a), sin(a));
    vec2 own = floor(u_lightPos);
    vec2 cell = floor(q);
    if (cell == own) return q;
    bool corner = cell.x != own.x && cell.y != own.y
        && blocked(vec2(cell.x, own.y)) && blocked(vec2(own.x, cell.y));
    return blocked(cell) || corner ? u_lightPos : q;
}

void main() {
    // Light per art pixel (16 per tile) keeps the falloff as crisp as the sprites.
    vec2 p = (floor(v_world * 16.0) + 0.5) / 16.0;
    float d = length(p - u_lightPos);
    if (d >= u_radius) discard;

    // Ray i ends at point i % 4 of the 2 x 2 points; soft light sends the 16 rays from 16 disc
    // points, a point light only the 4 from its centre.
    int rays = u_soft > 0.5 ? 16 : 4;
    float lit = 0.0;
    for (int i = 0; i < 16; i++) {
        if (i >= rays) break;
        float corner = mod(float(i), 4.0);
        vec2 sub = p + vec2(mod(corner, 2.0) < 0.5 ? -SUB : SUB, corner < 1.5 ? -SUB : SUB);
        if (u_edge < 0.5) sub = p;
        lit += lightAt(u_soft > 0.5 ? discPoint(i) : u_lightPos, sub);
    }
    lit /= float(rays);
    if (lit <= 0.0) discard;

    float x = d / u_radius;
    float x2 = x * x;
    float att = (1.0 - x2) * (1.0 - x2) / (1.0 + 2.5 * x2);
    gl_FragColor = vec4(u_color * att * lit * 0.5, 1.0);
}
