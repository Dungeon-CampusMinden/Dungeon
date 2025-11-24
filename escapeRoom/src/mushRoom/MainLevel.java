package mushRoom;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.entities.LeverFactory;
import contrib.entities.NPCFactory;
import contrib.entities.WorldItemBuilder;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.item.concreteItem.ItemHammer;
import contrib.modules.levelHide.LevelHideFactory;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.DrawSystem;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.shader.ColorGradeShader;
import core.utils.components.draw.shader.HueRemapShader;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import mushRoom.modules.items.AxeItem;
import mushRoom.modules.items.LanternItem;
import mushRoom.modules.journal.JournalItem;
import mushRoom.modules.journal.JournalPageFactory;
import mushRoom.modules.mushrooms.MushroomFactory;
import mushRoom.modules.mushrooms.MushroomItem;
import mushRoom.modules.mushrooms.Mushrooms;
import mushRoom.shaders.MushroomPostProcessing;

/** The MushRoom. */
public class MainLevel extends DungeonLevel {

  private static final int TO_GENERATE_PER_TYPE = 4;

  private NpcState npcState = NpcState.FIRST_TALK;

  private int maxMushrooms;
  private Entity npc;

  private DoorTile puzzlePushDoor;
  private DoorTile puzzlePushExit;

  private DoorTile buttonsDoor;
  private DoorTile buttonsExit;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public MainLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {
    DrawSystem ds = (DrawSystem) Game.systems().get(DrawSystem.class);
    ds.levelShaders()
        .add("yellow", new ColorGradeShader(0.2f, 1, 1).region(new Rectangle(getPoint("yellow-start"), getPoint("yellow-end"))).transitionSize(5));
    ds.levelShaders()
        .add(
            "orange",
            new ColorGradeShader(0.1f, 1, 1).region(new Rectangle(getPoint("orange-start"), getPoint("orange-end"))).transitionSize(5));
    ds.levelShaders()
        .add(
            "green",
            new ColorGradeShader(0.3f, 1, 1).region(new Rectangle(getPoint("green-start"), getPoint("green-end"))).transitionSize(5));
    ds.levelShaders()
        .add(
            "gray",
            new ColorGradeShader(0.5f, 0.1f, 0.6f)
                .region(new Rectangle(getPoint("gray-start"), getPoint("gray-end"))).transitionSize(5));

    Point homeStart = getPoint("home-start");
    Point homeEnd = getPoint("home-end");
    Rectangle homeRect = new Rectangle(homeEnd.x() - homeStart.x() + 1, homeEnd.y() - homeStart.y() + 1, homeStart.x(), homeStart.y());
    homeRect = homeRect.expand(-1);
    ds.sceneShaders().add("pp", new MushroomPostProcessing(homeRect).viewDistance(0.2f));

    Game.add(LevelHideFactory.createLevelHide(getPoint("cave-1-start"), getPoint("cave-1-end")));
    Game.add(LevelHideFactory.createLevelHide(getPoint("hidden-1-start"), getPoint("hidden-1-end"), 1));

    generateMushrooms();
    listPoints("page").forEach(p -> {
      Game.add(JournalPageFactory.createJournalPage(p));
    });

    createPushPuzzle();
    createButtonsPuzzle();
    createCutTrees();

    listPoints("stone").forEach(p -> {
      try {
        Game.add(EntityFactory.newStone(p, 0));
      } catch (IOException e) {}
    });

    Game.add(WorldItemBuilder.buildWorldItem(new ItemHammer(), getPoint("hammer")));
    Game.add(WorldItemBuilder.buildWorldItem(new LanternItem(), getPoint("lantern")));

    npc = NPCFactory.createNPC(getPoint("npc-start"), "character/char03");
    npc.add(
        new InteractionComponent(
            1.5f,
            true,
            (e, who) -> {
              talkToNpc();
            }));
    Game.add(npc);
  }

  private void createButtonsPuzzle() {
    Game.add(LevelHideFactory.createLevelHide(getPoint("buttons-hide-start"), getPoint("buttons-hide-end"), 1));
    buttonsDoor = (DoorTile) tileAt(getPoint("buttons-door")).orElseThrow();
    buttonsDoor.close();
    buttonsExit = (DoorTile) tileAt(getPoint("buttons-exit")).orElseThrow();
    buttonsExit.close();
    Game.add(LeverFactory.createLever(getPoint("buttons-exit-lever"), new ICommand() {
      public void execute() {
        buttonsExit.open();
      }
      public void undo() {
        buttonsExit.close();
      }
    }));
    listPoints("buttons-plate").forEach(p -> {
      Game.add(LeverFactory.pressurePlate(p));
    });

    DrawSystem ds = (DrawSystem) Game.systems().get(DrawSystem.class);
    ds.levelShaders()
      .add("buttons-yellow", new ColorGradeShader(0.2f, 1, 1).region(new Rectangle(getPoint("buttons-yellow-start"), getPoint("buttons-yellow-end"))).transitionSize(1), 2);
    ds.levelShaders()
      .add(
        "buttons-orange",
        new ColorGradeShader(0.1f, 1, 1).region(new Rectangle(getPoint("buttons-orange-start"), getPoint("buttons-orange-end"))).transitionSize(1), 2);
    ds.levelShaders()
      .add(
        "buttons-green",
        new ColorGradeShader(0.3f, 1, 1).region(new Rectangle(getPoint("buttons-green-start"), getPoint("buttons-green-end"))).transitionSize(1), 2);
    ds.levelShaders()
      .add(
        "buttons-gray",
        new ColorGradeShader(0.5f, 0.1f, 0.6f)
          .region(new Rectangle(getPoint("buttons-gray-start"), getPoint("buttons-gray-end"))).transitionSize(1), 2);
  }

  private void createCutTrees() {
    Deco deco = Deco.TreeMedium;
    Vector2 offset = Vector2.of(-deco.defaultCollider().x(), -deco.defaultCollider().y());
    listPoints("cut-tree").forEach(p -> {
      Entity tree = DecoFactory.createDeco(p, deco);
      tree.remove(DecoComponent.class);
      tree.add(new InteractionComponent(1.5f, true, (e, who) -> {
        who.fetch(InventoryComponent.class).ifPresent(inv -> {
          if(inv.hasItem(AxeItem.class)){
            DialogUtils.showTextPopup("Rumms. Der Baum fällt mit einem lauten Krachen zu Boden.", "Holz hacken");
            Game.remove(e);
          } else {
            DialogUtils.showTextPopup("Dieser Baum sieht etwas morsch aus, man kann ihn bestimmt fällen.", "Hmm");
          }
        });
      }));
      tree.fetch(DrawComponent.class).ifPresent(dc -> {
        dc.shaders().add("cuttable", new HueRemapShader(0.33f, 0.1f, 0.1f));
        dc.shaders().add("cuttable-color", new ColorGradeShader(-1, 0.5f, 0.5f));
      });
      tree.fetch(PositionComponent.class).ifPresent(pc -> {
        pc.position(pc.position().translate(offset));
      });
      Game.add(tree);
    });

    Game.add(WorldItemBuilder.buildWorldItem(new AxeItem(), getPoint("axe")));
  }

  private void createPushPuzzle() {
    puzzlePushDoor = (DoorTile) tileAt(getPoint("puzzle-push-door")).orElseThrow();
//    puzzlePushDoor.close();
    puzzlePushExit = (DoorTile) tileAt(getPoint("puzzle-push-exit")).orElseThrow();
    puzzlePushExit.close();

    Game.add(LeverFactory.createLever(getPoint("puzzle-push-exit-lever"), new ICommand() {
      public void execute() {
        puzzlePushExit.open();
      }
      public void undo() {
        puzzlePushExit.close();
      }
    }));

    Game.add(LevelHideFactory.createLevelHide(getPoint("push-hidden-start"), getPoint("push-hidden-end")));
  }

  @Override
  protected void onTick() {
    DrawSystem ds = DrawSystem.getInstance();

    // Check if lantern item is available, to set the viewDistance of the mushroom shader
    boolean hasLantern = Game.player().flatMap(p -> p.fetch(InventoryComponent.class))
        .map(inv -> inv.hasItem(LanternItem.class))
        .orElse(false);
    boolean inLevelEditor = LevelEditorSystem.active();
    float viewDistance = inLevelEditor ? 1.0f : (hasLantern ? 0.5f : 0.2f);
    if(ds.sceneShaders().get("pp") instanceof MushroomPostProcessing mpp){
      mpp.viewDistance(viewDistance);
    }
  }

  private void generateMushrooms() {
    List<Point> allPoints = listPoints("mushroom");
    Collections.shuffle(allPoints);

    int maxMushroomCount = Mushrooms.values().length * TO_GENERATE_PER_TYPE;
    if (allPoints.size() > maxMushroomCount) {
      allPoints = allPoints.subList(0, maxMushroomCount);
    }

    List<Point> finalAllPoints = allPoints;
    IntStream.range(0, allPoints.size())
        .forEach(
            index -> {
              Point p = finalAllPoints.get(index);
              if (index >= Mushrooms.values().length * TO_GENERATE_PER_TYPE) {
                return;
              }

              Mushrooms type = Mushrooms.values()[index % Mushrooms.values().length];
              if (!type.poisonous) {
                maxMushrooms++;
              }
              Game.add(
                  MushroomFactory.createMushroom(
                      p, Mushrooms.values()[index % Mushrooms.values().length], type.poisonous));
            });
  }

  private Tuple<Integer, Integer> getCollectionState() {
    return Game.player()
        .flatMap(p -> p.fetch(InventoryComponent.class))
        .map(
            inventory -> {
              long collected =
                  Arrays.stream(inventory.items())
                      .filter(item -> item instanceof MushroomItem.BaseMushroom)
                      .reduce(0, (a, b) -> a + b.stackSize(), Integer::sum);
              long collectedPoisonous =
                  Arrays.stream(inventory.items())
                      .filter(
                          item -> {
                            if (!(item instanceof MushroomItem.BaseMushroom bm)) {
                              return false;
                            }
                            return bm.type().poisonous;
                          })
                      .reduce(0, (a, b) -> a + b.stackSize(), Integer::sum);
              if (collected - collectedPoisonous >= maxMushrooms
                  && npcState == NpcState.DURING_COLLECTING) {
                npcState = NpcState.ALL_COLLECTED;
              }
              return Tuple.of((int) collected, (int) collectedPoisonous);
            })
        .orElse(Tuple.of(0, 0));
  }

  private void talkToNpc() {
    Tuple<Integer, Integer> collectionState = getCollectionState();
    if (collectionState.a() >= maxMushrooms && npcState == NpcState.DURING_COLLECTING) {
      npcState = NpcState.ALL_COLLECTED;
      if (collectionState.b() > 0) {
        npcState = NpcState.ALL_COLLECTED_POISONOUS;
        npcDie();
      }
    }
    DebugDrawSystem.setEntityQuickInfo(
        npc,
        "Collection State:\n - "
            + collectionState.a()
            + " / "
            + maxMushrooms
            + " mushrooms\n - "
            + collectionState.b()
            + " poisonous");

    switch (npcState) {
      case FIRST_TALK:
        DialogUtils.showTextPopup(
            "Oh man, der Sturz vorhin hat mich doch etwas mehr mitgenommen, als ich gedacht hätte. Ich glaube nicht, dass ich weiterlaufen kann. Zum Glück gibt es in diesem Wald einige Pilze, mit denen ich einen Heiltrank brauen kann.",
            "Yoooohooo");
        npcState = NpcState.SECOND_TALK;
        break;
      case SECOND_TALK:
        DialogUtils.showTextPopup(
            "Sammle von jeder Sorte "
                + TO_GENERATE_PER_TYPE
                + " Stück. Nicht alle Pilze sind ungefährlich, einige sind giftig! Nimm dieses Notizbuch, um die Pilze zu identifizieren. Öffne es mit <B>",
            "Yoooohooo");
        npcState = NpcState.THIRD_TALK;
        giveJournal();
        break;
      case THIRD_TALK:
        DialogUtils.showTextPopup(
            "Einige Seiten in dem Notizbuch fehlen, aber vielleicht findest du in der Umgebung mehr Informationen über die Pilze. Bitte beeile dich, sonst klappe ich hier noch zusammen!",
            "Yoooohooo");
        npcState = NpcState.DURING_COLLECTING;
        break;
      case DURING_COLLECTING:
        DialogUtils.showTextPopup(
            "Hast du schon alle Pilze gesammelt? Ich brauche sie dringend, um den Heiltrank zu brauen! "
                + TO_GENERATE_PER_TYPE
                + " Stück von jeder nicht giftigen Sorte sollten reichen."
                + " Du hast gerade schon "
                + collectionState.a()
                + " Pilze gesammelt, insgesamt brauche ich "
                + maxMushrooms
                + " Stück!",
            "Yoooohooo");
        break;
      case ALL_COLLECTED:
        DialogUtils.showTextPopup(
            "Danke, dass du die Pilze gesammelt hast! Mit diesem Heiltrank fühle ich mich schon viel besser. Du bist ein wahrer Freund!",
            "Yoooohooo");
        break;
      case ALL_COLLECTED_POISONOUS:
        DialogUtils.showTextPopup(
            "Oh nein! Du hast einige giftige Pilze mitgebracht! Jetzt muss ich wohl draufgehen...",
            "Yoooohooo");
        npcState = NpcState.DEAD;
        break;
      case DEAD:
        DialogUtils.showTextPopup("......", "...");
        break;
    }
  }

  private void giveJournal(){
    Game.player().flatMap(e -> e.fetch(InventoryComponent.class)).ifPresent(inventory -> {
      inventory.add(new JournalItem());
    });
  }

  private void npcDie() {
    npc.fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.shaders().add("poisoned", new HueRemapShader(0.05f, 0.333f, 0.05f));
              dc.currentAnimation().getConfig().framesPerSprite(999999);
              DrawSystem.getInstance().changeEntityDepth(npc, DepthLayer.BackgroundDeco.depth());
            });
    npc.fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              pc.rotation(90);
              pc.position(pc.position().translate(-0.5f, -0.5f));
            });
    npc.fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.isSolid(false);
            });
  }

  private enum NpcState {
    FIRST_TALK,
    SECOND_TALK,
    THIRD_TALK,
    DURING_COLLECTING,
    ALL_COLLECTED,
    ALL_COLLECTED_POISONOUS,
    DEAD,
  }
}
