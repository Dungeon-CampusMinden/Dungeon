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
    for (float x : new float[] {5.15f, 8.65f}) {
      Entity upright = sheet("winch-upright", end.translate(x, .1f), 336, 32, 16, 48, 1, 4);
      upright.add(new CollideComponent(Vector2.ZERO, Vector2.of(1, 1)));
    }
    prop("winch-beam", end.translate(5.15f, 3.85f), "objects/crate/basic.png", 4.5f, .45f);
    bracket =
        prop("winch-bracket", end.translate(6.8f, 3.55f), "objects/crate/basic.png", .9f, .4f);
    bracket.fetch(DrawComponent.class).orElseThrow().tintColor(0xA66D48FF);
    weightStart = end.translate(6.85f, 1.2f);
    weight = prop("counterweight", weightStart, "objects/push-stone.png", 1.4f, 1.4f);
    for (int i = 0; i < 3; i++) {
      Entity link = new Entity("programming-winch-chain-" + i);
      link.add(new PositionComponent(end.translate(7.15f, 2.6f + i * .3f)));
      link.add(
          new DrawComponent(
              new SimpleIPath("spritesheets/FD_Dungeon_Free.png"), new SpritesheetConfig(304, 80)));
      link.fetch(PositionComponent.class).orElseThrow().scale(Vector2.of(.8f, .8f));
      link.fetch(DrawComponent.class).orElseThrow().depth(DepthLayer.Player.depth());
      Game.add(link);
      chain.add(link);
    }

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

  static Entity sheet(
      String name, Point at, int x, int y, int w, int h, float width, float height) {
    Entity entity = new Entity("programming-cellar-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(Vector2.of(width * Math.min(w, h) / w, height * Math.min(w, h) / h));
    entity.add(position);
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath("spritesheets/FG_Cellar.png"), new SpritesheetConfig(x, y, 1, 1, w, h));
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
      bracket.fetch(PositionComponent.class).orElseThrow().rotation(-35);
    }
    if (time >= 2.2f && time < 3.3f) {
      float drop = Math.min(1, (time - 2.2f) / 1.1f);
      float fall = drop * drop;
      weight.fetch(PositionComponent.class).orElseThrow().position(weightStart.translate(0, -fall));
      for (int i = 0; i < chain.size(); i++) {
        var link = chain.get(i).fetch(PositionComponent.class).orElseThrow();
        link.position(
            new Point(weightStart.x() + .3f, weightStart.y() + 1.4f - fall + i * (.3f + fall / 2)));
        link.rotation((float) Math.sin(time * 24 + i) * 12);
      }
    }
    if (crossed(before, 3.3f)) {
      weight.fetch(PositionComponent.class).orElseThrow().position(weightStart.translate(0, -1));
      chain.forEach(link -> link.fetch(PositionComponent.class).orElseThrow().rotation(0));
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

  private void finish() {
    Runnable callback = finished;
    finished = null;
    callback.run();
  }
}
