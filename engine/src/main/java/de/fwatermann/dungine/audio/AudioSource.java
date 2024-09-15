package de.fwatermann.dungine.audio;

import de.fwatermann.dungine.utils.Disposable;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class AudioSource implements Disposable {

  private final int alSourceId;

  AudioSource(boolean loop, boolean relative) {
    this.alSourceId = AL10.alGenSources();
    AL10.alSourcei(this.alSourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    AL10.alSourcei(this.alSourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
  }

  public AudioSource setBuffer(AudioBuffer buffer) {
    this.stop();
    AL10.alSourcei(this.alSourceId, AL10.AL_BUFFER, buffer.alBufferId());
    return this;
  }

  public AudioSource stop() {
    AL10.alSourceStop(this.alSourceId);
    return this;
  }

  public AudioSource play() {
    AL10.alSourcePlay(this.alSourceId);
    return this;
  }

  public AudioSource pause() {
    AL10.alSourcePause(this.alSourceId);
    return this;
  }

  public AudioSource gain(float gain) {
    AL10.alSourcef(this.alSourceId, AL10.AL_GAIN, gain);
    return this;
  }

  public float gain() {
    return AL10.alGetSourcef(this.alSourceId, AL10.AL_GAIN);
  }

  public AudioSource pitch(float pitch) {
    AL10.alSourcef(this.alSourceId, AL10.AL_PITCH, pitch);
    return this;
  }

  public float pitch() {
    return AL10.alGetSourcef(this.alSourceId, AL10.AL_PITCH);
  }

  public AudioSource position(float x, float y, float z) {
    AL10.alSource3f(this.alSourceId, AL10.AL_POSITION, x, y, z);
    return this;
  }

  public AudioSource position(Vector3f position) {
    return this.position(position.x, position.y, position.z);
  }

  public AudioSource velocity(float x, float y, float z) {
    AL10.alSource3f(this.alSourceId, AL10.AL_VELOCITY, x, y, z);
    return this;
  }

  public AudioSource velocity(Vector3f velocity) {
    return this.velocity(velocity.x, velocity.y, velocity.z);
  }

  public AudioSource relative(boolean relative) {
    AL10.alSourcei(this.alSourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
    return this;
  }

  public boolean relative() {
    return AL10.alGetSourcei(this.alSourceId, AL10.AL_SOURCE_RELATIVE) == AL10.AL_TRUE;
  }

  public boolean playing() {
    return AL10.alGetSourcei(this.alSourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
  }

  @Override
  public void dispose() {
    this.stop();
    AL10.alDeleteSources(this.alSourceId);
  }
}
