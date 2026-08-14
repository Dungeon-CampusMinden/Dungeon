package engine.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.DebugTelemetryRequest;

/** Converter for client-to-server debug telemetry requests. */
public final class DebugTelemetryRequestConverter
    implements MessageConverter<
        DebugTelemetryRequest, engine.network.proto.c2s.DebugTelemetryRequest> {
  private static final byte WIRE_TYPE_ID = 23;

  @Override
  public engine.network.proto.c2s.DebugTelemetryRequest toProto(DebugTelemetryRequest message) {
    return engine.network.proto.c2s.DebugTelemetryRequest.newBuilder()
        .setRequestId(message.requestId())
        .setMode(toProto(message.mode()))
        .setIntervalMs(message.intervalMs())
        .build();
  }

  @Override
  public DebugTelemetryRequest fromProto(engine.network.proto.c2s.DebugTelemetryRequest proto) {
    return new DebugTelemetryRequest(
        proto.getRequestId(), fromProto(proto.getMode()), proto.getIntervalMs());
  }

  @Override
  public Class<DebugTelemetryRequest> domainType() {
    return DebugTelemetryRequest.class;
  }

  @Override
  public Class<engine.network.proto.c2s.DebugTelemetryRequest> protoType() {
    return engine.network.proto.c2s.DebugTelemetryRequest.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.DebugTelemetryRequest> parser() {
    return engine.network.proto.c2s.DebugTelemetryRequest.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static engine.network.proto.c2s.DebugTelemetryRequestMode toProto(
      DebugTelemetryRequest.Mode mode) {
    return switch (mode) {
      case ONCE ->
          engine.network.proto.c2s.DebugTelemetryRequestMode.DEBUG_TELEMETRY_REQUEST_MODE_ONCE;
      case START_STREAM ->
          engine.network.proto.c2s.DebugTelemetryRequestMode
              .DEBUG_TELEMETRY_REQUEST_MODE_START_STREAM;
      case STOP_STREAM ->
          engine.network.proto.c2s.DebugTelemetryRequestMode
              .DEBUG_TELEMETRY_REQUEST_MODE_STOP_STREAM;
    };
  }

  private static DebugTelemetryRequest.Mode fromProto(
      engine.network.proto.c2s.DebugTelemetryRequestMode mode) {
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
