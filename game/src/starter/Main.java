package starter;

import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.item.concreteItem.ItemResourceIronOre;
import contrib.item.concreteItem.ItemResourceWood;
import contrib.level.generator.graphBased.RoombasedLevelGenerator;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // toggle this to off, if you want to use the default level generator
        boolean useRoomBasedLevel = true;

        Game.initBaseLogger();
        Debugger debugger = new Debugger();
        // start the game
        configGame();

        if (useRoomBasedLevel) onSetupRoomBasedLevel(10, 5, 1);
        else {
            onSetup();
            onLevelLoad(5, 1);
        }

        onFrame(debugger);

        // build and start game
        Game.run();
    }

    private static void onLevelLoad(int monstercount, int chestcount) {
        Game.userOnLevelLoad(
                (firstTime) -> {
                    if (firstTime) {
                        try {
                            for (int i = 0; i < monstercount; i++)
                                Game.add(EntityFactory.randomMonster());
                            for (int i = 0; i < chestcount; i++) Game.add(EntityFactory.newChest());
                            Game.add(EntityFactory.newCraftingCauldron());
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                        Game.levelSize(LevelSize.randomSize());
                        ItemResourceIronOre ironOre = new ItemResourceIronOre();
                        ItemResourceWood wood = new ItemResourceWood();
                        Game.hero()
                                .flatMap(entity -> entity.fetch(InventoryComponent.class))
                                .ifPresent(
                                        inventoryComponent -> {
                                            inventoryComponent.add(ironOre);
                                            inventoryComponent.add(wood);
                                        });
                    }
                });
    }

    private static void onFrame(Debugger debugger) {
        Game.userOnFrame(debugger::execute);
    }

    private static void onSetup() {
        Game.userOnSetup(
                () -> {
                    createSystems();
                    createHero();
                    Crafting.loadRecipes();
                });
    }

    private static void onSetupRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
        Game.userOnSetup(
                () -> {
                    createSystems();
                    createHero();
                    Crafting.loadRecipes();
                    createRoomBasedLevel(roomcount, monstercount, chestcount);
                });
    }

    private static void configGame() throws IOException {
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(true);
        Game.windowTitle("My Dungeon");
    }

    private static void createRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
        // create entity sets
        Set<Set<Entity>> entities = new HashSet<>();
        for (int i = 0; i < roomcount; i++) {
            Set<Entity> set = new HashSet<>();
            entities.add(set);
            if (i == roomcount / 2) {
                try {
                    set.add(EntityFactory.newCraftingCauldron());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int j = 0; j < monstercount; j++) {
                try {
                    set.add(EntityFactory.randomMonster());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int k = 0; k < chestcount; k++) {
                try {
                    set.add(EntityFactory.newChest());
                } catch (IOException e) {

                }
            }
        }
        ILevel level = RoombasedLevelGenerator.level(entities, DesignLabel.randomDesign());
        Game.currentLevel(level);
    }

    private static void createHero() {
        Entity hero = null;
        try {
            hero = (EntityFactory.newHero());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Game.add(hero);
        Game.hero(hero);
    }

    private static void createSystems() {
        Game.add(new CollisionSystem());
        Game.add(new AISystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());
        Game.add(new HudSystem());
        Game.add(new SpikeSystem());
    }
}
