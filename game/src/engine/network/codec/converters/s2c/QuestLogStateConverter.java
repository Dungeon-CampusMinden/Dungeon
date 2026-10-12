package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.QuestLogStateMessage;
import engine.network.proto.s2c.QuestLogEntryState;
import java.util.Optional;

/** Converter for server-to-client quest log state messages. */
public final class QuestLogStateConverter
    implements MessageConverter<
        QuestLogStateMessage, engine.network.proto.s2c.QuestLogStateMessage> {
  private static final byte WIRE_TYPE_ID = 31;

  @Override
  public engine.network.proto.s2c.QuestLogStateMessage toProto(QuestLogStateMessage message) {
    engine.network.proto.s2c.QuestLogStateMessage.Builder builder =
        engine.network.proto.s2c.QuestLogStateMessage.newBuilder()
            .setAvailable(message.available());
    for (QuestLogStateMessage.Entry entry : message.entries()) {
      QuestLogEntryState.Builder entryBuilder =
          QuestLogEntryState.newBuilder()
              .setTab(entry.tab())
              .setText(entry.text())
              .setTimestamp(entry.timestamp())
              .setUserCreated(entry.userCreated())
              .setOwner(entry.owner())
              .setOnlyForCreator(entry.onlyForCreator());
      entry.title().ifPresent(entryBuilder::setTitle);
      builder.addEntries(entryBuilder);
    }
    return builder.build();
  }

  @Override
  public QuestLogStateMessage fromProto(engine.network.proto.s2c.QuestLogStateMessage proto) {
    return new QuestLogStateMessage(
        proto.getAvailable(),
        proto.getEntriesList().stream()
            .map(
                entry ->
                    new QuestLogStateMessage.Entry(
                        entry.getTab(),
                        entry.getText(),
                        entry.getTimestamp(),
                        entry.getUserCreated(),
                        entry.getOwner(),
                        entry.getOnlyForCreator(),
                        entry.hasTitle() ? Optional.of(entry.getTitle()) : Optional.empty()))
            .toList());
  }

  @Override
  public Class<QuestLogStateMessage> domainType() {
    return QuestLogStateMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.QuestLogStateMessage> protoType() {
    return engine.network.proto.s2c.QuestLogStateMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.QuestLogStateMessage> parser() {
    return engine.network.proto.s2c.QuestLogStateMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
