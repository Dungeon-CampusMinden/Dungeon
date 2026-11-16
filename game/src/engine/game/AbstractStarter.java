package engine.game;

import engine.Game;
import engine.level.DungeonLevel;
import engine.level.loader.DungeonLoader;
import engine.network.SnapshotTranslator;
import engine.network.config.EntitySpawnStrategy;
import engine.network.config.NetworkConfig;
import engine.utils.IVoidFunction;
import engine.utils.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base configuration shared by the multiplayer {@link ClientStarter} and {@link ServerStarter}.
 *
 * <p>It captures the pre-run configuration that is identical for client and server (levels, config
 * file, audio, frame rate, network translators, registration and setup callbacks) and applies it in
 * {@link #applyShared()}. Concrete subclasses add their role-specific configuration in {@link
 * #apply()} (which the {@link MainMenu} invokes before {@link Game#run()}).
 *
 * <p>Instances are immutable and created through the role-specific builders, which extend the
 * self-referential {@link Builder} so all shared options are defined exactly once.
 */
public abstract class AbstractStarter {

  /** Port the role binds to / connects to. */
  protected final int port;

  private final List<Tuple<String, Class<? extends DungeonLevel>>> levels;
  private final IVoidFunction onConfigure;
  private final IVoidFunction onSetup;
  private final boolean disableAudio;
  private final SnapshotTranslator snapshotTranslator;
  private final EntitySpawnStrategy entitySpawnStrategy;

  /**
   * Creates a starter from the given builder.
   *
   * @param builder the builder holding the shared configuration
   */
  protected AbstractStarter(Builder<?> builder) {
    this.port = builder.port;
    this.levels = List.copyOf(builder.levels);
    this.onConfigure = builder.onConfigure;
    this.onSetup = builder.onSetup;
    this.disableAudio = builder.disableAudio;
    this.snapshotTranslator = builder.snapshotTranslator;
    this.entitySpawnStrategy = builder.entitySpawnStrategy;
  }

  /**
   * @return the in-loop setup callback of this role
   */
  final IVoidFunction onSetup() {
    return onSetup;
  }

  /**
   * Applies the full pre-run configuration of this role.
   *
   * <p>Implementations first apply their role-specific configuration (multiplayer flags, port,
   * character classes, ...) and then call {@link #applyShared()}.
   */
  public abstract void apply();

  /** Applies the configuration shared by client and server roles. */
  protected final void applyShared() {
    onConfigure.execute();
    levels.forEach(DungeonLoader::addLevel);
    Game.disableAudio(disableAudio);
    if (snapshotTranslator != null) {
      NetworkConfig.SNAPSHOT_TRANSLATOR = snapshotTranslator;
    }
    if (entitySpawnStrategy != null) {
      NetworkConfig.ENTITY_SPAWN_STRATEGY = entitySpawnStrategy;
    }
    Game.userOnSetup(onSetup);
  }

  /**
   * Self-referential builder base for the shared starter configuration.
   *
   * @param <T> the concrete builder type, returned by all configuration methods for fluent chaining
   */
  public abstract static class Builder<T extends Builder<T>> {

    private final IVoidFunction onSetup;
    private final List<Tuple<String, Class<? extends DungeonLevel>>> levels = new ArrayList<>();
    private int port = PreRunConfiguration.networkPort();
    private IVoidFunction onConfigure = () -> {};
    private boolean disableAudio = false;
    private SnapshotTranslator snapshotTranslator;
    private EntitySpawnStrategy entitySpawnStrategy;

    /**
     * Creates the builder with the required setup callback.
     *
     * @param onSetup the in-loop setup callback (registered via {@link Game#userOnSetup})
     */
    protected Builder(IVoidFunction onSetup) {
      this.onSetup = Objects.requireNonNull(onSetup, "onSetup");
    }

    /**
     * @return this builder, typed as the concrete builder
     */
    protected abstract T self();

    /**
     * Copies the configuration that is identical for all roles of one project (config file,
     * snapshot translator and entity spawn strategy) from an already created starter.
     *
     * @param starter the starter to inherit the shared configuration from
     * @return this builder
     */
    final T inheritSharedFrom(AbstractStarter starter) {
      Objects.requireNonNull(starter, "starter");
      this.snapshotTranslator = starter.snapshotTranslator;
      this.entitySpawnStrategy = starter.entitySpawnStrategy;
      return self();
    }

    /**
     * Sets the network port (default {@link PreRunConfiguration#networkPort()}).
     *
     * @param port the port in range {@code 1..65535}
     * @return this builder
     */
    public T port(int port) {
      if (port <= 0 || port > 65535) {
        throw new IllegalArgumentException("port must be in range 1..65535");
      }
      this.port = port;
      return self();
    }

    /**
     * Adds levels to register via {@link DungeonLoader#addLevel}.
     *
     * @param levels the level name/handler tuples
     * @return this builder
     */
    @SafeVarargs
    public final T levels(Tuple<String, Class<? extends DungeonLevel>>... levels) {
      Collections.addAll(this.levels, Objects.requireNonNull(levels, "levels"));
      return self();
    }

    /**
     * Sets a callback for pre-run registrations (items, dialogs, ...), run before the game starts.
     *
     * @param onConfigure the registration callback
     * @return this builder
     */
    public T onConfigure(IVoidFunction onConfigure) {
      this.onConfigure = Objects.requireNonNull(onConfigure, "onConfigure");
      return self();
    }

    /**
     * Sets whether audio is disabled (default {@code false}).
     *
     * @param disableAudio {@code true} to disable audio
     * @return this builder
     */
    public T disableAudio(boolean disableAudio) {
      this.disableAudio = disableAudio;
      return self();
    }

    /**
     * Sets the {@link SnapshotTranslator} used by {@link NetworkConfig}.
     *
     * @param snapshotTranslator the snapshot translator
     * @return this builder
     */
    public T snapshotTranslator(SnapshotTranslator snapshotTranslator) {
      this.snapshotTranslator = Objects.requireNonNull(snapshotTranslator, "snapshotTranslator");
      return self();
    }

    /**
     * Sets the {@link EntitySpawnStrategy} used by {@link NetworkConfig}.
     *
     * @param entitySpawnStrategy the entity spawn strategy
     * @return this builder
     */
    public T entitySpawnStrategy(EntitySpawnStrategy entitySpawnStrategy) {
      this.entitySpawnStrategy = Objects.requireNonNull(entitySpawnStrategy, "entitySpawnStrategy");
      return self();
    }
  }
}
