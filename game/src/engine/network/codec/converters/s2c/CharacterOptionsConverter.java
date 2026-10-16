package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.CharacterOptions;
import feature.entities.CharacterClass;
import java.util.ArrayList;
import java.util.List;

/** Converter for pre-authentication character-options responses. */
public final class CharacterOptionsConverter
    implements MessageConverter<CharacterOptions, engine.network.proto.s2c.CharacterOptions> {
  private static final byte WIRE_TYPE_ID = 36;

  @Override
  public engine.network.proto.s2c.CharacterOptions toProto(CharacterOptions message) {
    engine.network.proto.s2c.CharacterOptions.Builder builder =
        engine.network.proto.s2c.CharacterOptions.newBuilder();
    for (CharacterOptions.Option option : message.characters()) {
      builder.addCharacters(
          engine.network.proto.s2c.CharacterOptions.Option.newBuilder()
              .setCharacterClassId(option.characterClass().ordinal())
              .setMaxCount(option.maxCount())
              .setPlayerCount(option.playerCount())
              .setSelectable(option.selectable())
              .setAssigned(option.assigned()));
    }
    return builder.build();
  }

  @Override
  public CharacterOptions fromProto(engine.network.proto.s2c.CharacterOptions proto) {
    List<CharacterOptions.Option> options = new ArrayList<>(proto.getCharactersCount());
    for (engine.network.proto.s2c.CharacterOptions.Option option : proto.getCharactersList()) {
      CharacterClass characterClass =
          CharacterClass.fromByteId(
              CommonProtoConverters.toByteExact(
                  option.getCharacterClassId(), "character_class_id"));
      options.add(
          new CharacterOptions.Option(
              characterClass,
              option.getMaxCount(),
              option.getPlayerCount(),
              option.getSelectable(),
              option.getAssigned()));
    }
    return new CharacterOptions(options);
  }

  @Override
  public Class<CharacterOptions> domainType() {
    return CharacterOptions.class;
  }

  @Override
  public Class<engine.network.proto.s2c.CharacterOptions> protoType() {
    return engine.network.proto.s2c.CharacterOptions.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.CharacterOptions> parser() {
    return engine.network.proto.s2c.CharacterOptions.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
