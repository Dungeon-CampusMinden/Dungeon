// Ambient occlusion walls cast onto the floor, drawn over the floor in steps of one art pixel: alpha
// is the share of light kept away. The distances to the wall art around an art pixel follow from
// the occlusion map (see light.frag): walls, closed hidden doors and the void cast occlusion, closed
// normal doors do not. In the oblique view a wall shows its face to the south, so the crease at its
// foot is dark and long, the one beside it is lighter and shorter, and the one behind it is hidden.
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
uniform vec2 u_reach;        // art pixels the occlusion reaches beside and below wall art

// Darkness of the first art pixel beside and below wall art.
const float SIDE_DARK = 0.42;
const float BELOW_DARK = 0.62;
// Bands of darkness, evenly spaced to the eye (by square root).
const float BANDS = 9.0;

// Share of light the creases beside and below walls leave, and the darkness around outer corners.
float creases = 1.0;
float corner = 0.0;

vec4 occlusion(vec2 tile) {
    vec2 uv = (tile - u_occRect.xy + 0.5) / u_occRect.zw;
    if (uv.x < 0.0 || uv.y < 0.0 || uv.x > 1.0 || uv.y > 1.0) return vec4(1.0, 0.0, 0.0, 0.0);
    return texture2D(u_occlusion, uv);
}

bool casts(vec4 occ) {
    return occ.r > 0.75 || (occ.r < 0.5 && occ.g < 0.5);
}

bool receives(vec4 occ) {
    return occ.r < 0.5 && occ.b > 0.5;
}

float inset(float channel) {
    return floor(channel * 255.0 / 16.0 + 0.5);
}

// Darkness at a gap from wall art, in art pixels beside (x) and below (y) it. Around an outer
// corner, where both are above 0, an ellipse joins the crease beside the art to the one below it.
float dark(vec2 gap) {
    vec2 g = gap / (u_reach + 1.0);
    float t = length(g);
    if (t >= 1.0) return 0.0;
    float below = g.y * g.y / (t * t);
    float rise = mix((u_reach.x + 1.0) / u_reach.x, (u_reach.y + 1.0) / u_reach.y, below) * (1.0 - t);
    return mix(SIDE_DARK, BELOW_DARK, below) * pow(min(rise, 1.0), 1.6);
}

// Adds the occlusion at art pixel q of a tile by the wall art of the tile at an offset to it. Beside
// the art it counts as that many extra art pixels further away.
void add(vec2 q, vec2 offset, vec4 occ, float extra) {
    if (!casts(occ)) return;
    vec2 origin = offset * 16.0;
    float left = origin.x + inset(occ.g);
    float right = origin.x + 16.0 - inset(occ.b);
    vec2 gap = max(vec2(max(left - q.x, q.x - right + 1.0), origin.y + inset(occ.a) - q.y), 0.0);
    if (gap.y < 0.5) gap.x += extra;
    if (gap.x > 0.5 && gap.y > 0.5) corner = max(corner, dark(gap));
    else creases *= 1.0 - dark(gap);
}

float occluded() {
    // Zoomed out, the centre of a fragment can lie on the border between two art pixels; the nudge
    // keeps it on the same side of that border while the camera moves.
    vec2 px = floor(v_world * 16.0 + 0.01);
    vec2 tile = floor(px / 16.0);
    vec2 q = px - tile * 16.0;

    vec4 self = occlusion(tile);
    vec4 w = occlusion(tile + vec2(-1.0, 0.0));
    vec4 e = occlusion(tile + vec2(1.0, 0.0));
    // Of a wall's tile only the floor seen beside or below its art receives occlusion.
    bool strip = self.r > 0.5
        && ((q.x < inset(self.g) && receives(w))
            || (q.x >= 16.0 - inset(self.b) && receives(e))
            || (q.y < inset(self.a) && receives(occlusion(tile + vec2(0.0, -1.0)))));
    if (!receives(self) && !strip) return 0.0;

    vec4 n = occlusion(tile + vec2(0.0, 1.0));
    vec4 nw = occlusion(tile + vec2(-1.0, 1.0));
    vec4 ne = occlusion(tile + vec2(1.0, 1.0));
    // Where a wall ends to the north, the crease beside it narrows over the last two art pixels.
    float end = max(q.y - 13.0, 0.0);
    if (strip) add(q, vec2(0.0), self, casts(n) ? 0.0 : end);
    add(q, vec2(-1.0, 0.0), w, casts(nw) ? 0.0 : end);
    add(q, vec2(1.0, 0.0), e, casts(ne) ? 0.0 : end);
    add(q, vec2(0.0, 1.0), n, 0.0);
    // Next to a closer wall the corner of a diagonal one adds nothing.
    add(q, vec2(-1.0, 1.0), nw, 0.0);
    add(q, vec2(1.0, 1.0), ne, 0.0);

    float band = floor(sqrt(max(1.0 - creases, corner)) * BANDS + 0.5) / BANDS;
    return band * band;
}

void main() {
    gl_FragColor = vec4(0.0, 0.0, 0.0, occluded());
}
