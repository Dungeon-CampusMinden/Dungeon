#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;
uniform vec4 u_entityBounds;

// ----- Custom uniforms -----
uniform sampler2D u_bgTexture;
uniform float u_ballSize;
uniform float u_curvedEdgeWidth;
uniform vec2 u_ballOffset;
uniform vec4 u_textureRegion;
uniform vec4 u_ballColor;
uniform float u_glowStrength;
uniform vec4 u_glowColor;
uniform float u_motes;          // share of the magic motes shown around the ball, 0 for none

const float BALL_TEXTURE_VERTICAL_GAP = 0.08;
const float glowRingWidth = 0.012;
const float glowExtend = 0.1;
const float glowDissipation = 1.6;
const float glowRevolveSpeed = 0.25;
const float glowSpeed = 0.12;
const int MOTES = 72;
const float MOTE_PIXEL = 1.5;   // art pixels per mote pixel

float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float ballNoise(vec2 p) {
  vec2 cell = floor(p);
  vec2 local = fract(p);
  local = local * local * (3.0 - 2.0 * local);

  return mix(
      mix(hash(cell), hash(cell + vec2(1.0, 0.0)), local.x),
      mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), local.x),
      local.y);
}

// Magic motes leave the rim of the ball, drift outward and up and fade; each mote repeats with a
// new start on the rim. Returns the brightest mote core at the given mote pixel, its white-hot
// share and the soft halo of all motes around it.
vec3 motes(vec2 pixel, float radius, float pixelSize) {
  vec3 result = vec3(0.0);
  for (int i = 0; i < MOTES; i++) {
    float fi = float(i);
    float seed = hash(vec2(fi, 3.7));
    float period = mix(2.4, 4.4, hash(vec2(fi, 9.1)));
    float t = u_time / period + seed;
    float cycle = floor(t);
    float life = fract(t);
    if (hash(vec2(fi, cycle)) >= u_motes) continue;

    // Mostly from the upper half of the rim.
    float angle = (hash(vec2(cycle, fi * 1.3)) * 1.4 - 0.2) * PI;
    vec2 dir = vec2(cos(angle), sin(angle));
    float rise = mix(0.08, 0.2, hash(vec2(fi, cycle + 5.0)));
    vec2 pos = dir * (radius + 0.008 + 0.04 * life)
        + vec2(0.012 * sin(life * 9.0 + seed * 6.28), rise * life);
    if (length(pos) < radius) continue;

    vec2 offset = pixel - floor(pos / pixelSize);
    float d2 = dot(offset, offset);
    if (d2 > 25.0) continue;
    float twinkle = 0.65 + 0.35 * sin(u_time * 10.0 + seed * 40.0);
    float fade = smoothstep(0.0, 0.12, life) * (1.0 - smoothstep(0.55, 1.0, life));
    // A quarter of the motes are larger sparkles: a cross while they are fresh.
    bool sparkle = hash(vec2(fi, cycle + 2.0)) < 0.25 && life < 0.5;
    float shape = d2 == 0.0 ? 1.0 : sparkle && d2 == 1.0 ? 0.55 : 0.0;
    float b = shape * fade * twinkle;
    if (b > result.x) result.xy = vec2(b, twinkle * twinkle * (sparkle ? 1.0 : 0.5));
    result.z += exp(-d2 * 0.22) * fade * twinkle * 0.5;
  }
  return result;
}

void main() {
  float shortSide = min(u_resolution.x, u_resolution.y);
  vec2 center = vec2(0.5) + u_ballOffset;
  vec2 ballPosition = (uv - center) * u_resolution / shortSide;
  float radius = u_ballSize * 0.5;
  float distanceFromCenter = length(ballPosition);

  vec4 background = unPma(texture2D(u_bgTexture, vec2(uv.x, 1.0 - uv.y)));
  vec4 result = pma(background);

  if (distanceFromCenter <= radius) {
    vec2 spherePosition = ballPosition / radius;
    float sphereZ = sqrt(max(0.0, 1.0 - dot(spherePosition, spherePosition)));
    float textureLimit = 1.0 - BALL_TEXTURE_VERTICAL_GAP;
    float maxLatitude = asin(textureLimit);
    vec2 sceneUv = vec2(
        atan(spherePosition.x, sphereZ) / PI + 0.5,
        asin(clamp(spherePosition.y, -textureLimit, textureLimit))
            / (2.0 * maxLatitude) + 0.5);
    if (u_curvedEdgeWidth > 0.0) {
      // Keep the center unchanged; bend the sampling coordinates only within the rim.
      // Sampling once avoids double images where the two projections meet.
      float distanceToEdgePixels = (radius - distanceFromCenter) * shortSide;
      float curvedWeight = 1.0 - smoothstep(0.0, u_curvedEdgeWidth, distanceToEdgePixels);
      sceneUv = mix(spherePosition * 0.5 + 0.5, sceneUv, curvedWeight);
    }
    vec4 ballLayer = pma(u_ballColor);

    if (u_curvedEdgeWidth > 0.0 || abs(spherePosition.y) <= textureLimit) {
      vec2 mappedSceneUv = u_textureRegion.xy + sceneUv * u_textureRegion.zw;
      // The scene over the ball colour, which shows where the scene is transparent.
      vec4 sceneColor = texture2D(u_texture, mappedSceneUv);
      ballLayer = sceneColor + ballLayer * (1.0 - sceneColor.a);
    }

    result = ballLayer + result * (1.0 - ballLayer.a);
  }

  float edgeDistance = distanceFromCenter - radius;
  float angle = atan(ballPosition.y, ballPosition.x) - u_time * glowRevolveSpeed;
  vec2 ringPosition = vec2(cos(angle), sin(angle)) * 4.0;
  vec2 noiseMovement = vec2(u_time, -u_time * 0.73) * glowSpeed;
  float coarseNoise = ballNoise(ringPosition + noiseMovement);
  float fineNoise = ballNoise(ringPosition * 2.3 - noiseMovement * 1.7);
  float fogNoise = coarseNoise * 0.7 + fineNoise * 0.3;

  float pixelSize = 1.0 / shortSide;
  float opaqueRing =
      1.0
          - smoothstep(
              glowRingWidth,
              glowRingWidth + pixelSize * 2.0,
              abs(edgeDistance));

  float noisyExtend = glowExtend * mix(0.65, 1.25, fogNoise);
  float fogProgress =
      clamp((abs(edgeDistance) - glowRingWidth) / noisyExtend, 0.0, 1.0);
  float fog = pow(1.0 - fogProgress, glowDissipation);
  float noiseInfluence =
      mix(1.0, 0.55 + fogNoise * 0.45, smoothstep(0.0, 0.35, fogProgress));
  fog *= noiseInfluence * step(glowRingWidth, abs(edgeDistance));

  float glow = max(opaqueRing, fog) * u_glowStrength;
  if (u_curvedEdgeWidth > 0.0) {
    // Leave the curved scene visible inside the ball, retaining the bright outer halo.
    glow *= mix(0.2, 1.0, smoothstep(-glowRingWidth, 0.0, edgeDistance));
  }
  float glowAlpha = clamp(glow * u_glowColor.a, 0.0, 1.0);
  vec4 glowLayer = vec4(u_glowColor.rgb * glowAlpha, glowAlpha);
  result = glowLayer + result * (1.0 - glowLayer.a);

  // Motes reach no further than this above the rim.
  if (u_motes > 0.0 && edgeDistance > -0.01 && edgeDistance < 0.3) {
    // u_entityBounds is the world area of the scene buffer here, sixteen art pixels per tile.
    float pixelSize = MOTE_PIXEL / (16.0 * min(u_entityBounds.z, u_entityBounds.w));
    vec3 mote = motes(floor(ballPosition / pixelSize), radius, pixelSize);
    // A deeper shade of the ball colour, so the motes read as magic rather than stars.
    vec3 deep = u_ballColor.rgb * u_ballColor.rgb;
    vec3 halo = deep * min(mote.z, 0.8);
    vec3 core = mix(deep, u_glowColor.rgb, 0.5) * mote.x;
    result.rgb += halo + mix(core, vec3(mote.x), mote.y);
    result.a = max(result.a, min(max(mote.x, mote.z), 1.0));
  }

  gl_FragColor = result;
}
