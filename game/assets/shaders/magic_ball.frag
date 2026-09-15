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
uniform vec2 u_ballOffset;
uniform vec4 u_textureRegion;
uniform vec4 u_ballColor;
uniform float u_glowStrength;
uniform vec4 u_glowColor;

const float BALL_TEXTURE_VERTICAL_GAP = 0.08;
const float glowRingWidth = 0.012;
const float glowExtend = 0.1;
const float glowDissipation = 1.6;
const float glowRevolveSpeed = 0.25;
const float glowSpeed = 0.12;

float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
  vec2 cell = floor(p);
  vec2 local = fract(p);
  local = local * local * (3.0 - 2.0 * local);

  return mix(
      mix(hash(cell), hash(cell + vec2(1.0, 0.0)), local.x),
      mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), local.x),
      local.y);
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
    vec4 ballColor = u_ballColor;

    if (abs(spherePosition.y) <= textureLimit) {
      float maxLatitude = asin(textureLimit);
      vec2 sceneUv =
          vec2(
              atan(spherePosition.x, sphereZ) / PI + 0.5,
              asin(clamp(spherePosition.y, -textureLimit, textureLimit))
                      / (2.0 * maxLatitude)
                  + 0.5);
      vec2 mappedSceneUv = u_textureRegion.xy + sceneUv * u_textureRegion.zw;
      vec4 sceneColor = unPma(texture2D(u_texture, mappedSceneUv));
      if (sceneColor.a > 0.0) {
        ballColor = sceneColor;
      }
    }

    vec4 ballLayer = pma(ballColor);
    result = ballLayer + result * (1.0 - ballLayer.a);
  }

  float edgeDistance = distanceFromCenter - radius;
  float angle = atan(ballPosition.y, ballPosition.x) - u_time * glowRevolveSpeed;
  vec2 ringPosition = vec2(cos(angle), sin(angle)) * 4.0;
  vec2 noiseMovement = vec2(u_time, -u_time * 0.73) * glowSpeed;
  float coarseNoise = noise(ringPosition + noiseMovement);
  float fineNoise = noise(ringPosition * 2.3 - noiseMovement * 1.7);
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
  float glowAlpha = clamp(glow * u_glowColor.a, 0.0, 1.0);
  vec4 glowLayer = vec4(u_glowColor.rgb * glowAlpha, glowAlpha);
  result = glowLayer + result * (1.0 - glowLayer.a);

  gl_FragColor = result;
}
