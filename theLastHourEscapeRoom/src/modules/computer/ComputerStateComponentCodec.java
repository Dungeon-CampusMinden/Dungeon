package modules.computer;

import core.network.codec.DialogValueCodec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Codec for encoding and decoding {@link ComputerStateComponent} values for network transport.
 *
 * <p>Binary layout:
 *
 * <ul>
 *   <li>ComputerProgress enum name (UTF)
 *   <li>infected flag (boolean)
 *   <li>hasVirusType flag (boolean)
 *   <li>virus type (UTF, only when hasVirusType is true)
 * </ul>
 */
public final class ComputerStateComponentCodec implements DialogValueCodec<ComputerStateComponent> {

  @Override
  public String typeId() {
    return "ComputerStateComponent";
  }

  @Override
  public Class<ComputerStateComponent> type() {
    return ComputerStateComponent.class;
  }

  @Override
  public byte[] encode(ComputerStateComponent value) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream)) {
      dataOutput.writeUTF(value.state().name());
      dataOutput.writeBoolean(value.isInfected());
      boolean hasVirusType = value.virusType() != null;
      dataOutput.writeBoolean(hasVirusType);
      if (hasVirusType) {
        dataOutput.writeUTF(value.virusType());
      }
      dataOutput.flush();
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to encode ComputerStateComponent", e);
    }
  }

  @Override
  public ComputerStateComponent decode(byte[] data) {
    try (DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(data))) {
      ComputerProgress state = ComputerProgress.valueOf(dataInput.readUTF());
      boolean infected = dataInput.readBoolean();
      String virusType = dataInput.readBoolean() ? dataInput.readUTF() : null;
      return new ComputerStateComponent(state, infected, virusType);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to decode ComputerStateComponent", e);
    }
  }
}
