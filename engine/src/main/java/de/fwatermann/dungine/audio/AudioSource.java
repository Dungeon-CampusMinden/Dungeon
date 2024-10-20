package de.fwatermann.dungine.audio;

import de.fwatermann.dungine.utils.Disposable;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

/**
 * The `AudioSource` class represents an audio source in the audio environment.
 * It provides methods to control playback, set properties like gain, pitch, position, and velocity, and manage the audio buffer.
 */
public class AudioSource implements Disposable {

  private final int alSourceId;
  private AudioContext context;

  /**
   * Constructs a new `AudioSource` with the specified context, loop, and relative settings.
   *
   * @param context the audio context
   * @param loop whether the audio source should loop
   * @param relative whether the audio source is relative to the listener
   */
  AudioSource(AudioContext context, boolean loop, boolean relative) {
    this.alSourceId = AL10.alGenSources();
    this.context = context;
    AL10.alSourcei(this.alSourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    AL10.alSourcei(
        this.alSourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
  }

  /**
   * Sets the audio buffer for this source.
   *
   * @param buffer the audio buffer
   * @return the updated `AudioSource` instance
   */
  public AudioSource setBuffer(AudioBuffer buffer) {
    this.stop();
    AL10.alSourcei(this.alSourceId, AL10.AL_BUFFER, buffer.alBufferId());
    return this;
  }

  /**
   * Stops playback of the audio source.
   *
   * @return the updated `AudioSource` instance
   */
  public AudioSource stop() {
    AL10.alSourceStop(this.alSourceId);
    return this;
  }

  /**
   * Starts playback of the audio source.
   *
   * @return the updated `AudioSource` instance
   */
  public AudioSource play() {
    AL10.alSourcePlay(this.alSourceId);
    return this;
  }

  /**
   * Pauses playback of the audio source.
   *
   * @return the updated `AudioSource` instance
   */
  public AudioSource pause() {
    AL10.alSourcePause(this.alSourceId);
    return this;
  }

  /**
   * Sets the gain (volume) of the audio source.
   *
   * @param gain the gain value
   * @return the updated `AudioSource` instance
   */
  public AudioSource gain(float gain) {
    AL10.alSourcef(this.alSourceId, AL10.AL_GAIN, gain);
    return this;
  }

  /**
   * Gets the gain (volume) of the audio source.
   *
   * @return the gain value
   */
  public float gain() {
    return AL10.alGetSourcef(this.alSourceId, AL10.AL_GAIN);
  }

  /**
   * Sets the pitch of the audio source.
   *
   * @param pitch the pitch value
   * @return the updated `AudioSource` instance
   */
  public AudioSource pitch(float pitch) {
    AL10.alSourcef(this.alSourceId, AL10.AL_PITCH, pitch);
    return this;
  }

  /**
   * Gets the pitch of the audio source.
   *
   * @return the pitch value
   */
  public float pitch() {
    return AL10.alGetSourcef(this.alSourceId, AL10.AL_PITCH);
  }

  /**
   * Sets the position of the audio source.
   *
   * @param x the x-coordinate of the position
   * @param y the y-coordinate of the position
   * @param z the z-coordinate of the position
   * @return the updated `AudioSource` instance
   */
  public AudioSource position(float x, float y, float z) {
    AL10.alSource3f(this.alSourceId, AL10.AL_POSITION, x, y, z);
    return this;
  }

  /**
   * Sets the position of the audio source.
   *
   * @param position the `Vector3f` representing the position
   * @return the updated `AudioSource` instance
   */
  public AudioSource position(Vector3f position) {
    return this.position(position.x, position.y, position.z);
  }

  /**
   * Sets the velocity of the audio source.
   *
   * @param x the x-coordinate of the velocity
   * @param y the y-coordinate of the velocity
   * @param z the z-coordinate of the velocity
   * @return the updated `AudioSource` instance
   */
  public AudioSource velocity(float x, float y, float z) {
    AL10.alSource3f(this.alSourceId, AL10.AL_VELOCITY, x, y, z);
    return this;
  }

  /**
   * Sets the velocity of the audio source.
   *
   * @param velocity the `Vector3f` representing the velocity
   * @return the updated `AudioSource` instance
   */
  public AudioSource velocity(Vector3f velocity) {
    return this.velocity(velocity.x, velocity.y, velocity.z);
  }

  /**
   * Sets whether the audio source is relative to the listener.
   *
   * @param relative whether the audio source is relative
   * @return the updated `AudioSource` instance
   */
  public AudioSource relative(boolean relative) {
    AL10.alSourcei(
        this.alSourceId, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
    return this;
  }

  /**
   * Checks if the audio source is relative to the listener.
   *
   * @return true if the audio source is relative, false otherwise
   */
  public boolean relative() {
    return AL10.alGetSourcei(this.alSourceId, AL10.AL_SOURCE_RELATIVE) == AL10.AL_TRUE;
  }

  /**
   * Checks if the audio source is currently playing.
   *
   * @return true if the audio source is playing, false otherwise
   */
  public boolean playing() {
    return AL10.alGetSourcei(this.alSourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
  }

  /**
   * Gets the audio context associated with this source.
   *
   * @return the audio context
   */
  public AudioContext context() {
    return this.context;
  }

  /**
   * Sets whether the audio source should loop.
   *
   * @param loop whether the audio source should loop
   * @return the updated `AudioSource` instance
   */
  public AudioSource loop(boolean loop) {
    AL10.alSourcei(this.alSourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    return this;
  }

  /**
   * Checks if the audio source is set to loop.
   *
   * @return true if the audio source is set to loop, false otherwise
   */
  public boolean loop() {
    return AL10.alGetSourcei(this.alSourceId, AL10.AL_LOOPING) == AL10.AL_TRUE;
  }

  /**
   * Disposes of the audio source, releasing any resources it holds.
   */
  @Override
  public void dispose() {
    this.dispose(true);
  }

  /**
   * Disposes of the audio source, optionally removing it from the context.
   *
   * @param rmFromContext whether to remove the source from the context
   */
  void dispose(boolean rmFromContext) {
    this.stop();
    AL10.alDeleteSources(this.alSourceId);
    if(rmFromContext)
      this.context.removeSource(this);
  }
}
