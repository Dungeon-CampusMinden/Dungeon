package starter;

import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.level.generator.graphBased.RoombasedLevelGenerator;
import core.level.utils.DesignLabel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Logger LOGGER = Logger.getLogger("Main");
        Debugger debugger = new Debugger();
        // start the game
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(true);
        Game.userOnSetup(
                () -> {
                    try {
                        Entity hero = (EntityFactory.newHero());
                        Game.add(hero);
                        Game.hero(hero);
                        Crafting.loadRecipes();
                        // create entity sets
                        Set<Set<Entity>> entities = new HashSet<>();
                        int roomCount = 10;
                        for (int i = 0; i < roomCount; i++) {
                            Set<Entity> set = new HashSet<>();
                            entities.add(set);
                            if (i == roomCount / 2) set.add(EntityFactory.newCraftingCauldron());
                            int monsterCount = 3;
                            int chestCount = 1;
                            for (int j = 0; j < monsterCount; j++)
                                set.add(EntityFactory.randomMonster());
                            for (int k = 0; k < chestCount; k++) set.add(EntityFactory.newChest());
                        }
                        ILevel level =
                                RoombasedLevelGenerator.level(entities, DesignLabel.randomDesign());
                        Game.currentLevel(level);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        Game.userOnFrame(debugger::execute);
        Game.windowTitle("My Dungeon");
        Game.add(new AISystem());
        Game.add(new CollisionSystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());
        // build and start game
        Game.run();
    }
}
