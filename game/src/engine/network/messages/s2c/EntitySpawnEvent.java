package engine.network.messages.s2c;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.network.codec.ShaderComponentCodec;
import engine.network.messages.NetworkMessage;
import engine.network.messages.c2s.RequestEntitySpawn;
import engine.utils.components.draw.DrawComponentFactory;
import engine.utils.components.draw.DrawInfoData;
import feature.components.CharacterClassComponent;
import feature.shader.ShaderComponent;
import java.util.Map;
import java.util.Objects;

/**
 * Server→client: spawn entity.
 *
 * <p>Expected max size: small (<= 256 bytes).
 *
 * <p>This should be sent in response to a {@link RequestEntitySpawn} message and if a new entity is
 * created on the server that the client needs to know about.
 *
 * <p>For data-only entities, {@code positionComponent} and {@code drawInfo} can be absent and
 * custom state can be conveyed via {@code metadata}.
 *
 * @see CharacterClassComponent CharacterClassComponent for mapping characterClassId to
 *     CharacterClass
 */
public final class EntitySpawnEvent implements NetworkMessage {

  private final int entityId;
  private final PositionComponent positionComponent;
  private final DrawInfoData drawInfo;
  private final PlayerComponent playerComponent;
  private final byte characterClassId;
  private final ShaderComponentState shaderComponent;
  private final Map<String, String> metadata;

  /**
   * Constructor from Entity object.
   *
   * <p>This will throw {@link java.util.NoSuchElementException} if the entity does not have {@link
   * PositionComponent} or {@link DrawComponent}.
   *
   * @param entity the entity to create the event from
   */
  public EntitySpawnEvent(Entity entity) {
    this(
        entity.id(),
        entity.fetch(PositionComponent.class).orElseThrow(),
        DrawComponentFactory.toDrawInfo(entity.fetch(DrawComponent.class).orElseThrow()),
        entity.fetch(PlayerComponent.class).orElse(null),
        entity
            .fetch(CharacterClassComponent.class)
            .map(ccc -> (byte) ccc.characterClass().ordinal())
            .orElse((byte) 0),
        ShaderComponentCodec.toState(entity.fetch(ShaderComponent.class).orElse(null)),
        Map.of());
  }

  /**
   * Constructs a spawn event without metadata.
   *
   * @param entityId the entity id
   * @param positionComponent the entity position component, may be null for data-only entities
   * @param drawInfo the draw info, may be null for data-only entities
   * @param playerComponent the player component, may be null
   * @param characterClassId the character class id
   */
  public EntitySpawnEvent(
      int entityId,
      PositionComponent positionComponent,
      DrawInfoData drawInfo,
      PlayerComponent playerComponent,
      byte characterClassId) {
    this(
        entityId,
        positionComponent,
        drawInfo,
        playerComponent,
        characterClassId,
        null,
        Map.of());
  }

  /**
   * Constructs a spawn event.
   *
   * @param entityId the entity id
   * @param positionComponent the entity position component, may be null for data-only entities
   * @param drawInfo the draw info, may be null for data-only entities
   * @param playerComponent the player component, may be null
   * @param characterClassId the character class id
   * @param metadata metadata for subproject-specific state
   */
  public EntitySpawnEvent(
      int entityId,
      PositionComponent positionComponent,
      DrawInfoData drawInfo,
      PlayerComponent playerComponent,
      byte characterClassId,
      Map<String, String> metadata) {
    this(
        entityId,
        positionComponent,
        drawInfo,
        playerComponent,
        characterClassId,
        null,
        metadata);
  }

  /**
   * Constructs a spawn event.
   *
   * @param entityId the entity id
   * @param positionComponent the entity position component, may be null for data-only entities
   * @param drawInfo the draw info, may be null for data-only entities
   * @param playerComponent the player component, may be null
   * @param characterClassId the character class id
   * @param shaderComponent synchronized shader state, may be null
   * @param metadata metadata for subproject-specific state
   */
  public EntitySpawnEvent(
      int entityId,
      PositionComponent positionComponent,
      DrawInfoData drawInfo,
      PlayerComponent playerComponent,
      byte characterClassId,
      ShaderComponentState shaderComponent,
      Map<String, String> metadata) {
    this.entityId = entityId;
    this.positionComponent = positionComponent;
    this.drawInfo = drawInfo;
    this.playerComponent = playerComponent;
    this.characterClassId = characterClassId;
    this.shaderComponent = shaderComponent;
    this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }

  /**
   * Creates a builder for {@link EntitySpawnEvent}.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the unique id of the entity.
   *
   * @return the entity id
   */
  public int entityId() {
    return entityId;
  }

  /**
   * Gets the entity position component.
   *
   * @return the position component, or null for data-only entities
   */
  public PositionComponent positionComponent() {
    return positionComponent;
  }

  /**
   * Gets draw info for the entity.
   *
   * @return the draw info, or null for data-only entities
   */
  public DrawInfoData drawInfo() {
    return drawInfo;
  }

  /**
   * Gets player component info for hero entities.
   *
   * @return the player component, or null if not a player entity
   */
  public PlayerComponent playerComponent() {
    return playerComponent;
  }

  /**
   * Gets character class id for player entities.
   *
   * @return the character class id (0 if absent)
   */
  public byte characterClassId() {
    return characterClassId;
  }

  /**
   * Gets synchronized shader component state.
   *
   * @return shader component state, or null when absent
   */
  public ShaderComponentState shaderComponent() {
    return shaderComponent;
  }

  /**
   * Gets metadata for subproject-specific state.
   *
   * @return immutable metadata map
   */
  public Map<String, String> metadata() {
    return metadata;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EntitySpawnEvent other)) {
      return false;
    }
    return entityId == other.entityId
        && characterClassId == other.characterClassId
        && Objects.equals(positionComponent, other.positionComponent)
        && Objects.equals(drawInfo, other.drawInfo)
        && Objects.equals(playerComponent, other.playerComponent)
        && Objects.equals(shaderComponent, other.shaderComponent)
        && Objects.equals(metadata, other.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        entityId,
        positionComponent,
        drawInfo,
        playerComponent,
        characterClassId,
        shaderComponent,
        metadata);
  }

  @Override
  public String toString() {
    return "EntitySpawnEvent{"
        + "entityId="
        + entityId
        + ", positionComponent="
        + positionComponent
        + ", drawInfo="
        + drawInfo
        + ", playerComponent="
        + playerComponent
        + ", characterClassId="
        + characterClassId
        + ", shaderComponent="
        + shaderComponent
        + ", metadata="
        + metadata
        + "}";
  }

  /** Builder for {@link EntitySpawnEvent}. */
  public static final class Builder {
    private int entityId;
    private PositionComponent positionComponent;
    private DrawInfoData drawInfo;
    private PlayerComponent playerComponent;
    private byte characterClassId;
    private ShaderComponentState shaderComponent;
    private Map<String, String> metadata = Map.of();

    private Builder() {}

    /**
     * Sets the entity id.
     *
     * @param entityId the entity id
     * @return this builder
     */
    public Builder entityId(int entityId) {
      this.entityId = entityId;
      return this;
    }

    /**
     * Sets the position component.
     *
     * @param positionComponent the position component
     * @return this builder
     */
    public Builder positionComponent(PositionComponent positionComponent) {
      this.positionComponent = positionComponent;
      return this;
    }

    /**
     * Sets draw info.
     *
     * @param drawInfo draw info
     * @return this builder
     */
    public Builder drawInfo(DrawInfoData drawInfo) {
      this.drawInfo = drawInfo;
      return this;
    }

    /**
     * Sets player component.
     *
     * @param playerComponent player component
     * @return this builder
     */
    public Builder playerComponent(PlayerComponent playerComponent) {
      this.playerComponent = playerComponent;
      return this;
    }

    /**
     * Sets character class id.
     *
     * @param characterClassId character class id
     * @return this builder
     */
    public Builder characterClassId(byte characterClassId) {
      this.characterClassId = characterClassId;
      return this;
    }

    /**
     * Sets synchronized shader component state.
     *
     * @param shaderComponent shader component state
     * @return this builder
     */
    public Builder shaderComponent(ShaderComponentState shaderComponent) {
      this.shaderComponent = shaderComponent;
      return this;
    }

    /**
     * Sets metadata.
     *
     * @param metadata metadata map
     * @return this builder
     */
    public Builder metadata(Map<String, String> metadata) {
      this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
      return this;
    }

    /**
     * Builds the event instance.
     *
     * @return a new {@link EntitySpawnEvent}
     */
    public EntitySpawnEvent build() {
      return new EntitySpawnEvent(
          entityId,
          positionComponent,
          drawInfo,
          playerComponent,
          characterClassId,
          shaderComponent,
          metadata);
    }
  }
}
