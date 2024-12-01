package de.fwatermann.dungine.audio;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.Disposable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

/**
 * The `AudioContext` class manages the audio environment, including sources, buffers, and the
 * listener. It initializes the OpenAL context and provides methods to create and manage audio
 * sources and buffers.
 */
public class AudioContext implements Disposable {

  private long alDevice;
  private long alContext;

  private final Vector3f lastListenerPosition = new Vector3f();

  private final ReentrantReadWriteLock sourcesLock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock buffersLock = new ReentrantReadWriteLock();

  private final Map<String, AudioSource> sources = new HashMap<>();
  private final Set<AudioBuffer> buffer = new HashSet<>();

  private AudioListener listener;
  private Camera<?> camera;

  /** Constructs a new `AudioContext` and initializes the OpenAL context. */
  public AudioContext() {
    this.init();
  }

  private void init() {
    this.alDevice = ALC10.alcOpenDevice((ByteBuffer) null);
    if (this.alDevice == 0) {
      throw new RuntimeException("Failed to open default OpenAL device.");
    }
    ALCCapabilities alcCaps = ALC.createCapabilities(this.alDevice);
    this.alContext = ALC10.alcCreateContext(this.alDevice, (IntBuffer) null);
    if (this.alContext == 0) {
      throw new RuntimeException("Failed to create OpenAL context.");
    }
    ALC10.alcMakeContextCurrent(this.alContext);
    AL.createCapabilities(alcCaps);

    this.listener = new AudioListener();
  }

  /**
   * Creates a new audio source with the specified name, loop, and relative settings.
   *
   * @param name the name of the audio source
   * @param loop whether the audio source should loop
   * @param relative whether the audio source is relative to the listener
   * @return the created `AudioSource` instance
   */
  public AudioSource createSource(String name, boolean loop, boolean relative) {
    try {
      this.sourcesLock.writeLock().lock();
      AudioSource source = new AudioSource(this, loop, relative);
      AudioSource old = this.sources.put(name, source);
      if (old != null) old.dispose();
      return source;
    } finally {
      this.sourcesLock.writeLock().unlock();
    }
  }

  /**
   * Retrieves the audio source with the specified name.
   *
   * @param name the name of the audio source
   * @return the `AudioSource` instance, or null if not found
   */
  public AudioSource source(String name) {
    try {
      this.sourcesLock.readLock().lock();
      return this.sources.get(name);
    } finally {
      this.sourcesLock.readLock().unlock();
    }
  }

  /**
   * Removes the specified audio source.
   *
   * @param source the `AudioSource` instance to remove
   * @return the removed `AudioSource` instance, or null if not found
   */
  public AudioSource removeSource(AudioSource source) {
    try {
      this.sourcesLock.writeLock().lock();
      Map.Entry<String, AudioSource> entry =
          this.sources.entrySet().stream()
              .filter(e -> e.getValue() == source)
              .findFirst()
              .orElse(null);
      if (entry != null) {
        return this.sources.remove(entry.getKey());
      } else {
        return null;
      }
    } finally {
      this.sourcesLock.writeLock().unlock();
    }
  }

  /**
   * Removes the audio source with the specified name.
   *
   * @param name the name of the audio source to remove
   * @return the removed `AudioSource` instance, or null if not found
   */
  public AudioSource removeSource(String name) {
    try {
      this.sourcesLock.writeLock().lock();
      return this.sources.remove(name);
    } finally {
      this.sourcesLock.writeLock().unlock();
    }
  }

  /**
   * Creates a new audio buffer with the specified resource and file type.
   *
   * @param resource the resource to load the audio data from
   * @param fileType the type of the audio file
   * @return the created `AudioBuffer` instance
   */
  public AudioBuffer createBuffer(Resource resource, AudioBuffer.AudioFileType fileType) {
    try {
      this.buffersLock.writeLock().lock();
      AudioBuffer buffer = new AudioBuffer(resource, fileType);
      this.buffer.add(buffer);
      return buffer;
    } finally {
      this.buffersLock.writeLock().unlock();
    }
  }

  /**
   * Retrieves the audio listener.
   *
   * @return the `AudioListener` instance
   */
  public AudioListener listener() {
    return this.listener;
  }

  /**
   * Updates the audio context, processing the OpenAL context and updating the listener's position
   * and orientation.
   *
   * <p>This method is called once per update tick by the `GameState` instance. It should not be
   * called manually by the user except this object is created manually and is not managed by the
   * `GameState` object.
   *
   * @param deltaTime the time elapsed since the last update
   */
  public void update(float deltaTime) {
    ALC10.alcProcessContext(this.alContext);

    if (this.camera != null) {
      Vector3f newPos = this.camera.position();
      this.listener.position(newPos);
      this.listener.orientation(this.camera.front(), this.camera.up());
      this.listener.velocity(newPos.sub(this.lastListenerPosition, new Vector3f()).div(deltaTime));
      this.lastListenerPosition.set(newPos);
    } else {
      this.listener.position(0, 0, 0);
      this.listener.orientation(0, 0, -1, 0, 1, 0);
    }
  }

  /**
   * Retrieves the camera associated with the audio context.
   *
   * @return the `Camera` instance
   */
  public Camera<?> camera() {
    return this.camera;
  }

  /**
   * Sets the camera for the audio context.
   *
   * @param camera the `Camera` instance to set
   * @return the updated `AudioContext` instance
   */
  public AudioContext camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }

  /** Disposes of the audio context, releasing any resources it holds. */
  @Override
  public void dispose() {
    try {
      this.sourcesLock.writeLock().lock();
      for (AudioSource source : this.sources.values()) {
        source.dispose(false);
      }
      this.sources.clear();
    } finally {
      this.sourcesLock.writeLock().unlock();
    }

    try {
      this.buffersLock.writeLock().lock();
      for (AudioBuffer buffer : this.buffer) {
        buffer.dispose();
      }
      this.buffer.clear();
    } finally {
      this.buffersLock.writeLock().unlock();
    }

    boolean current = ALC10.alcGetCurrentContext() == this.alContext;
    if (current) {
      ALC10.alcMakeContextCurrent(0);
    }
    ALC10.alcDestroyContext(this.alContext);
    if (current) {
      ALC10.alcCloseDevice(this.alDevice);
    }
  }
}
