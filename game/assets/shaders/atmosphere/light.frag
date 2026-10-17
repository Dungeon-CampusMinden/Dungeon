// One point light, drawn as a quad around the light into the additive light buffer. Walls cast
// hard, tile-aligned shadows: the ray from the light to each art pixel is traced through the
// occlusion map, one texel per tile: red = blocks light; for walls green, blue and alpha hold the
// floor visible left of, right of and below the wall's art, in sixteenths of a tile; for other
// tiles green marks open ground, as opposed to the void. The buffer stores half the light so values
// up to 2 survive the 8-bit target.
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

// 1 if the ray from the light to the target crosses no blocking tile before the target's tile.
// The tile of the wall a pixel belongs to never shadows it when ownWall is set, so walls are lit
// as a whole and the light does not cut diagonal wedges into the brickwork.
float visible(vec2 target, vec2 targetTile, bool ownWall, vec2 wallTile) {
    vec2 toTarget = target - u_lightPos;
    vec2 cell = floor(u_lightPos);
    vec2 stepDir = vec2(toTarget.x >= 0.0 ? 1.0 : -1.0, toTarget.y >= 0.0 ? 1.0 : -1.0);
    vec2 tDelta = 1.0 / max(abs(toTarget), vec2(1e-5));
    vec2 tMax = vec2(
        (stepDir.x > 0.0 ? cell.x + 1.0 - u_lightPos.x : u_lightPos.x - cell.x) * tDelta.x,
        (stepDir.y > 0.0 ? cell.y + 1.0 - u_lightPos.y : u_lightPos.y - cell.y) * tDelta.y);
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
        vec2 gap = abs(cell - wallTile);
        if (ownWall && max(gap.x, gap.y) <= 1.0) continue;
        if (blocked(cell)) return 0.0;
    }
    return 1.0;
}

float lightAt(vec2 p) {
    vec2 tile = floor(p);
    vec4 occ = occlusion(tile);
    if (occ.r < 0.5) return visible(p, tile, false, tile);

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
        if (u_lightPos.x > tile.x + left) return 0.0;
        return visible(vec2(tile.x - EDGE, p.y), west, false, tile);
    }
    if (openE && f.x > 1.0 - right) {
        if (u_lightPos.x < tile.x + 1.0 - right) return 0.0;
        return visible(vec2(tile.x + 1.0 + EDGE, p.y), east, false, tile);
    }
    if (openS && f.y < bottom) {
        if (u_lightPos.y > tile.y + bottom) return 0.0;
        return visible(vec2(p.x, tile.y - EDGE), below, false, tile);
    }

    // The art takes light through each side that looks onto open ground, where light reaches the
    // ground right in front of it. Each side fades out over a tile and a half as the light passes
    // behind it, so walking past a wall does not switch its light on and off.
    if (openS || openE || openW) {
        float lit = 0.0;
        if (openS) {
            float facing = clamp((tile.y + 1.5 - u_lightPos.y) / 1.5, 0.0, 1.0);
            if (facing > 0.0) lit = max(lit, facing * visible(vec2(p.x, tile.y - EDGE), below, false, tile));
        }
        if (openE) {
            float facing = clamp((u_lightPos.x - tile.x + 0.5) / 1.5, 0.0, 1.0);
            if (facing > 0.0) lit = max(lit, facing * visible(vec2(tile.x + 1.0 + EDGE, p.y), east, false, tile));
        }
        if (openW) {
            float facing = clamp((tile.x + 1.5 - u_lightPos.x) / 1.5, 0.0, 1.0);
            if (facing > 0.0) lit = max(lit, facing * visible(vec2(tile.x - EDGE, p.y), west, false, tile));
        }
        return lit;
    }
    // Inside a wall run: lit where the light reaches it past its own wall.
    return visible(p, tile, true, tile);
}

void main() {
    // Light per art pixel (16 per tile) keeps the falloff as crisp as the sprites.
    vec2 p = (floor(v_world * 16.0) + 0.5) / 16.0;
    float d = length(p - u_lightPos);
    if (d >= u_radius) discard;

    float lit = lightAt(p);
    if (lit <= 0.0) discard;

    float x = d / u_radius;
    float x2 = x * x;
    float att = (1.0 - x2) * (1.0 - x2) / (1.0 + 2.5 * x2);
    gl_FragColor = vec4(u_color * att * lit * 0.5, 1.0);
}
