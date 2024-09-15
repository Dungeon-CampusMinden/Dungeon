package de.fwatermann.dungine.audio;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class AudioListener {

  AudioListener() {
    AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
    AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
  }

  public AudioListener position(float x, float y, float z) {
    AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    return this;
  }

  public AudioListener position(Vector3f position) {
    return this.position(position.x, position.y, position.z);
  }

  public AudioListener velocity(float x, float y, float z) {
    AL10.alListener3f(AL10.AL_VELOCITY, x, y, z);
    return this;
  }

  public AudioListener velocity(Vector3f velocity) {
    return this.velocity(velocity.x, velocity.y, velocity.z);
  }

  public AudioListener orientation(
      float atX, float atY, float atZ, float upX, float upY, float upZ) {
    float[] data = new float[] {atX, atY, atZ, upX, upY, upZ};
    AL10.alListenerfv(AL10.AL_ORIENTATION, data);
    return this;
  }

  public AudioListener orientation(Vector3f at, Vector3f up) {
    return this.orientation(at.x, at.y, at.z, up.x, up.y, up.z);
  }
}
