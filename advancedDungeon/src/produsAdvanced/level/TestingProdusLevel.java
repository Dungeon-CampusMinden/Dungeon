package produsAdvanced.level;

import components.AntiMaterialBarrierComponent;
import components.LasergridComponent;
import contrib.components.LeverComponent;
import contrib.entities.*;
import contrib.item.Item;
import contrib.item.concreteItem.*;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.AdvancedFactory;
import entities.TractorBeamFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import level.AdvancedLevel;
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;

/** This level can be used for testing new implementations of items, skills, monsters and more. */
public class TestingProdusLevel extends AdvancedLevel {
  private DoorTile door1;
  private DoorTile plate_door;
  private LeverComponent plate1;
  private LeverComponent beamSwitchLever1;
  private LeverComponent laserLever;
  private LeverComponent barrierLever;
  private LeverComponent portalBeamLever;
  private LeverComponent portalSwitchLever;
  private LeverComponent portalRemoveLever;
  private List<Entity> tractorBeamEntities;
  private List<Entity> infiniBeamEntities1;
  private List<Entity> infbeam2;
  private List<Entity> infbeam3;
  private List<Entity> infbeam4;
  private List<Entity> portalBeam;
  private Entity lasergrid;
  private Entity antibarrier;
  private Entity beam;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public TestingProdusLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "TestingProdusLevel");
  }

  @Override
  protected void onFirstTick() {
    door1 = (DoorTile) Game.tileAt(new Coordinate(13, 8)).orElse(null);
    // door1.close();
    Item bow = new ItemWoodenBow();
    Item arrow1 = new ItemWoodenArrow(10);
    Item arrow2 = new ItemWoodenArrow(10);
    Item hammer = new ItemHammer();
    Point bowSpawnPoint = new Point(5, 5);
    Point arrowSpawnPoint1 = new Point(7, 5);
    Point arrowSpawnPoint2 = new Point(9, 5);
    Point hammerSpawnPoint = new Point(5, 7);
    bow.drop(bowSpawnPoint);
    arrow1.drop(arrowSpawnPoint1);
    arrow2.drop(arrowSpawnPoint2);
    hammer.drop(hammerSpawnPoint);

    // Game.add(MiscFactory.newHeartPickup(new Point(11, 5), 5));
    // Game.add(MiscFactory.newFairyPickup(new Point(13, 5)));
    Item fairyOnTheGround = new ItemFairy();
    Item heartOnTheGround = new ItemHeart(5);
    fairyOnTheGround.drop(new Point(13, 5));
    heartOnTheGround.drop(new Point(11, 5));

    Point keySpawnPoint = new Point(5, 3);
    Point bigKeySpawnPoint = new Point(7, 3);
    Item smallKey = new ItemKey();
    Item bigKey = new ItemBigKey();
    smallKey.drop(keySpawnPoint);
    bigKey.drop(bigKeySpawnPoint);

    Set<Item> items = new HashSet<>();
    Item stuff = new ItemPotionHealth();
    Item stuff2 = new ItemResourceEgg();
    items.add(stuff);
    items.add(stuff2);

    Set<Item> stoneItems = new HashSet<>();
    Item keyInStone = new ItemKey();
    Item fairy = new ItemFairy();
    Item heartInStone = new ItemHeart(5);
    Item bigKeyInStone = new ItemBigKey();
    Item potionInStone = new ItemPotionHealth();
    Item eggInStone = new ItemResourceEgg();
    // stoneItems.add(fairy);
    stoneItems.add(eggInStone);
    stoneItems.add(keyInStone);
    // stoneItems.add(heartInStone);
    stoneItems.add(bigKeyInStone);
    stoneItems.add(potionInStone);

    Set<Item> vaseItems = new HashSet<>();
    Item heart = new ItemHeart(5);
    vaseItems.add(heart);

    plate_door = (DoorTile) Game.tileAt(new Coordinate(3, 10)).orElse(null);
    plate_door.close();
    Entity plateEntity = LeverFactory.pressurePlate(new Point(3, 9));
    plate1 =
        plateEntity
            .fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(plateEntity, LeverComponent.class));
    Game.add(plateEntity);

    Entity beamSwitchLever = LeverFactory.createLever(new Point(34, 6));
    beamSwitchLever1 =
        beamSwitchLever
            .fetch(LeverComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(beamSwitchLever, LeverComponent.class));
    Game.add(beamSwitchLever);

    Entity laserLeverEntity = LeverFactory.createLever(new Point(1, 6));
    laserLever =
        laserLeverEntity
            .fetch(LeverComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(laserLeverEntity, LeverComponent.class));
    Game.add(laserLeverEntity);

    Entity barrierLeverEntity = LeverFactory.createLever(new Point(1, 3));
    barrierLever =
        barrierLeverEntity
            .fetch(LeverComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(barrierLeverEntity, LeverComponent.class));
    Game.add(barrierLeverEntity);

    Entity portalBeamLeverEntity = LeverFactory.createLever(new Point(10, 13));
    portalBeamLever =
        portalBeamLeverEntity
            .fetch(LeverComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(portalBeamLeverEntity, LeverComponent.class));
    Game.add(portalBeamLeverEntity);

    Entity portalSwitchLeverEntity = LeverFactory.createLever(new Point(8, 13));
    portalSwitchLever =
        portalSwitchLeverEntity
            .fetch(LeverComponent.class)
            .orElseThrow(
                () ->
                    MissingComponentException.build(portalSwitchLeverEntity, LeverComponent.class));
    Game.add(portalSwitchLeverEntity);

    Entity portalRemoveLeverEntity = LeverFactory.createLever(new Point(6, 13));
    portalRemoveLever =
        portalRemoveLeverEntity
            .fetch(LeverComponent.class)
            .orElseThrow(
                () ->
                    MissingComponentException.build(portalRemoveLeverEntity, LeverComponent.class));
    Game.add(portalRemoveLeverEntity);

    try {
      // Entity monster = DungeonMonster.GOBLIN.builder().build(new Point(1, 1));
      // monster.add(new SpikyComponent(10, DamageType.PHYSICAL, 10));
      // monster.add(new PositionComponent());
      // Game.add(monster);
      // Game.add(MonsterFactory.randomMonster());
      Game.add(MiscFactory.newLockedChest(items, new Point(9, 3), ItemKey.class));
      Game.add(MiscFactory.createDoorBlocker(door1, ItemBigKey.class));
      Game.add(EntityFactory.newStone(new Point(7, 7), stoneItems));
      Game.add(EntityFactory.newVase(new Point(9, 7), vaseItems));
      Game.add(EntityFactory.newStone(new Point(7, 9)));
      Game.add(EntityFactory.newVase(new Point(9, 9)));
      Game.add(EntityFactory.newStone(new Point(11, 9), 0.5f));
      Game.add(EntityFactory.newVase(new Point(11, 7), 0.5f));

      lasergrid = AdvancedFactory.laserGrid(new Point(3, 6), true);
      Game.add(lasergrid);

      antibarrier = AdvancedFactory.antiMaterialBarrier(new Point(3, 3), true);
      Game.add(antibarrier);

      Game.add(AdvancedFactory.attachablePortalCube(new Point(5, 9)));
      /*
      tractorBeamEntities =
          TractorBeamFactory.createFullTractorBeam(new Point(1, 1), new Point(5, 1));
      for (Entity beamEntity : tractorBeamEntities) {
        System.out.println(beamEntity);
        Game.add(beamEntity);
      }
       */
      /*
      infiniBeamEntities1 = TractorBeamFactory.createTractorBeam(new Point(29, 2), Direction.RIGHT);
      // infiniBeamEntities1 =
      // TractorBeamFactory.createTractorBeam(new Point(29, 2), new Point(35, 2));
      for (Entity beamEntity : infiniBeamEntities1) {
        // System.out.println(beamEntity);
        Game.add(beamEntity);
      }

      infbeam2 = TractorBeamFactory.createTractorBeam(new Point(38, 1), Direction.UP);
      for (Entity beamEntity2 : infbeam2) {
        // System.out.println(beamEntity2);
        Game.add(beamEntity2);
      }

      infbeam3 = TractorBeamFactory.createTractorBeam(new Point(39, 10), Direction.LEFT);
      for (Entity beamEntity3 : infbeam3) {
        // System.out.println(beamEntity3);
        Game.add(beamEntity3);
      }

      infbeam4 = TractorBeamFactory.createTractorBeam(new Point(30, 11), Direction.DOWN);
      for (Entity beamEntity4 : infbeam4) {
        // System.out.println(beamEntity4);
        Game.add(beamEntity4);
      }

      portalBeam = TractorBeamFactory.createTractorBeam(new Point(11, 13), Direction.UP);
      for (Entity portalBeamEntity : portalBeam) {
        // System.out.println(portalBeamEntity);
        Game.add(portalBeamEntity);
      }

       */

      beam = TractorBeamFactory.createTractorBeam(new Point(11, 13), Direction.UP);

      // ---------------------------------------------------------------------------------------
      // ---------------------------------------------------------------------------------------
      /*
      Game.add(
          EntityFactory.newMovingArrowSentry(
              new Point(16.0f, 8.0f), new Point(16.0f, 2.0f), Direction.RIGHT, 500, 5));

      Game.add(
          EntityFactory.newMovingArrowWallSentry(
              new Point(17.0f, 0.0f), new Point(22.0f, 0.0f), Direction.UP, 500, 5));

      Game.add(
          EntityFactory.newStationaryArrowWallSentry(
              new Point(22.0f, 11.0f), Direction.DOWN, 500, 5));

      Game.add(
          EntityFactory.newStationaryArrowSentry(new Point(25.0f, 10.0f), Direction.DOWN, 500, 5));


       */
      // ---------------------------------------------------------------------------------------
      /*
      Game.add(
          EntityFactory.newMovingFireballSentry(
              new Point(16.0f, 8.0f), new Point(16.0f, 2.0f), Direction.RIGHT, 500, 5));

      Game.add(
          EntityFactory.newMovingFireballWallSentry(
              new Point(17.0f, 0.0f), new Point(22.0f, 0.0f), Direction.UP, 500, 5));

      Game.add(
          EntityFactory.newStationaryFireballWallSentry(
              new Point(22.0f, 11.0f), Direction.DOWN, 500, 5));

      Game.add(
          EntityFactory.newStationaryFireballSentry(
              new Point(25.0f, 10.0f), Direction.DOWN, 500, 5));
      */

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  protected void onTick() {
    if (plate1.isOn()) {
      plate_door.open();
    } else plate_door.close();

    if (beamSwitchLever1.isOn()) {
      System.out.println("lever ON!");
      beamSwitchLever1.toggle();

      TractorBeamFactory.reverseTractorBeam(infiniBeamEntities1);
      TractorBeamFactory.reverseTractorBeam(infbeam2);
      TractorBeamFactory.reverseTractorBeam(infbeam3);
      TractorBeamFactory.reverseTractorBeam(infbeam4);
    }

    // #####################################################################
    if (portalBeamLever.isOn()) {
      System.out.println("lever ON!");
      portalBeamLever.toggle();

      // TractorBeamFactory.extendTractorBeam(Direction.RIGHT, new Point(1f, 14f), portalBeam);
    }

    if (portalSwitchLever.isOn()) {
      System.out.println("lever ON!");
      portalSwitchLever.toggle();
      System.out.println(portalBeam);
      beam.fetch(TractorBeamComponent.class)
          .ifPresent(
              tbc -> {
                TractorBeamFactory.reverseTractorBeam(tbc.tractorBeamEntities);
              });
      // TractorBeamFactory.reverseTractorBeam(portalBeam);
    }

    if (portalRemoveLever.isOn()) {
      System.out.println("lever ON!");
      portalRemoveLever.toggle();
      System.out.println("VORHER" + portalBeam);
      TractorBeamFactory.trimAfterFirstBeamEmitter(portalBeam);
      System.out.println("NACHHER" + portalBeam);
    }

    // #######################################################################

    LasergridComponent lgc = lasergrid.fetch(LasergridComponent.class).orElse(null);

    if (laserLever.isOn()) {
      lgc.deactivate();
    } else {
      lgc.activate();
    }

    AntiMaterialBarrierComponent ambc =
        antibarrier.fetch(AntiMaterialBarrierComponent.class).orElse(null);

    if (barrierLever.isOn()) {
      ambc.deactivate();
    } else {
      ambc.activate();
    }
  }
}
