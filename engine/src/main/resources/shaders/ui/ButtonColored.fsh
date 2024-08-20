#version 330 core

in vec3 vs_FragCoord;

uniform int uFillColor;
uniform int uBorderColor;
uniform float uBorderWidth;
uniform float uBorderRadius;

uniform mat4 uModel;
uniform vec2 uSize;

out vec4 fragColor;

float calcProjCircleDistance(vec2 point, vec2 center, mat4 transformMatrix, float radius) {
  vec4 projPoint = transformMatrix * vec4(point, 0.0f, 1.0f);
  vec4 projCenter = transformMatrix * vec4(center, 0.0f, 1.0f);
  return distance(projPoint.xyz, projCenter.xyz);
}

int calcState(vec2 point, vec2 size, mat4 transformMatrix, float pRadius, float borderWidth) {
  if(uSize.x == 0 || uSize.y == 0) {
    return 0;
  }

  vec2 ratio = vec2(1.0f, 1.0f) / size;
  vec2 radius = vec2(pRadius, pRadius) * ratio;
  vec2 projPoint = (transformMatrix * vec4(point, 0.0f, 1.0f)).xy;

  float dist = -1;
  if(point.x <= radius.x && point.y <= radius.y) {
    dist = distance(projPoint, (transformMatrix * vec4(radius, 0.0f, 1.0f)).xy);
  } else if(point.x >= (1.0f - radius.x) && point.y <= radius.y) {
    dist = distance(projPoint, (transformMatrix * vec4(1.0f - radius.x, radius.y, 0.0f, 1.0f)).xy);
  } else if(point.x <= radius.x && point.y >= (1.0f - radius.y)) {
    dist = distance(projPoint, (transformMatrix * vec4(radius.x, 1.0f - radius.y, 0.0f, 1.0f)).xy);
  } else if(point.x >= (1.0f - radius.x) && point.y >= (1.0f - radius.y)) {
    dist = distance(projPoint, (transformMatrix * vec4(1.0f - radius.x, 1.0f - radius.y, 0.0f, 1.0f)).xy);
  }

  if(dist == -1) {
    float bX = borderWidth * ratio.x;
    if(point.x <= bX || point.x >= (1.0f - bX)) {
      return 2;
    }
    float bY = borderWidth * ratio.y;
    if(point.y <= bY || point.y >= 1.0f - bY) {
      return 2;
    }
    return 1;
  } else if(dist <= pRadius) {
    if(dist >= pRadius - borderWidth) {
      return 2;
    } else {
      return 1;
    }
  } else {
    return 0;
  }
}

void main() {
  int fragState = calcState(vs_FragCoord.xy, uSize, uModel, uBorderRadius, uBorderWidth);

  if (fragState == 1) {
    fragColor = vec4(
      ((uFillColor >> 24) & 0xFF) / 255.0f,
      ((uFillColor >> 16) & 0xFF) / 255.0f,
      ((uFillColor >> 8) & 0xFF) / 255.0f,
      ((uFillColor >> 0) & 0xFF) / 255.0f
    );
  } else if (fragState == 2) {
    fragColor = vec4(
      ((uBorderColor >> 24) & 0xFF) / 255.0f,
      ((uBorderColor >> 16) & 0xFF) / 255.0f,
      ((uBorderColor >> 8) & 0xFF) / 255.0f,
      ((uBorderColor >> 0) & 0xFF) / 255.0f
    );
  } else {
    discard;
    return;
  }
}
