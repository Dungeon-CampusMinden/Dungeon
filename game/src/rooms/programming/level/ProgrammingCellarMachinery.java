package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import java.util.ArrayList;
import java.util.List;
import rooms.programming.modules.loops.LoopMaze;

/** Server-owned clearing work and the broken counterweight that releases the upstairs wall. */
final class ProgrammingCellarMachinery {
  private final DungeonLevel level;
  private final Entity golem;
  private final List<List<Entity>> debris = new ArrayList<>();
  private final List<Entity> wall = new ArrayList<>();
  private final List<Entity> chain = new ArrayList<>();
  private final Entity weight;
  private final Entity bracket;
  private final Entity focus;
  private final Point weightStart;
  private UIComponent scene;
  private Runnable finished;
  private int station;
  private float time;

  ProgrammingCellarMachinery(DungeonLevel level, Entity golem) {
    this.level = level;
    this.golem = golem;
    Point origin = level.getPoint("maze-origin");
    for (var checkpoint : LoopMaze.checkpoints()) {
      Point goal = LoopMaze.world(origin, checkpoint.goal());
      List<Entity> pile = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        Point at = goal.translate(1 + i * .85f, 2.1f + (i % 2) * .1f);
        if (debris.size() == 4) at = goal.translate(5.2f + i * .8f, .1f);
        Entity stone = prop("rubble", at, "objects/stone", .8f, .55f);
        stone.fetch(DrawComponent.class).orElseThrow().depth(DepthLayer.Ground.depth());
        pile.add(stone);
      }
      debris.add(pile);
    }

    Point end = LoopMaze.world(origin, LoopMaze.checkpoints().getLast().goal());
    Entity frame = art("winch-frame", end.translate(5.15f, .1f), "winch-frame", 48, 40, 3.75f);
    // Collider units follow the 40px shorter edge, including both feet of the 48px-wide frame.
    frame.add(new CollideComponent(Vector2.of(.05f, .08f), Vector2.of(1.1f, .13f)));
    bracket =
        art("winch-bracket", end.translate(6.725f, 2.3825f), "winch-bracket-intact", 16, 8, .45f);
    bracket.add(
        new DrawComponent(
            new StateMachine(
                List.of(
                    new State(
                        "idle",
                        new SimpleIPath("rooms/programming/art/winch-bracket-intact.png"),
                        new SpritesheetConfig(0, 0, 1, 1, 16, 8)),
                    new State(
                        "broken",
                        new SimpleIPath("rooms/programming/art/winch-bracket-broken.png"),
                        new SpritesheetConfig(0, 0, 1, 1, 16, 8))))));
    weightStart = end.translate(6.7f, 1.2f);
    weight = art("counterweight", weightStart, "winch-weight", 16, 16, 1.4f);
    for (int i = 0; i < 8; i++)
      chain.add(art("winch-chain-" + i, weightStart, "winch-chain", 4, 8, .16f));
    updateChain(0);

    Point gate = level.getPoint("act2-gate-start");
    var wallTexture = level.tileAt(gate.translate(-1, 0)).orElseThrow().texturePath();
    for (int x = (int) gate.x(); x <= level.getPoint("act2-gate-end").x(); x++) {
      Point at = new Point(x, gate.y());
      Entity floor = prop("wall-threshold", at, "rooms/programming/sluice.png", 1, 1);
      floor.fetch(DrawComponent.class).orElseThrow().depth(DepthLayer.Ground.depth());
      wall.add(prop("sliding-wall", at, wallTexture.pathString(), 1, 1));
    }
    focus = prop("sequence-focus", end.translate(4, 1), "objects/stone", 1, 1);
    focus.fetch(DrawComponent.class).orElseThrow().tintColor(0xFFFFFF00);
  }

  static Entity prop(String name, Point at, String path, float width, float height) {
    Entity entity = new Entity("programming-cellar-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(Vector2.of(width, height));
    entity.add(position);
    DrawComponent draw = new DrawComponent(new SimpleIPath(path));
    if (path.equals("objects/stone")) draw.stateMachine().setState("idle", null);
    draw.depth(DepthLayer.Player.depth());
    entity.add(draw);
    Game.add(entity);
    return entity;
  }

  /**
   * Native frame dimensions keep client rendering and headless geometry in agreement.
   *
   * @param name entity name suffix
   * @param at lower-left sprite anchor
   * @param file room art filename without extension
   * @param width native frame width in pixels
   * @param height native frame height in pixels
   * @param scale world size of the shorter frame edge
   * @return the spawned prop
   */
  static Entity art(String name, Point at, String file, int width, int height, float scale) {
    Entity entity = new Entity("programming-cellar-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(scale);
    entity.add(position);
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath("rooms/programming/art/" + file + ".png"),
            new SpritesheetConfig(0, 0, 1, 1, width, height));
    draw.depth(DepthLayer.Player.depth());
    entity.add(draw);
    Game.add(entity);
    return entity;
  }

  boolean working() {
    return finished != null;
  }

  void clear(int index, Runnable onFinished) {
    station = index;
    time = 0;
    finished = onFinished;
    if (station == 4) {
      // Remove terminal/reading overlays before changing cameras, including another player's view.
      Game.levelEntities()
          .flatMap(e -> e.fetch(UIComponent.class).stream())
          .toList()
          .forEach(ui -> UIUtils.closeDialog(ui, true));
      Game.allPlayers().forEach(ProgrammingTerminal::stopWalking);
    }
  }

  void tick(float delta) {
    float before = time;
    time += delta;
    var body = golem.fetch(PositionComponent.class).orElseThrow();
    if (time < 1.4f) body.rotation((float) Math.sin(time * 9) * 5);
    else body.rotation(0);
    if (crossed(before, 1.1f)) {
      for (Entity stone : debris.get(station))
        stone.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("breaking", null);
    }
    if (crossed(before, 1.7f)) debris.get(station).forEach(Game::remove);
    if (station != 4) {
      if (time >= 1.8f) finish();
      return;
    }
    if (crossed(before, .35f)) scene = ProgrammingObservation.sequence(focus.id());
    if (crossed(before, 2.2f)) {
      bracket.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("broken", null);
    }
    if (time >= 2.2f && time < 3.3f) {
      float drop = Math.min(1, (time - 2.2f) / 1.1f);
      float fall = drop * drop;
      weight.fetch(PositionComponent.class).orElseThrow().position(weightStart.translate(0, -fall));
      updateChain(fall);
    }
    if (crossed(before, 3.3f)) {
      weight.fetch(PositionComponent.class).orElseThrow().position(weightStart.translate(0, -1));
      updateChain(1);
    }
    if (crossed(before, 4f)) {
      Point start = level.getPoint("act2-gate-start");
      Point end = level.getPoint("act2-gate-end");
      focus
          .fetch(PositionComponent.class)
          .orElseThrow()
          .position(new Point((start.x() + end.x()) / 2, start.y() - 2));
    }
    if (time >= 5 && time < 6.6f) {
      float lift = (time - 5) / 1.6f;
      for (Entity slab : wall) {
        var at = slab.fetch(PositionComponent.class).orElseThrow();
        at.position(new Point(at.position().x(), level.getPoint("act2-gate-start").y() + lift));
      }
    }
    if (crossed(before, 6.6f)) {
      wall.forEach(Game::remove);
      ProgrammingGates.open(level, 2);
    }
    if (time >= 7.4f) {
      if (scene != null) UIUtils.closeDialog(scene, true);
      finish();
    }
  }

  private boolean crossed(float before, float event) {
    return before < event && time >= event;
  }

  private void updateChain(float drop) {
    // Feed rigid links from the drum instead of stretching the chain sprites during the fall.
    for (int i = 0; i < chain.size(); i++) {
      Entity link = chain.get(i);
      float y = weightStart.y() + 1.205f - i * .25f;
      link.fetch(PositionComponent.class)
          .orElseThrow()
          .position(new Point(weightStart.x() + .62f, y));
      link.fetch(DrawComponent.class)
          .orElseThrow()
          .tintColor(y + .32f >= weightStart.y() + 1.28f - drop ? -1 : 0xFFFFFF00);
    }
  }

  private void finish() {
    Runnable callback = finished;
    finished = null;
    callback.run();
  }
}
