package core.network.codec;

import contrib.item.Item;
import contrib.item.ItemRegistry;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.sound.SoundSpec;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DrawInfoData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shared conversion helpers for common network protobuf sub-types. */
public final class CommonProtoConverters {

  private CommonProtoConverters() {}

  /**
   * Converts a {@link Point} into its protobuf representation.
   *
   * @param point the domain point
   * @return the protobuf point
   */
  public static core.network.proto.common.Point toProto(Point point) {
    return core.network.proto.common.Point.newBuilder().setX(point.x()).setY(point.y()).build();
  }

  /**
   * Converts a protobuf point into a {@link Point}.
   *
   * @param proto the protobuf point
   * @return the domain point
   */
  public static Point fromProto(core.network.proto.common.Point proto) {
    return new Point(proto.getX(), proto.getY());
  }

  /**
   * Converts a {@link Vector2} into its protobuf representation.
   *
   * @param vector the domain vector
   * @return the protobuf vector
   */
  public static core.network.proto.common.Vector2 toProto(Vector2 vector) {
    return core.network.proto.common.Vector2.newBuilder().setX(vector.x()).setY(vector.y()).build();
  }

  /**
   * Converts a protobuf vector into a {@link Vector2}.
   *
   * @param proto the protobuf vector
   * @return the domain vector
   */
  public static Vector2 fromProto(core.network.proto.common.Vector2 proto) {
    return Vector2.of(proto.getX(), proto.getY());
  }

  /**
   * Converts a {@link PositionComponent} into its protobuf representation.
   *
   * @param component the position component
   * @return the protobuf position info
   */
  public static core.network.proto.common.PositionInfo toProto(PositionComponent component) {
    return core.network.proto.common.PositionInfo.newBuilder()
        .setPosition(toProto(component.position()))
        .setViewDirection(toProto(component.viewDirection()))
        .setRotation(component.rotation())
        .setScale(toProto(component.scale()))
        .build();
  }

  /**
   * Converts a protobuf position info into a {@link PositionComponent}.
   *
   * @param proto the protobuf position info
   * @return the domain position component
   */
  public static PositionComponent fromProto(core.network.proto.common.PositionInfo proto) {
    if (!proto.hasPosition()) {
      throw new IllegalArgumentException("PositionInfo.position is required.");
    }

    PositionComponent component =
        new PositionComponent(fromProto(proto.getPosition()), fromProto(proto.getViewDirection()));
    component.rotation(proto.getRotation());
    if (proto.hasScale()) {
      component.scale(fromProto(proto.getScale()));
    }
    return component;
  }

  /**
   * Converts a {@link Direction} into its protobuf representation.
   *
   * @param direction the domain direction
   * @return the protobuf direction
   */
  public static core.network.proto.common.Direction toProto(Direction direction) {
    return switch (direction) {
      case UP -> core.network.proto.common.Direction.DIRECTION_UP;
      case DOWN -> core.network.proto.common.Direction.DIRECTION_DOWN;
      case LEFT -> core.network.proto.common.Direction.DIRECTION_LEFT;
      case RIGHT -> core.network.proto.common.Direction.DIRECTION_RIGHT;
      case NONE -> core.network.proto.common.Direction.DIRECTION_NONE;
    };
  }

  /**
   * Converts a protobuf direction into a {@link Direction}.
   *
   * @param proto the protobuf direction
   * @return the domain direction
   */
  public static Direction fromProto(core.network.proto.common.Direction proto) {
    return switch (proto) {
      case DIRECTION_UP -> Direction.UP;
      case DIRECTION_DOWN -> Direction.DOWN;
      case DIRECTION_LEFT -> Direction.LEFT;
      case DIRECTION_RIGHT -> Direction.RIGHT;
      case DIRECTION_NONE, DIRECTION_UNSPECIFIED, UNRECOGNIZED -> Direction.NONE;
    };
  }

  /**
   * Converts a {@link SoundSpec} into its protobuf representation.
   *
   * @param spec the domain sound spec
   * @return the protobuf sound spec
   */
  public static core.network.proto.common.SoundSpec toProto(SoundSpec spec) {
    return core.network.proto.common.SoundSpec.newBuilder()
        .setInstanceId(spec.instanceId())
        .setSoundName(spec.soundName())
        .setBaseVolume(spec.baseVolume())
        .setLooping(spec.looping())
        .setPitch(spec.pitch())
        .setPan(spec.pan())
        .setMaxDistance(spec.maxDistance())
        .setAttenuationFactor(spec.attenuationFactor())
        .build();
  }

  /**
   * Converts a protobuf sound specification into a {@link SoundSpec}.
   *
   * @param proto the protobuf sound specification
   * @return the domain sound specification
   */
  public static SoundSpec fromProto(core.network.proto.common.SoundSpec proto) {
    return SoundSpec.builder(proto.getSoundName())
        .instanceId(proto.getInstanceId())
        .volume(proto.getBaseVolume())
        .looping(proto.getLooping())
        .pitch(proto.getPitch())
        .pan(proto.getPan())
        .maxDistance(proto.getMaxDistance())
        .attenuation(proto.getAttenuationFactor())
        .build();
  }

  /**
   * Converts {@link DrawInfoData} into its protobuf representation.
   *
   * @param drawInfo the domain draw info
   * @return the protobuf draw info
   */
  public static core.network.proto.s2c.DrawInfo toProto(DrawInfoData drawInfo) {
    if (drawInfo == null) {
      throw new IllegalArgumentException("DrawInfoData is required.");
    }
    String texturePath = drawInfo.texturePath();
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException("DrawInfoData.texturePath is required.");
    }

    core.network.proto.s2c.DrawInfo.Builder builder =
        core.network.proto.s2c.DrawInfo.newBuilder().setTexturePath(texturePath);
    if (drawInfo.scaleX() != null) {
      builder.setScaleX(drawInfo.scaleX());
    }
    if (drawInfo.scaleY() != null) {
      builder.setScaleY(drawInfo.scaleY());
    }

    String animationName = drawInfo.animationName();
    if (animationName != null && !animationName.isEmpty()) {
      int frameIndex = drawInfo.currentFrame() != null ? Math.max(0, drawInfo.currentFrame()) : 0;
      builder.setCurrentAnimation(
          core.network.proto.s2c.AnimationInfo.newBuilder()
              .setAnimationName(animationName)
              .setCurrentFrame(frameIndex)
              .build());
    }

    DrawInfoData.AnimationConfigData animationConfig = drawInfo.animationConfig();
    if (animationConfig == null) {
      throw new IllegalArgumentException("DrawInfoData.animationConfig is required.");
    }
    builder.setAnimationConfig(
        core.network.proto.s2c.AnimationConfigInfo.newBuilder()
            .setFramesPerSprite(animationConfig.framesPerSprite())
            .setLooping(animationConfig.looping())
            .setCentered(animationConfig.centered())
            .setMirrored(animationConfig.mirrored())
            .build());

    DrawInfoData.SpritesheetConfigData spritesheetConfig = drawInfo.spritesheetConfig();
    if (spritesheetConfig != null) {
      builder.setSpritesheetConfig(
          core.network.proto.s2c.SpritesheetConfigInfo.newBuilder()
              .setSpriteWidth(spritesheetConfig.spriteWidth())
              .setSpriteHeight(spritesheetConfig.spriteHeight())
              .setOffsetX(spritesheetConfig.offsetX())
              .setOffsetY(spritesheetConfig.offsetY())
              .setRows(spritesheetConfig.rows())
              .setColumns(spritesheetConfig.columns())
              .build());
    }

    List<DrawInfoData.StateData> states = drawInfo.states();
    if (states != null) {
      for (DrawInfoData.StateData state : states) {
        builder.addStates(toProtoStateInfo(state));
      }
    }

    return builder.build();
  }

  /**
   * Converts protobuf draw info into {@link DrawInfoData}.
   *
   * @param proto the protobuf draw info
   * @return the domain draw info
   */
  public static DrawInfoData fromProto(core.network.proto.s2c.DrawInfo proto) {
    if (proto.getTexturePath().isEmpty()) {
      throw new IllegalArgumentException("DrawInfo.texture_path is required.");
    }

    Float scaleX = proto.hasScaleX() ? proto.getScaleX() : null;
    Float scaleY = proto.hasScaleY() ? proto.getScaleY() : null;
    String animationName = null;
    Integer currentFrame = null;

    if (proto.hasCurrentAnimation()) {
      core.network.proto.s2c.AnimationInfo animationInfo = proto.getCurrentAnimation();
      if (!animationInfo.getAnimationName().isEmpty()) {
        animationName = animationInfo.getAnimationName();
        currentFrame = animationInfo.getCurrentFrame();
      }
    }

    if (!proto.hasAnimationConfig()) {
      throw new IllegalArgumentException("DrawInfo.animation_config is required.");
    }

    core.network.proto.s2c.AnimationConfigInfo animationConfigInfo = proto.getAnimationConfig();
    DrawInfoData.AnimationConfigData animationConfig =
        new DrawInfoData.AnimationConfigData(
            animationConfigInfo.getFramesPerSprite(),
            animationConfigInfo.getLooping(),
            animationConfigInfo.getCentered(),
            animationConfigInfo.getMirrored());

    DrawInfoData.SpritesheetConfigData spritesheetConfig = null;
    if (proto.hasSpritesheetConfig()) {
      core.network.proto.s2c.SpritesheetConfigInfo spritesheetConfigInfo =
          proto.getSpritesheetConfig();
      spritesheetConfig =
          new DrawInfoData.SpritesheetConfigData(
              spritesheetConfigInfo.getSpriteWidth(),
              spritesheetConfigInfo.getSpriteHeight(),
              spritesheetConfigInfo.getOffsetX(),
              spritesheetConfigInfo.getOffsetY(),
              spritesheetConfigInfo.getRows(),
              spritesheetConfigInfo.getColumns());
    }

    List<DrawInfoData.StateData> states = null;
    if (proto.getStatesCount() > 0) {
      states = new ArrayList<>(proto.getStatesCount());
      for (core.network.proto.s2c.DrawStateInfo stateInfo : proto.getStatesList()) {
        states.add(fromProtoStateInfo(stateInfo));
      }
    }

    return new DrawInfoData(
        proto.getTexturePath(),
        scaleX,
        scaleY,
        animationName,
        currentFrame,
        animationConfig,
        spritesheetConfig,
        states);
  }

  private static core.network.proto.s2c.DrawStateInfo toProtoStateInfo(
      DrawInfoData.StateData state) {
    if (state == null) {
      throw new IllegalArgumentException("DrawInfoData.states entry is required.");
    }
    String stateName = state.stateName();
    if (stateName == null || stateName.isBlank()) {
      throw new IllegalArgumentException("DrawInfoData.StateData.stateName is required.");
    }
    core.network.proto.s2c.DrawStateInfo.Builder builder =
        core.network.proto.s2c.DrawStateInfo.newBuilder()
            .setStateName(stateName)
            .setBaseAnimation(toProtoStateAnimation(state.baseAnimation()))
            .setStateType(toProtoStateType(state.stateType()));
    if (state.leftAnimation() != null) {
      builder.setLeftAnimation(toProtoStateAnimation(state.leftAnimation()));
    }
    if (state.upAnimation() != null) {
      builder.setUpAnimation(toProtoStateAnimation(state.upAnimation()));
    }
    if (state.rightAnimation() != null) {
      builder.setRightAnimation(toProtoStateAnimation(state.rightAnimation()));
    }
    return builder.build();
  }

  private static core.network.proto.s2c.StateAnimationInfo toProtoStateAnimation(
      DrawInfoData.StateAnimationData animation) {
    if (animation == null) {
      throw new IllegalArgumentException("DrawInfoData.StateData.baseAnimation is required.");
    }
    String texturePath = animation.texturePath();
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException(
          "DrawInfoData.StateAnimationData.texturePath is required.");
    }
    DrawInfoData.AnimationConfigData animationConfig = animation.animationConfig();
    if (animationConfig == null) {
      throw new IllegalArgumentException(
          "DrawInfoData.StateAnimationData.animationConfig is required.");
    }
    core.network.proto.s2c.StateAnimationInfo.Builder builder =
        core.network.proto.s2c.StateAnimationInfo.newBuilder()
            .setTexturePath(texturePath)
            .setAnimationConfig(
                core.network.proto.s2c.AnimationConfigInfo.newBuilder()
                    .setFramesPerSprite(animationConfig.framesPerSprite())
                    .setLooping(animationConfig.looping())
                    .setCentered(animationConfig.centered())
                    .setMirrored(animationConfig.mirrored())
                    .build());
    if (animation.scaleX() != null) {
      builder.setScaleX(animation.scaleX());
    }
    if (animation.scaleY() != null) {
      builder.setScaleY(animation.scaleY());
    }
    DrawInfoData.SpritesheetConfigData spritesheetConfig = animation.spritesheetConfig();
    if (spritesheetConfig != null) {
      builder.setSpritesheetConfig(
          core.network.proto.s2c.SpritesheetConfigInfo.newBuilder()
              .setSpriteWidth(spritesheetConfig.spriteWidth())
              .setSpriteHeight(spritesheetConfig.spriteHeight())
              .setOffsetX(spritesheetConfig.offsetX())
              .setOffsetY(spritesheetConfig.offsetY())
              .setRows(spritesheetConfig.rows())
              .setColumns(spritesheetConfig.columns())
              .build());
    }
    return builder.build();
  }

  private static core.network.proto.s2c.DrawStateType toProtoStateType(
      DrawInfoData.StateType stateType) {
    if (stateType == null) {
      return core.network.proto.s2c.DrawStateType.DRAW_STATE_TYPE_BASIC;
    }
    return switch (stateType) {
      case BASIC -> core.network.proto.s2c.DrawStateType.DRAW_STATE_TYPE_BASIC;
      case SIMPLE_DIRECTIONAL ->
          core.network.proto.s2c.DrawStateType.DRAW_STATE_TYPE_SIMPLE_DIRECTIONAL;
      case DIRECTIONAL -> core.network.proto.s2c.DrawStateType.DRAW_STATE_TYPE_DIRECTIONAL;
    };
  }

  private static DrawInfoData.StateData fromProtoStateInfo(
      core.network.proto.s2c.DrawStateInfo proto) {
    if (proto.getStateName().isEmpty()) {
      throw new IllegalArgumentException("DrawStateInfo.state_name is required.");
    }
    if (!proto.hasBaseAnimation()) {
      throw new IllegalArgumentException("DrawStateInfo.base_animation is required.");
    }
    DrawInfoData.StateAnimationData baseAnimation =
        fromProtoStateAnimation(proto.getBaseAnimation());
    DrawInfoData.StateAnimationData leftAnimation =
        proto.hasLeftAnimation() ? fromProtoStateAnimation(proto.getLeftAnimation()) : null;
    DrawInfoData.StateAnimationData upAnimation =
        proto.hasUpAnimation() ? fromProtoStateAnimation(proto.getUpAnimation()) : null;
    DrawInfoData.StateAnimationData rightAnimation =
        proto.hasRightAnimation() ? fromProtoStateAnimation(proto.getRightAnimation()) : null;
    return new DrawInfoData.StateData(
        proto.getStateName(),
        fromProtoStateType(proto.getStateType()),
        baseAnimation,
        leftAnimation,
        upAnimation,
        rightAnimation);
  }

  private static DrawInfoData.StateAnimationData fromProtoStateAnimation(
      core.network.proto.s2c.StateAnimationInfo proto) {
    if (proto.getTexturePath().isEmpty()) {
      throw new IllegalArgumentException("StateAnimationInfo.texture_path is required.");
    }
    if (!proto.hasAnimationConfig()) {
      throw new IllegalArgumentException("StateAnimationInfo.animation_config is required.");
    }
    Float scaleX = proto.hasScaleX() ? proto.getScaleX() : null;
    Float scaleY = proto.hasScaleY() ? proto.getScaleY() : null;
    core.network.proto.s2c.AnimationConfigInfo animationConfigInfo = proto.getAnimationConfig();
    DrawInfoData.AnimationConfigData animationConfig =
        new DrawInfoData.AnimationConfigData(
            animationConfigInfo.getFramesPerSprite(),
            animationConfigInfo.getLooping(),
            animationConfigInfo.getCentered(),
            animationConfigInfo.getMirrored());

    DrawInfoData.SpritesheetConfigData spritesheetConfig = null;
    if (proto.hasSpritesheetConfig()) {
      core.network.proto.s2c.SpritesheetConfigInfo spritesheetConfigInfo =
          proto.getSpritesheetConfig();
      spritesheetConfig =
          new DrawInfoData.SpritesheetConfigData(
              spritesheetConfigInfo.getSpriteWidth(),
              spritesheetConfigInfo.getSpriteHeight(),
              spritesheetConfigInfo.getOffsetX(),
              spritesheetConfigInfo.getOffsetY(),
              spritesheetConfigInfo.getRows(),
              spritesheetConfigInfo.getColumns());
    }
    return new DrawInfoData.StateAnimationData(
        proto.getTexturePath(), scaleX, scaleY, animationConfig, spritesheetConfig);
  }

  private static DrawInfoData.StateType fromProtoStateType(
      core.network.proto.s2c.DrawStateType proto) {
    return switch (proto) {
      case DRAW_STATE_TYPE_SIMPLE_DIRECTIONAL -> DrawInfoData.StateType.SIMPLE_DIRECTIONAL;
      case DRAW_STATE_TYPE_DIRECTIONAL -> DrawInfoData.StateType.DIRECTIONAL;
      case DRAW_STATE_TYPE_UNSPECIFIED, DRAW_STATE_TYPE_BASIC, UNRECOGNIZED ->
          DrawInfoData.StateType.BASIC;
    };
  }

  /**
   * Converts a {@link PlayerComponent} into its protobuf representation.
   *
   * @param component the domain player component
   * @return the protobuf player info
   */
  public static core.network.proto.s2c.PlayerInfo toProto(PlayerComponent component) {
    return core.network.proto.s2c.PlayerInfo.newBuilder()
        .setPlayerName(component.playerName())
        .setIsLocalPlayer(component.isLocal())
        .build();
  }

  /**
   * Converts protobuf player info into a {@link PlayerComponent}.
   *
   * @param proto the protobuf player info
   * @return the domain player component
   */
  public static PlayerComponent fromProto(core.network.proto.s2c.PlayerInfo proto) {
    return new PlayerComponent(proto.getIsLocalPlayer(), proto.getPlayerName());
  }

  /**
   * Converts an {@link Item} into its protobuf representation.
   *
   * @param item the domain item
   * @return the protobuf item
   */
  public static core.network.proto.common.Item toProto(Item item) {
    String itemType = ItemRegistry.idFor(item);
    core.network.proto.common.Item.Builder builder =
        core.network.proto.common.Item.newBuilder()
            .setItemType(itemType)
            .setStackSize(item.stackSize())
            .setMaxStackSize(item.maxStackSize());

    Map<String, String> itemData = item.itemData();
    if (itemData != null && !itemData.isEmpty()) {
      builder.putAllItemData(itemData);
    }

    return builder.build();
  }

  /**
   * Converts protobuf item data into an {@link Item}.
   *
   * @param proto the protobuf item
   * @return the domain item
   */
  public static Item fromProto(core.network.proto.common.Item proto) {
    String itemType = proto.getItemType();
    Class<? extends Item> itemClass =
        ItemRegistry.lookup(itemType)
            .orElseThrow(() -> new IllegalArgumentException("Unknown item type: " + itemType));

    Map<String, String> itemData = proto.getItemDataMap();
    try {
      Optional<Item> itemFromData =
          itemData.isEmpty() ? Optional.empty() : ItemRegistry.create(itemType, itemData);
      if (!itemData.isEmpty() && itemFromData.isEmpty()) {
        throw new IllegalArgumentException(
            "Item data provided but no factory registered for item type: " + itemType);
      }
      Item item;
      if (itemFromData.isPresent()) {
        item = itemFromData.get();
      } else {
        item = itemClass.getDeclaredConstructor().newInstance();
      }
      int maxStackSize = proto.getMaxStackSize();
      if (maxStackSize > 0) {
        item.maxStackSize(maxStackSize);
      }
      item.stackSize(proto.getStackSize());
      return item;
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("Failed to instantiate item type: " + itemType, e);
    }
  }

  /**
   * Parses a direction string into a {@link Direction}.
   *
   * @param direction the direction text
   * @return the parsed direction, or {@link Direction#NONE} on invalid values
   */
  public static Direction parseDirection(String direction) {
    if (direction == null) {
      return Direction.NONE;
    }
    try {
      return Direction.valueOf(direction);
    } catch (IllegalArgumentException e) {
      return Direction.NONE;
    }
  }

  /**
   * Converts an int to byte while validating range.
   *
   * @param value the value to convert
   * @param fieldName the field name for diagnostics
   * @return the converted byte value
   */
  public static byte toByteExact(int value, String fieldName) {
    if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
      throw new IllegalArgumentException(fieldName + " out of range for byte: " + value);
    }
    return (byte) value;
  }

  /**
   * Converts an int to short while validating range.
   *
   * @param value the value to convert
   * @param fieldName the field name for diagnostics
   * @return the converted short value
   */
  public static short toShortExact(int value, String fieldName) {
    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
      throw new IllegalArgumentException(fieldName + " out of range for short: " + value);
    }
    return (short) value;
  }
}
