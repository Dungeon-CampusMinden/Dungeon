struct Frame {
  sampler2D frameTexture;
  ivec2 position; // in pixels
  ivec2 size; // in pixels
};

struct Animation
{
  Frame currentFrame;
  Frame nextFrame;
  float blendFactor;
};

vec4 colorOfFrameAt(vec2 texCoords, Frame frame) {
  ivec2 frameTextureSize = textureSize(frame.frameTexture, 0);
  vec2 frameSize = vec2(frame.size) / vec2(frameTextureSize);
  vec2 framePosition = vec2(frame.position) / vec2(frameTextureSize);
  vec2 frameTexCoords = texCoords * frameSize + framePosition;
  return texture(frame.frameTexture, frameTexCoords);
}

vec4 animationColor(Animation animation, vec2 texCoords) {
  vec4 currentFrameColor = colorOfFrameAt(texCoords, animation.currentFrame);
  if(animation.blendFactor == 0.0f) {
    return currentFrameColor;
  }
  vec4 nextFrameColor = colorOfFrameAt(texCoords, animation.nextFrame);
  return mix(currentFrameColor, nextFrameColor, animation.blendFactor);
}
