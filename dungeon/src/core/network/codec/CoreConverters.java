package core.network.codec;

import core.network.codec.converters.c2s.ConnectRequestConverter;
import core.network.codec.converters.c2s.DialogResponseConverter;
import core.network.codec.converters.c2s.InputMessageConverter;
import core.network.codec.converters.c2s.RegisterUdpConverter;
import core.network.codec.converters.c2s.RequestEntitySpawnConverter;
import core.network.codec.converters.c2s.SoundFinishedConverter;
import core.network.codec.converters.s2c.ConnectAckConverter;
import core.network.codec.converters.s2c.ConnectRejectConverter;
import core.network.codec.converters.s2c.DialogCloseConverter;
import core.network.codec.converters.s2c.DialogShowConverter;
import core.network.codec.converters.s2c.EntityDespawnConverter;
import core.network.codec.converters.s2c.EntitySpawnBatchConverter;
import core.network.codec.converters.s2c.EntitySpawnEventConverter;
import core.network.codec.converters.s2c.EntityStateConverter;
import core.network.codec.converters.s2c.GameOverConverter;
import core.network.codec.converters.s2c.LevelChangeConverter;
import core.network.codec.converters.s2c.RegisterAckConverter;
import core.network.codec.converters.s2c.SnapshotConverter;
import core.network.codec.converters.s2c.SoundPlayConverter;
import core.network.codec.converters.s2c.SoundStopConverter;

/** Registers all built-in dungeon network message converters. */
public final class CoreConverters {

  private CoreConverters() {}

  /**
   * Registers all core dungeon converters in the provided registry.
   *
   * @param registry the target registry
   */
  public static void registerAll(ConverterRegistry registry) {
    registry.register(new ConnectRequestConverter());
    registry.register(new InputMessageConverter());
    registry.register(new DialogResponseConverter());
    registry.register(new RegisterUdpConverter());
    registry.register(new RequestEntitySpawnConverter());
    registry.register(new SoundFinishedConverter());
    registry.register(new ConnectAckConverter());
    registry.register(new ConnectRejectConverter());
    registry.register(new DialogShowConverter());
    registry.register(new DialogCloseConverter());
    registry.register(new EntitySpawnEventConverter());
    registry.register(new EntitySpawnBatchConverter());
    registry.register(new EntityDespawnConverter());
    registry.register(new EntityStateConverter());
    registry.register(new GameOverConverter());
    registry.register(new LevelChangeConverter());
    registry.register(new RegisterAckConverter());
    registry.register(new SnapshotConverter());
    registry.register(new SoundPlayConverter());
    registry.register(new SoundStopConverter());
  }
}
