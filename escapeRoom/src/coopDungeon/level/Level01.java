package coopDungeon.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.systems.EventScheduler;
import contrib.utils.IComponentCommand;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

public class Level01 extends DungeonLevel {

  public static final int DELAY_MILLIS = 1000;
  private DoorTile door;
  private ExitTile exit;
  private LeverComponent l1, l2, l3, l4;
  private EventScheduler.ScheduledAction a1, a2 = null;

  public Level01(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 1");
  }

  @Override
  protected void onFirstTick() {
    setupLever();
    door = (DoorTile) Game.randomTile(LevelElement.DOOR).get();
    door.close();
    exit = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    exit.close();
  }

  private void setupLever() {
    Entity l = LeverFactory.pressurePlate(customPoints.get(0).toCenteredPoint());
    Game.add(l);
    l1 = l.fetch(LeverComponent.class).get();

    l = LeverFactory.pressurePlate(customPoints.get(1).toCenteredPoint());
    Game.add(l);
    l2 = l.fetch(LeverComponent.class).get();

    // TODO move to LeverFactory as timedLever
    l =
        LeverFactory.createLever(
            customPoints.get(2).toCenteredPoint(),
            new IComponentCommand<LeverComponent>() {
              @Override
              public void execute(LeverComponent comp) {
                try {
                  System.out.println("EXECUTE");
                  throw new RuntimeException();
                } catch (Exception exception) {
                  exception.printStackTrace();
                }
                if (a1 == null || !EventScheduler.isScheduled(a1)) {
                  System.out.println("SCHEDULE");
                  a1 =
                      EventScheduler.scheduleAction(
                          () -> {
                            System.out.println("ACTION");
                            if (comp.isOn()) {
                              System.out.println("TOGGLE");
                              comp.toggle();
                              Entity lever =
                                  Game.entityAtTile(Game.tileAT(customPoints.get(2)))
                                      .filter(entity -> entity.isPresent(LeverComponent.class))
                                      .findFirst()
                                      .get();
                              lever
                                  .fetch(DrawComponent.class)
                                  .ifPresent(
                                      drawComponent ->
                                          drawComponent.currentAnimation(
                                              comp.isOn() ? "on" : "off"));
                            }
                          },
                          DELAY_MILLIS);
                }
              }

              @Override
              public void undo(LeverComponent comp) {
                System.out.println("REMOVE");
                EventScheduler.cancelAction(a1);
              }
            });

    l3 = l.fetch(LeverComponent.class).get();
    Game.add(l);
    l =
        LeverFactory.createLever(
            customPoints.get(3).toCenteredPoint(),
            new IComponentCommand<LeverComponent>() {
              @Override
              public void execute(LeverComponent comp) {
                System.out.println("EXCUTE");
                if (a1 == null || !EventScheduler.isScheduled(a1)) {
                  System.out.println("SCHEDULE");
                  a1 =
                      EventScheduler.scheduleAction(
                          () -> {
                            System.out.println("ACTION");
                            if (comp.isOn()) {
                              System.out.println("TOGGLE");
                              comp.toggle();
                              Entity lever =
                                  Game.entityAtTile(Game.tileAT(customPoints.get(3)))
                                      .filter(entity -> entity.isPresent(LeverComponent.class))
                                      .findFirst()
                                      .get();
                              lever
                                  .fetch(DrawComponent.class)
                                  .ifPresent(
                                      drawComponent ->
                                          drawComponent.currentAnimation(
                                              comp.isOn() ? "on" : "off"));
                            }
                          },
                          DELAY_MILLIS);
                }
              }

              @Override
              public void undo(LeverComponent comp) {
                System.out.println("REMOVE");
                EventScheduler.cancelAction(a1);
              }
            });
    Game.add(l);
    l4 = l.fetch(LeverComponent.class).get();
  }

  @Override
  protected void onTick() {
    if (l1.isOn() || l2.isOn()) {
      door.open();
    } else door.close();

    if (l3.isOn() && l4.isOn()) {
      exit.open();
    }
  }
}
