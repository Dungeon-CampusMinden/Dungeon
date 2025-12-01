package level.portal;

import components.AntiMaterialBarrierComponent;
import components.LasergridComponent;
import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.utils.EntityUtils;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.AdvancedFactory;
import entities.LightBridgeFactory;
import entities.LightWallFactory;
import entities.TractorBeamFactory;
import java.util.Map;
import level.AdvancedLevel;
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;

/** demo level. */
public class PortalDemoLevel extends AdvancedLevel {

  private Entity cube1,
      sphere1,
      launcher1,
      catcher1,
      tractorBeam1,
      lightBridge1,
      lightWall1,
      tractorBeam2,
      lightBridge2,
      lightWall2,
      lightWall3,
      laserGrid1,
      pressurePlate1,
      pressurePlate2;
  private LeverComponent switch1,
      switch2,
      switch3,
      switch4,
      switch5,
      switch6,
      switch7,
      switch8,
      switch9,
      plate1,
      plate2;

  ICommand switch0Action =
      new ICommand() {
        @Override
        public void execute() {
          TractorBeamFactory.reverseTractorBeam(
              tractorBeam1.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
        }

        @Override
        public void undo() {
          TractorBeamFactory.reverseTractorBeam(
              tractorBeam1.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
        }
      };

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalDemoLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  @Override
  protected void onFirstTick() {
    pressurePlate1 = AdvancedFactory.cubePressurePlate(namedPoints.get("CubePlate1"), 1);
    plate1 = pressurePlate1.fetch(LeverComponent.class).get();
    pressurePlate2 = AdvancedFactory.spherePressurePlate(namedPoints.get("spherePlate1"), 1);
    plate2 = pressurePlate2.fetch(LeverComponent.class).get();
    cube1 = AdvancedFactory.attachablePortalCube(namedPoints.get("Würfel1"));
    sphere1 = AdvancedFactory.moveableSphere(namedPoints.get("Kugel1"));
    launcher1 =
        AdvancedFactory.energyPelletLauncher(
            namedPoints.get("Energie"), Direction.RIGHT, 10000000, 10000);
    catcher1 = AdvancedFactory.energyPelletCatcher(namedPoints.get("Energie2"), Direction.RIGHT);
    tractorBeam1 =
        TractorBeamFactory.createTractorBeam(namedPoints.get("Traktor1"), Direction.DOWN);
    lightBridge1 =
        LightBridgeFactory.createEmitter(namedPoints.get("Brücke1"), Direction.DOWN, false);
    lightWall1 = LightWallFactory.createEmitter(namedPoints.get("Wand1"), Direction.DOWN, false);
    tractorBeam2 =
        TractorBeamFactory.createTractorBeam(namedPoints.get("Traktor2"), Direction.DOWN);
    lightBridge2 =
        LightBridgeFactory.createEmitter(namedPoints.get("Brücke2"), Direction.DOWN, false);
    lightWall2 = LightWallFactory.createEmitter(namedPoints.get("Wand2"), Direction.DOWN, false);
    lightWall3 = LightWallFactory.createEmitter(namedPoints.get("Wand3"), Direction.LEFT, false);
    Game.add(AdvancedFactory.laserGrid(namedPoints.get("Grid1"), true));
    Game.add(AdvancedFactory.laserGrid(namedPoints.get("Grid2"), true));
    Game.add(AdvancedFactory.laserGrid(namedPoints.get("Grid3"), true));
    Game.add(AdvancedFactory.laserGrid(namedPoints.get("Grid4"), true));
    Game.add(AdvancedFactory.laserGrid(namedPoints.get("Grid5"), true));
    Game.add(AdvancedFactory.antiMaterialBarrier(namedPoints.get("Anti1"), true));
    Game.add(AdvancedFactory.antiMaterialBarrier(namedPoints.get("Anti2"), true));
    Game.add(AdvancedFactory.antiMaterialBarrier(namedPoints.get("Anti3"), true));
    Game.add(AdvancedFactory.antiMaterialBarrier(namedPoints.get("Anti4"), true));
    Game.add(AdvancedFactory.antiMaterialBarrier(namedPoints.get("Anti5"), true));

    EntityUtils.spawnLever(namedPoints.get("Lever0"), switch0Action);
    Entity s1 = LeverFactory.createLever(namedPoints.get("Lever1"));
    switch1 = s1.fetch(LeverComponent.class).get();
    Entity s2 = LeverFactory.createLever(namedPoints.get("Lever2"));
    switch2 = s2.fetch(LeverComponent.class).get();
    Entity s3 = LeverFactory.createLever(namedPoints.get("Lever3"));
    switch3 = s3.fetch(LeverComponent.class).get();
    Entity s4 = LeverFactory.createLever(namedPoints.get("Lever4"));
    switch4 = s4.fetch(LeverComponent.class).get();
    Entity s5 = LeverFactory.createLever(namedPoints.get("Lever5"));
    switch5 = s5.fetch(LeverComponent.class).get();
    Entity s6 = LeverFactory.createLever(namedPoints.get("Lever6"));
    switch6 = s6.fetch(LeverComponent.class).get();
    Entity s7 = LeverFactory.createLever(namedPoints.get("Lever7"));
    switch7 = s7.fetch(LeverComponent.class).get();
    Entity s8 = LeverFactory.createLever(namedPoints.get("Lever8"));
    switch8 = s8.fetch(LeverComponent.class).get();
    Entity s9 = LeverFactory.createLever(namedPoints.get("Lever9"));
    switch9 = s9.fetch(LeverComponent.class).get();
    Game.add(pressurePlate1);
    Game.add(pressurePlate2);
    Game.add(launcher1);
    Game.add(catcher1);
    Game.add(sphere1);
    Game.add(cube1);
    Game.add(lightBridge1);
    Game.add(lightBridge2);
    Game.add(lightWall1);
    Game.add(lightWall2);
    Game.add(lightWall3);
    Game.add(s1);
    Game.add(s2);
    Game.add(s3);
    Game.add(s4);
    Game.add(s5);
    Game.add(s6);
    Game.add(s7);
    Game.add(s8);
    Game.add(s9);
  }

  @Override
  protected void onTick() {
    if (plate1.isOn()) tractorBeam1.fetch(TractorBeamComponent.class).get().activate();
    else tractorBeam1.fetch(TractorBeamComponent.class).get().deactivate();
    if (plate2.isOn()) LightBridgeFactory.activate(lightBridge1);
    else LightBridgeFactory.deactivate(lightBridge1);
    if (switch3.isOn()) LightWallFactory.activate(lightWall1);
    else LightWallFactory.deactivate(lightWall1);
    if (switch4.isOn()) tractorBeam2.fetch(TractorBeamComponent.class).get().activate();
    else tractorBeam2.fetch(TractorBeamComponent.class).get().deactivate();
    if (switch5.isOn()) LightWallFactory.activate(lightWall2);
    else LightWallFactory.deactivate(lightWall2);
    if (switch6.isOn()) LightBridgeFactory.activate(lightBridge2);
    else LightBridgeFactory.deactivate(lightBridge2);
    if (switch7.isOn()) LightWallFactory.activate(lightWall3);
    else LightWallFactory.deactivate(lightWall3);
    if (switch8.isOn()) {
      Game.allEntities()
          .forEach(
              e ->
                  e.fetch(AntiMaterialBarrierComponent.class)
                      .ifPresent(AntiMaterialBarrierComponent::activate));
    } else {
      Game.allEntities()
          .forEach(
              e ->
                  e.fetch(AntiMaterialBarrierComponent.class)
                      .ifPresent(AntiMaterialBarrierComponent::deactivate));
    }
    if (switch9.isOn()) {
      Game.allEntities()
          .forEach(e -> e.fetch(LasergridComponent.class).ifPresent(LasergridComponent::activate));
    } else {
      Game.allEntities()
          .forEach(
              e -> e.fetch(LasergridComponent.class).ifPresent(LasergridComponent::deactivate));
    }
    /*if (catcher1.fetch(ToggleableComponent.class).get().isActive()) {
      launcher1.remove(AIComponent.class);
    }*/
  }
}
