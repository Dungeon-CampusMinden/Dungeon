package de.fwatermann.dungine.audio;

import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.Disposable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * The `AudioBuffer` class represents an audio buffer that loads audio data from a resource. It
 * supports loading OGG Vorbis audio files and provides methods to access the OpenAL buffer ID and
 * the resource.
 */
public class AudioBuffer implements Disposable {

  private int alBufferId = 0;
  private final int currentBuffer = 0;

  private final Resource resource;
  private final AudioFileType fileType;

  /**
   * Constructs a new `AudioBuffer` with the specified resource and audio file type.
   *
   * @param resource the resource to load the audio data from
   * @param fileType the type of the audio file
   */
  AudioBuffer(Resource resource, AudioFileType fileType) {
    this.resource = resource;
    this.fileType = fileType;
    this.load();
  }

  /** Loads the audio data from the resource based on the file type. */
  private void load() {
    try {
      ByteBuffer fileData = this.resource.readBytes();
      switch (this.fileType) {
        case OGGVorbis -> this.loadOGGVorbis(fileData);
        default -> throw new RuntimeException("Unsupported audio file type: " + this.fileType);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load audio file: " + this.resource, e);
    }
  }

  /**
   * Loads OGG Vorbis audio data from the specified byte buffer.
   *
   * @param data the byte buffer containing the OGG Vorbis audio data
   */
  private void loadOGGVorbis(ByteBuffer data) {
    try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        IntBuffer error = stack.mallocInt(1);
        long decoder = STBVorbis.stb_vorbis_open_memory(data, error, null);
        if (decoder == 0) {
          throw new RuntimeException(
              "Failed to open OGG Vorbis file: " + STBVorbis.stb_vorbis_get_error(decoder));
        }
        STBVorbis.stb_vorbis_get_info(decoder, info);
        int channels = info.channels();
        int sampleRate = info.sample_rate();
        int length = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
        ShortBuffer pcm = MemoryUtil.memAllocShort(length);
        pcm.limit(
            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
        STBVorbis.stb_vorbis_close(decoder);

        this.alBufferId = AL10.alGenBuffers();
        AL10.alBufferData(
            this.alBufferId,
            channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16,
            pcm,
            sampleRate);
      }
    }
  }

  /**
   * Gets the OpenAL buffer ID.
   *
   * @return the OpenAL buffer ID
   */
  int alBufferId() {
    return this.alBufferId;
  }

  /**
   * Gets the resource associated with this audio buffer.
   *
   * @return the resource associated with this audio buffer
   */
  public Resource resource() {
    return this.resource;
  }

  /** Disposes of the audio buffer, releasing any resources it holds. */
  @Override
  public void dispose() {
    AL10.alDeleteBuffers(this.alBufferId);
  }

  /** Enum representing the supported audio file types. */
  public enum AudioFileType {
    /** OGG Vorbis audio file type. */
    OGGVorbis,
  }
}
