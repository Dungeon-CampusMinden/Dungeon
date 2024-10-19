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

public class AudioBuffer implements Disposable {

  private int alBufferId = 0;
  private int currentBuffer = 0;

  private Resource resource;
  private AudioFileType fileType;

  /**
   * Constructs a new AudioBuffer with the specified resource.
   *
   * @param resource the resource to load the audio data from
   */
  AudioBuffer(Resource resource, AudioFileType fileType) {
    this.resource = resource;
    this.fileType = fileType;
    this.load();
  }

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

  int alBufferId() {
    return this.alBufferId;
  }

  public Resource resource() {
    return this.resource;
  }

  @Override
  public void dispose() {
    AL10.alDeleteBuffers(this.alBufferId);
  }

  public enum AudioFileType {
    OGGVorbis,
  }
}
