#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
varying vec2 worldPos;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_resolution;

// ----- Custom uniforms -----
uniform sampler2D u_dudv;
uniform vec4 u_waterRegion; // x,y = bottom-left corner, z,w = size
uniform vec4 u_waterColor;
uniform float u_speed;
uniform float u_repeat;
uniform float u_foamMinWidth; // pixels
uniform float u_foamMaxWidth; // pixels
uniform float u_lineInterval; // seconds between foam lines leaving the shore

// ----- Tuning -----
#define WAVE_COUNT 10
#define WAVE_DRAG 0.28        // how strongly waves push each other around (clumping)
#define TIME_SCALE 5.0        // maps u_speed to wave phase speed
#define NORMAL_STRENGTH 0.35  // steepness of the lit slopes
#define DUDV_SCALE 0.04       // dudv texture tiles per pattern unit (kept low to avoid aliasing)
#define DETAIL_STRENGTH 0.4   // influence of the dudv ripples on the surface normal
#define WARP_STRENGTH 0.35    // how much the dudv bends the wave crests
#define SHALLOW_WIDTH 1.6     // tiles over which the water gets shallow towards the shore
#define CALM_WIDTH 2.0        // tiles over which the waves calm down towards the shore
#define LINE_SPEED 0.25       // tiles per second a foam line travels away from the shore
#define LINE_FADE 1.1         // tiles a foam line travels before it has faded out

// ----- Custom functions -----

// Single wave with a sharp crest and a wide trough: exp(sin(x) - 1).
// Returns (height, -d(height)/dx) so the caller can drag later waves along the slope.
vec2 wave(vec2 p, vec2 dir, float frequency, float phase) {
  float x = dot(dir, p) * frequency + phase;
  float h = exp(sin(x) - 1.0);
  return vec2(h, -h * cos(x));
}

// Sum of travelling waves in pseudo-random directions. Each wave is displaced by the slope
// of the previous ones, which makes crests clump together like real chop instead of forming
// a regular interference grid.
float getWaterHeight(vec2 p, float t) {
  float angle = 0.0;
  float frequency = 1.0;
  float timeMultiplier = 1.0;
  float weight = 1.0;
  float sumValues = 0.0;
  float sumWeights = 0.0;

  for (int i = 0; i < WAVE_COUNT; i++) {
    vec2 dir = vec2(sin(angle), cos(angle));
    vec2 res = wave(p, dir, frequency, t * timeMultiplier);
    p += dir * res.y * weight * WAVE_DRAG;
    sumValues += res.x * weight;
    sumWeights += weight;

    weight *= 0.8;
    frequency *= 1.18;
    // Deep water dispersion: phase speed grows with sqrt(frequency).
    timeMultiplier *= 1.086;
    angle += 2.399963; // golden angle spreads the directions evenly
  }

  return sumValues / sumWeights;
}

// Static fbm used as a depth map of the water floor. It intentionally does not move.
float getDepth(vec2 p) {
  float value = 0.0;
  float amplitude = 0.5;
  float frequency = 1.0;

  vec2 shift = vec2(100.0);
  mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));

  for (int i = 0; i < 5; i++) {
    vec2 waveDirection = vec2(cos(float(i) * 1.45), sin(float(i) * 3.12));
    vec2 offset = waveDirection * 5.0 * (0.15 + float(i) * 0.05);
    value += amplitude * snoise(p * frequency + offset);
    p = rot * p * 2.0 + shift;
    amplitude *= 0.5;
  }

  return value * 0.5 + 0.5;
}

vec2 sampleDudv(vec2 p) {
  return texture2D(u_dudv, fract(p)).rg * 2.0 - 1.0;
}

// ----- Main -----
void main() {
  vec4 color = unPma(texture2D(u_texture, uv));

  // The shader runs as an entity shader on a sprite that exactly covers the water region.
  vec2 waterPos = u_waterRegion.xy + uv * u_waterRegion.zw;
  float t = u_time * u_speed * TIME_SCALE;

  // Distance to the nearest shore in tiles, wobbled by static noise so the edge looks natural.
  vec2 toMin = waterPos - u_waterRegion.xy;
  vec2 toMax = u_waterRegion.xy + u_waterRegion.zw - waterPos;
  float shoreDist = min(min(toMin.x, toMin.y), min(toMax.x, toMax.y));
  float shoreNoise = snoise(waterPos * 0.9 + vec2(37.0, 11.0));
  float shore = max(shoreDist + shoreNoise * 0.12, 0.0);
  float shallow = 1.0 - smoothstep(0.0, SHALLOW_WIDTH, shore);
  float calm = smoothstep(0.0, CALM_WIDTH, shore);

  vec2 p = waterPos * u_repeat * 0.9;

  // Two dudv layers scrolling in different directions at different scales. Their sum never
  // repeats visibly and is used both to bend the wave crests and as fine ripple detail.
  mat2 rot = mat2(0.8, 0.6, -0.6, 0.8);
  vec2 d1 = sampleDudv(p * DUDV_SCALE + vec2(0.021, 0.013) * t);
  vec2 d2 = sampleDudv(rot * p * (DUDV_SCALE * 1.37) - vec2(0.017, -0.024) * t);
  vec2 detail = (d1 + d2) * 0.5;

  // Slight large scale variation between calmer and choppier areas.
  float chop = smoothstep(-0.6, 0.8, snoise(waterPos * 0.07 + vec2(0.03, 0.02) * t));

  vec2 wp = p + detail * WARP_STRENGTH;
  float e = 0.03;
  float h = getWaterHeight(wp, t);
  float hx = getWaterHeight(wp + vec2(e, 0.0), t);
  float hy = getWaterHeight(wp + vec2(0.0, e), t);
  vec2 slope = vec2(hx - h, hy - h) / e;

  float strength = NORMAL_STRENGTH * mix(0.72, 0.98, chop) * mix(0.3, 1.0, calm);
  vec3 normal = normalize(vec3(-slope * strength + detail * DETAIL_STRENGTH * strength, 1.0));

  // Top-down lighting: light comes from the top left, camera looks straight down.
  vec3 lightDir = normalize(vec3(-0.45, 0.6, 0.65));
  float diffuse = dot(normal, lightDir) - lightDir.z; // 0 on flat water
  // Sun glints use a steeper light so they appear on moderately tilted ripples.
  vec3 sunHalf = normalize(normalize(vec3(-0.3, 0.4, 0.87)) + vec3(0.0, 0.0, 1.0));
  float specular = pow(max(dot(normal, sunHalf), 0.0), 60.0);

  vec3 base = u_waterColor.rgb;
  vec3 deepWater = max(base - vec3(0.1), vec3(0.0));
  vec3 shallowWater = min(base * 1.15 + vec3(0.03, 0.14, 0.1), vec3(1.0));
  vec3 bright = min(base + vec3(0.35), vec3(1.0));
  vec3 foamColor = mix(bright, vec3(1.0), 0.6);

  float depth = getDepth(waterPos * 0.05);
  vec3 body = mix(deepWater, base, smoothstep(0.24, 0.44, depth));
  body = mix(body, shallowWater, shallow * shallow);
  body *= 0.84 + 0.34 * h;
  body *= 1.0 + diffuse * 2.5;

  // Light catches the front of each crest, giving the typical bright wave lines.
  float crest = smoothstep(0.04, 0.08, diffuse) * smoothstep(0.35, 0.55, h) * 0.5;
  float glint = smoothstep(0.55, 0.8, specular) * mix(0.77, 1.0, chop);
  float highlight = clamp(crest + glint, 0.0, 1.0);
  vec3 water = mix(clamp(body, 0.0, 1.0), bright, highlight);

  // Foam rim: always at least the minimum width, randomly and slowly growing outwards.
  float pixel = max(u_waterRegion.z / u_resolution.x, u_waterRegion.w / u_resolution.y);
  float rimNoise = snoise(waterPos * 1.1 + vec2(0.013, -0.009) * t);
  float rimPixels = mix(u_foamMinWidth, max(u_foamMaxWidth, u_foamMinWidth),
    smoothstep(-0.2, 1.0, rimNoise));
  float rimWidth = pixel * rimPixels;
  float foam = step(shoreDist, rimWidth);

  // Wavy foam lines that leave the rim and travel out into the water while fading.
  float lineDist = shoreDist - rimWidth;
  float wobble = snoise(waterPos * 1.4 + vec2(-0.011, 0.017) * t) * 0.08;
  float lineSpacing = LINE_SPEED * max(u_lineInterval, 0.01);
  float linePhase = fract(((lineDist + wobble) - u_time * LINE_SPEED) / lineSpacing);
  float lineFade = 1.0 - smoothstep(0.0, LINE_FADE, lineDist);
  float lineWidth = pixel / lineSpacing * mix(0.6, 1.4, lineFade);
  float broken = smoothstep(-0.3, 0.2, snoise(waterPos * 0.9 + vec2(3.0, -7.0) + t * 0.02));
  float lines = step(linePhase, lineWidth) * step(0.0, lineDist) * lineFade * broken * 0.8;
  water = mix(water, foamColor, clamp(max(foam, lines), 0.0, 1.0));

  gl_FragColor = pma(vec4(water, color.a * u_waterColor.a));
}
