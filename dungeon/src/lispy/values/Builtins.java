package lispy.values;

import contrib.components.BlockComponent;
import contrib.components.CollideComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/** Builtin functions for Lispy. */
public class Builtins {

  /** Support for list operations. */
  public static Map<String, Function<List<Value>, Value>> listsupport =
      Map.of(
          "list", ListVal::of,
          "cons",
              args -> {
                if (args.size() != 2) throw new RuntimeException("cons: expected two arguments");

                Value head = args.getFirst();
                ListVal tail = Value.asList(args.getLast());
                List<Value> out = new ArrayList<>(tail.elements().size() + 1);
                out.add(head);
                out.addAll(tail.elements());
                return ListVal.of(out);
              },
          "head",
              args -> {
                if (args.size() != 1) throw new RuntimeException("head: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                if (l.isEmpty()) throw new RuntimeException("head: got empty list");
                return l.elements().getFirst();
              },
          "tail",
              args -> {
                if (args.size() != 1) throw new RuntimeException("tail: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                if (l.isEmpty()) throw new RuntimeException("tail: got empty list");
                return ListVal.of(l.elements().subList(1, l.elements().size()));
              },
          "empty?",
              args -> {
                if (args.size() != 1) throw new RuntimeException("empty?: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                return new BoolVal(l.isEmpty());
              },
          "length",
              args -> {
                if (args.size() != 1) throw new RuntimeException("length: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                return new NumVal(l.elements().size());
              },
          "append",
              args ->
                  ListVal.of(
                      args.stream()
                          .map(Value::asList)
                          .flatMap(l -> l.elements().stream())
                          .toList()),
          "nth",
              args -> {
                if (args.size() != 2) throw new RuntimeException("nth: expected two arguments");

                int i = Value.asNum(args.getFirst());
                ListVal l = Value.asList(args.getLast());
                if (i < 0 || i >= l.elements().size())
                  throw new RuntimeException("nth: index out of bounds");
                return l.elements().get(i);
              });

  /** Support for printing values. */
  public static Map<String, Function<List<Value>, Value>> print =
      Map.of(
          "print",
          args -> {
            String line = args.stream().map(Value::pretty).reduce((a, b) -> a + " " + b).orElse("");
            System.out.println(line);
            return args.isEmpty() ? new BoolVal(true) : args.getLast();
          });

  /** Support for comparing values. */
  public static Map<String, Function<List<Value>, Value>> logicsupport =
      Map.of(
          "not",
          args -> {
            if (args.size() != 1) throw new RuntimeException("not: expected one argument");
            return new BoolVal(!Value.isTruthy(args.getFirst()));
          },
          "=",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("=: expected at least one argument");

            if (args.size() == 1) return new BoolVal(true);
            Value res = args.getFirst();
            return new BoolVal(args.stream().skip(1).allMatch(v -> Value.valueEquals(res, v)));
          },
          ">",
          args -> {
            if (args.isEmpty()) throw new RuntimeException(">: expected at least one argument");

            List<Integer> list = args.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) > 0));
          },
          "<",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("<: expected at least one argument");

            List<Integer> list = args.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) < 0));
          });

  /** Support for arithmetic operations. */
  public static Map<String, Function<List<Value>, Value>> mathsupport =
      Map.of(
          "+",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("+: expected at least one argument");
            return new NumVal(args.stream().map(Value::asNum).reduce(0, Integer::sum));
          },
          "-",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("-: expected at least one argument");

            int res = Value.asNum(args.getFirst());
            if (args.size() == 1) return new NumVal(-1 * res);
            return new NumVal(args.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a - b));
          },
          "*",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("*: expected at least one argument");
            return new NumVal(args.stream().map(Value::asNum).reduce(1, (a, b) -> a * b));
          },
          "/",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("/: expected at least one argument");

            int res = Value.asNum(args.getFirst());
            if (args.size() == 1) return new NumVal(1 / res);
            return new NumVal(args.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a / b));
          });

  /**
   * Support for dungeon functions (like in PRODUS).
   *
   * <p>This is real shit from utils.BlocklyCommands (blockly project).
   */
  public static Map<String, Function<List<Value>, Value>> dungeonsupport =
      Map.of(
          "shootFireball",
          args -> {
            Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
            new Skill(
                    new FireballSkill(
                        () ->
                            hero.fetch(CollideComponent.class)
                                .map(cc -> cc.center(hero))
                                .map(p -> p.translate(EntityUtils.getViewDirection(hero)))
                                .orElseThrow(
                                    () ->
                                        MissingComponentException.build(
                                            hero, CollideComponent.class)),
                        Integer.MAX_VALUE,
                        15f,
                        1),
                    1)
                .execute(hero);

            return new BoolVal(true);
          },
          "move",
          args -> {
            Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
            Direction viewDirection = EntityUtils.getViewDirection(hero);
            move(viewDirection, hero);
//            Game.levelEntities()
//                .filter(entity -> entity.name().equals("Blockly Black Knight"))
//                .findFirst()
//                .ifPresent(
//                    boss ->
//                        boss.fetch(PositionComponent.class)
//                            .ifPresent(pc -> move(pc.viewDirection(), boss)));

            return new BoolVal(true);
          },
          "turnleft",
          args -> {
            Entity entity = Game.hero().orElseThrow(MissingHeroException::new);
            PositionComponent pc =
                entity
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, PositionComponent.class));
            VelocityComponent vc =
                entity
                    .fetch(VelocityComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, VelocityComponent.class));
            Point oldP = pc.position();
            vc.applyForce("Movement", Direction.LEFT);
            // so the player can not glitch inside the next tile
            pc.position(oldP);

            return new BoolVal(true);
          },
          "turnright",
          args -> {
            Entity entity = Game.hero().orElseThrow(MissingHeroException::new);
            PositionComponent pc =
                entity
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, PositionComponent.class));
            VelocityComponent vc =
                entity
                    .fetch(VelocityComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, VelocityComponent.class));
            Point oldP = pc.position();
            vc.applyForce("Movement", Direction.RIGHT);
            // so the player can not glitch inside the next tile
            pc.position(oldP);

            return new BoolVal(true);
          });

  private static void move(final Direction direction, final Entity... entities) {
    double distanceThreshold = 0.1;

    record EntityComponents(
        PositionComponent pc, VelocityComponent vc, Coordinate targetPosition) {}

    List<EntityComponents> entityComponents = new ArrayList<>();

    for (Entity entity : entities) {
      PositionComponent pc =
          entity
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

      VelocityComponent vc =
          entity
              .fetch(VelocityComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

      Tile targetTile = Game.tileAt(pc.position(), direction).orElse(null);
      if (targetTile == null
          || (!targetTile.isAccessible() && !(targetTile instanceof PitTile))
          || Game.entityAtTile(targetTile).anyMatch(e -> e.isPresent(BlockComponent.class))) {
        return; // if any target tile is not accessible, don't move anyone
      }

      entityComponents.add(new EntityComponents(pc, vc, targetTile.coordinate()));
    }

    double[] distances =
        entityComponents.stream()
            .mapToDouble(e -> e.pc.position().distance(e.targetPosition.toCenteredPoint()))
            .toArray();
    double[] lastDistances = new double[entities.length];

    while (true) {
      boolean allEntitiesArrived = true;
      for (int i = 0; i < entities.length; i++) {
        EntityComponents comp = entityComponents.get(i);
        comp.vc.clearForces();
        comp.vc.currentVelocity(Vector2.ZERO);
        comp.vc.applyForce("Movement", direction.scale(7.5));

        lastDistances[i] = distances[i];
        distances[i] = comp.pc.position().distance(comp.targetPosition.toCenteredPoint());

        if (comp.vc().maxSpeed() > 0
            && Game.existInLevel(entities[i])
            && !(distances[i] <= distanceThreshold || distances[i] > lastDistances[i])) {
          allEntitiesArrived = false;
        }
      }

      if (allEntitiesArrived) break;
    }

    for (EntityComponents ec : entityComponents) {
      ec.vc.currentVelocity(Vector2.ZERO);
      ec.vc.clearForces();
      // check the position-tile via new request in case a new level was loaded
      Tile endTile = Game.tileAt(ec.pc.position()).orElse(null);
      if (endTile != null) ec.pc.position(endTile); // snap to grid
    }
  }
}
