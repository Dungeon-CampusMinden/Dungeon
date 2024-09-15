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
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

public class AudioContext implements Disposable {

  private long alDevice;
  private long alContext;

  private Vector3f lastListenerPosition = new Vector3f();

  private Map<String, AudioSource> sources = new HashMap<>();
  private Set<AudioBuffer> buffer = new HashSet<>();

  private AudioListener listener;
  private Camera<?> camera;

  public AudioContext() {
    this.init();
  }

  private void init() {
    this.alDevice = ALC10.alcOpenDevice((ByteBuffer) null);
    if(this.alDevice == 0) {
      throw new RuntimeException("Failed to open default OpenAL device.");
    }
    ALCCapabilities alcCaps = ALC.createCapabilities(this.alDevice);
    this.alContext = ALC10.alcCreateContext(this.alDevice, (IntBuffer) null);
    if(this.alContext == 0) {
      throw new RuntimeException("Failed to create OpenAL context.");
    }
    ALC10.alcMakeContextCurrent(this.alContext);
    AL.createCapabilities(alcCaps);

    this.listener = new AudioListener();
  }

  public AudioSource createSource(String name, boolean loop, boolean relative) {
    AudioSource source = new AudioSource(this, loop, relative);
    AudioSource old = this.sources.put(name, source);
    if(old != null) old.dispose();
    return source;
  }

  public AudioSource source(String name) {
    return this.sources.get(name);
  }

  public AudioSource removeSource(AudioSource source) {
    Map.Entry<String, AudioSource> entry = this.sources.entrySet().stream().filter(e -> e.getValue() == source).findFirst().orElse(null);
    if(entry != null) {
      return this.sources.remove(entry.getKey());
    } else {
      return null;
    }
  }

  public AudioSource removeSource(String name) {
    return this.sources.remove(name);
  }

  public AudioBuffer createBuffer(Resource resource, AudioBuffer.AudioFileType fileType) {
    AudioBuffer buffer = new AudioBuffer(resource, fileType);
    this.buffer.add(buffer);
    return buffer;
  }

  public AudioListener listener() {
    return this.listener;
  }

  public void update(float deltaTime) {
    ALC10.alcProcessContext(this.alContext);

    if(this.camera != null) {
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

  public Camera<?> camera() {
    return this.camera;
  }

  public AudioContext camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }

  @Override
  public void dispose() {
    for(AudioSource source : this.sources.values()) {
      source.dispose();
    }
    for(AudioBuffer buffer : this.buffer) {
      buffer.dispose();
    }
    this.sources.clear();
    this.buffer.clear();

    boolean current = ALC10.alcGetCurrentContext() == this.alContext;
    if(current){
      ALC10.alcMakeContextCurrent(0);
    }
    ALC10.alcDestroyContext(this.alContext);
    if(current) {
      ALC10.alcCloseDevice(this.alDevice);
    }
  }
}
