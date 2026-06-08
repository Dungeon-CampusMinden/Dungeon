package core.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.DebugTelemetryRequest;

/** Converter for client-to-server debug telemetry requests. */
public final class DebugTelemetryRequestConverter
    implements MessageConverter<
        DebugTelemetryRequest, core.network.proto.c2s.DebugTelemetryRequest> {
  private static final byte WIRE_TYPE_ID = 23;

  @Override
  public core.network.proto.c2s.DebugTelemetryRequest toProto(DebugTelemetryRequest message) {
    return core.network.proto.c2s.DebugTelemetryRequest.newBuilder()
        .setRequestId(message.requestId())
        .setMode(toProto(message.mode()))
        .setIntervalMs(message.intervalMs())
        .build();
  }

  @Override
  public DebugTelemetryRequest fromProto(core.network.proto.c2s.DebugTelemetryRequest proto) {
    return new DebugTelemetryRequest(
        proto.getRequestId(), fromProto(proto.getMode()), proto.getIntervalMs());
  }

  @Override
  public Class<DebugTelemetryRequest> domainType() {
    return DebugTelemetryRequest.class;
  }

  @Override
  public Class<core.network.proto.c2s.DebugTelemetryRequest> protoType() {
    return core.network.proto.c2s.DebugTelemetryRequest.class;
  }

  @Override
  public Parser<core.network.proto.c2s.DebugTelemetryRequest> parser() {
    return core.network.proto.c2s.DebugTelemetryRequest.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static core.network.proto.c2s.DebugTelemetryRequestMode toProto(
      DebugTelemetryRequest.Mode mode) {
    return switch (mode) {
      case ONCE ->
          core.network.proto.c2s.DebugTelemetryRequestMode.DEBUG_TELEMETRY_REQUEST_MODE_ONCE;
      case START_STREAM ->
          core.network.proto.c2s.DebugTelemetryRequestMode
              .DEBUG_TELEMETRY_REQUEST_MODE_START_STREAM;
      case STOP_STREAM ->
          core.network.proto.c2s.DebugTelemetryRequestMode.DEBUG_TELEMETRY_REQUEST_MODE_STOP_STREAM;
    };
  }

  private static DebugTelemetryRequest.Mode fromProto(
      core.network.proto.c2s.DebugTelemetryRequestMode mode) {
    return switch (mode) {
      case DEBUG_TELEMETRY_REQUEST_MODE_START_STREAM -> DebugTelemetryRequest.Mode.START_STREAM;
      case DEBUG_TELEMETRY_REQUEST_MODE_STOP_STREAM -> DebugTelemetryRequest.Mode.STOP_STREAM;
      case DEBUG_TELEMETRY_REQUEST_MODE_ONCE,
          DEBUG_TELEMETRY_REQUEST_MODE_UNSPECIFIED,
          UNRECOGNIZED ->
          DebugTelemetryRequest.Mode.ONCE;
    };
  }
}
