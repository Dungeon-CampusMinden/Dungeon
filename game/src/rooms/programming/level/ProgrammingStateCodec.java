package rooms.programming.level;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import rooms.programming.modules.methods.MethodsRoute;
import rooms.programming.modules.methods.MethodsWorkshop;
import tools.jackson.databind.json.JsonMapper;

/** Lossless room-state encoding for the string values in dialogs and snapshot metadata. */
final class ProgrammingStateCodec {
  private static final JsonMapper JSON =
      JsonMapper.builder()
          .addMixIn(MethodsWorkshop.State.class, ArrayRecord.class)
          .addMixIn(MethodsWorkshop.Block.class, ArrayRecord.class)
          .addMixIn(MethodsWorkshop.Definition.class, ArrayRecord.class)
          .addMixIn(MethodsRoute.Step.class, ArrayRecord.class)
          .build();
  private static final String COMPRESSED = "z:";
  private static final int MAX_JSON_BYTES = 1 << 20;

  private ProgrammingStateCodec() {}

  // The client and server share these record layouts; field names need not travel per block.
  @JsonFormat(shape = JsonFormat.Shape.ARRAY)
  private abstract static class ArrayRecord {}

  static String encode(Object state) {
    byte[] json = JSON.writeValueAsBytes(state);
    if (json.length > MAX_JSON_BYTES) throw new IllegalArgumentException("Room state is too large");
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DeflaterOutputStream compressed = new DeflaterOutputStream(bytes)) {
        compressed.write(json);
      }
      String encoded = COMPRESSED + Base64.getEncoder().encodeToString(bytes.toByteArray());
      return encoded.length() < json.length ? encoded : new String(json, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot encode room state", e);
    }
  }

  static <T> T decode(String value, Class<T> type) {
    if (!value.startsWith(COMPRESSED)) return JSON.readValue(value, type);
    byte[] bytes = Base64.getDecoder().decode(value.substring(COMPRESSED.length()));
    try (InflaterInputStream compressed =
        new InflaterInputStream(new ByteArrayInputStream(bytes))) {
      byte[] json = compressed.readNBytes(MAX_JSON_BYTES + 1);
      if (json.length > MAX_JSON_BYTES)
        throw new IllegalArgumentException("Room state is too large");
      return JSON.readValue(json, type);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot decode room state", e);
    }
  }
}
