package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the module sockets and module chips used by riddle 2. */
public final class ModuleEntityFactory {

  private ModuleEntityFactory() {}

  /**
   * Creates an inactive module socket.
   *
   * @param point socket position
   * @return configured inactive module socket
   */
  public static Entity moduleSocket(Point point) {
    return moduleSocket(point, false);
  }

  /**
   * Creates a module socket with its active or inactive appearance.
   *
   * @param point socket position
   * @param active whether the socket is already activated
   * @return configured module socket
   */
  public static Entity moduleSocket(Point point, boolean active) {
    Entity entity = new Entity("module_socket");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(
        new DrawComponent(
            new SimpleIPath(
                active ? "objects/tech/Screen_info_1.png" : "objects/tech/Screen_info_2.png")));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (socket, who) -> {
                  DialogUtils.showTextPopup(
                      moduleSocketStatus(socket),
                      SystemRecoveryText.key("world.module.socket-title"),
                      who.id());
                  announceGpuFaultIfInspected(socket, who);
                })));
    if (active) entity.name("module_socket_active");
    return entity;
  }

  /**
   * Marks a socket as active while preserving an occupied socket name.
   *
   * @param socket socket to activate
   */
  public static void activateModuleSocket(Entity socket) {
    socket.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    if (!socket.name().startsWith("module_socket_occupied_")) {
      socket.name("module_socket_active");
    }
  }

  /**
   * Marks an active socket as occupied by a module.
   *
   * @param socket socket to occupy
   * @param moduleName module name encoded in the entity name
   */
  public static void occupyModuleSocket(Entity socket, String moduleName) {
    socket.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    socket.name("module_socket_occupied_" + moduleName.toLowerCase());
  }

  /**
   * Marks an occupied socket as active and empty again.
   *
   * @param socket socket to clear
   */
  public static void clearModuleSocket(Entity socket) {
    socket.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    socket.name("module_socket_active");
  }

  /**
   * Creates a visible module chip at the supplied point.
   *
   * @param point chip position
   * @param moduleName module name shown when the chip is examined
   * @return configured module chip
   */
  public static Entity moduleChip(Point point, String moduleName) {
    Entity entity = new Entity("module_" + moduleName.toLowerCase());
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) -> {
                  DialogUtils.showTextPopup(
                      moduleName.equals("GPU")
                          ? SystemRecoveryText.key("world.module.gpu-broken")
                          : SystemRecoveryText.key("world.module.chip-occupied", moduleName),
                      SystemRecoveryText.key("world.module.socket-title"),
                      who.id());
                  if (moduleName.equals("GPU")) {
                    SystemRecoveryLevel.announceStoryForPlayer(
                        SystemRecoveryStoryDialogs.GPU_FAULT, who.id());
                  }
                })));
    return entity;
  }

  private static String moduleSocketStatus(Entity socket) {
    String name = socket.name();
    if (name.startsWith("module_socket_occupied_")) {
      String moduleName = name.substring("module_socket_occupied_".length());
      return SystemRecoveryText.key("world.module.socket-occupied", moduleName.toUpperCase());
    }
    if (name.equals("module_socket_active")) {
      return SystemRecoveryText.key("world.module.socket-active");
    }
    return SystemRecoveryText.key("world.module.socket-inactive");
  }

  private static void announceGpuFaultIfInspected(Entity socket, Entity player) {
    if ("module_socket_occupied_gpu".equals(socket.name())) {
      SystemRecoveryLevel.announceStoryForPlayer(SystemRecoveryStoryDialogs.GPU_FAULT, player.id());
    }
  }
}
