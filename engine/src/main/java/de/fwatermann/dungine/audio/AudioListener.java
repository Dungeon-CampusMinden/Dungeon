package de.fwatermann.dungine.audio;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

/**
 * The `AudioListener` class represents the listener in the audio environment.
 * It provides methods to set the position, velocity, and orientation of the listener.
 */
public class AudioListener {

  /**
   * Constructs a new `AudioListener` and initializes the listener's position and velocity to zero.
   */
  AudioListener() {
    AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
    AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
  }

  /**
   * Sets the position of the listener.
   *
   * @param x the x-coordinate of the listener's position
   * @param y the y-coordinate of the listener's position
   * @param z the z-coordinate of the listener's position
   * @return the updated `AudioListener` instance
   */
  public AudioListener position(float x, float y, float z) {
    AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    return this;
  }

  /**
   * Sets the position of the listener.
   *
   * @param position the `Vector3f` representing the listener's position
   * @return the updated `AudioListener` instance
   */
  public AudioListener position(Vector3f position) {
    return this.position(position.x, position.y, position.z);
  }

  /**
   * Sets the velocity of the listener.
   *
   * @param x the x-coordinate of the listener's velocity
   * @param y the y-coordinate of the listener's velocity
   * @param z the z-coordinate of the listener's velocity
   * @return the updated `AudioListener` instance
   */
  public AudioListener velocity(float x, float y, float z) {
    AL10.alListener3f(AL10.AL_VELOCITY, x, y, z);
    return this;
  }

  /**
   * Sets the velocity of the listener.
   *
   * @param velocity the `Vector3f` representing the listener's velocity
   * @return the updated `AudioListener` instance
   */
  public AudioListener velocity(Vector3f velocity) {
    return this.velocity(velocity.x, velocity.y, velocity.z);
  }

  /**
   * Sets the orientation of the listener.
   *
   * @param atX the x-coordinate of the "at" vector
   * @param atY the y-coordinate of the "at" vector
   * @param atZ the z-coordinate of the "at" vector
   * @param upX the x-coordinate of the "up" vector
   * @param upY the y-coordinate of the "up" vector
   * @param upZ the z-coordinate of the "up" vector
   * @return the updated `AudioListener` instance
   */
  public AudioListener orientation(float atX, float atY, float atZ, float upX, float upY, float upZ) {
    float[] data = new float[] {atX, atY, atZ, upX, upY, upZ};
    AL10.alListenerfv(AL10.AL_ORIENTATION, data);
    return this;
  }

  /**
   * Sets the orientation of the listener.
   *
   * @param at the `Vector3f` representing the "at" vector
   * @param up the `Vector3f` representing the "up" vector
   * @return the updated `AudioListener` instance
   */
  public AudioListener orientation(Vector3f at, Vector3f up) {
    return this.orientation(at.x, at.y, at.z, up.x, up.y, up.z);
  }
}
