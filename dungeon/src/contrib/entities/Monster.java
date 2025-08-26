package contrib.entities;

import com.badlogic.gdx.audio.Sound;
import contrib.components.*;
import contrib.item.Item;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;


import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public enum Monster {
    IMP(
        "Imp",                 
        5.0f,                  
        1.0f,                  
        e -> {},               
        true,                  
        new SimpleIPath("character/monster/imp"), 
        3,                     
        e -> {},               
        true,                  
        MonsterDeathSound.BASIC,
        Set.of(),              
        0.2f,                  
        Set.of(),              
        MonsterIdleSound.BASIC,
        () -> AIFactory.randomFightAI(),   
        () -> AIFactory.randomIdleAI(),     
        () -> (self) -> AIFactory.randomTransition(self).apply(self),
        5,                     
        2 * Game.frameRate(),  
        DamageType.PHYSICAL    
    );

    public static Random RANDOM = new Random();
    private static final int MAX_DISTANCE_FOR_DEATH_SOUND = 15;

    private final String name;
    // Velocity
    private final float speed;
    private final float mass;
    private final Consumer<Entity> onWallHit;
    private final boolean canEnterOpenPits;
    // Draw
    private final IPath texture;
    // Health
    private final int health;
    private final Consumer<Entity> onDeath;
    private final boolean removeOnDeath;
    private final MonsterDeathSound deathSound;
    // Drops
    final Set<Class<? extends Item>> drops;
    final float dropChance;
    final Set<Class<? extends Item>> guaranteedDrops; // NEW
    // Idle sound
    private final MonsterIdleSound idleSound;
    // AI
    private final Supplier<Consumer<Entity>> fightAISupplier;
    private final Supplier<Consumer<Entity>> idleAISupplier;
    private final Supplier<Function<Entity, Boolean>> transitionAISupplier;
    // Damage
    private final int collideDamage;
    private final int collideCooldown;
    private final DamageType damageType;

    Monster(
            String name,
            float speed,
            float mass,
            Consumer<Entity> onWallHit,
            boolean canEnterOpenPits,
            IPath texture,
            int health,
            Consumer<Entity> onDeath,
            boolean removeOnDeath,
            MonsterDeathSound deathSound,
            Set<Class<? extends Item>> drops,
            float dropChance,
            Set<Class<? extends Item>> guaranteedDrops,
            MonsterIdleSound idleSound,
            Supplier<Consumer<Entity>> fightAISupplier,
            Supplier<Consumer<Entity>> idleAISupplier,
            Supplier<Function<Entity, Boolean>> transitionAISupplier,
            int collideDamage,
            int collideCooldown,
            DamageType damageType) {
        this.name = name;
        this.speed = speed;
        this.mass = mass;
        this.onWallHit = onWallHit;
        this.canEnterOpenPits = canEnterOpenPits;
        this.texture = texture;
        this.health = health;
        this.onDeath = onDeath;
        this.removeOnDeath = removeOnDeath;
        this.deathSound = deathSound;
        this.drops = drops;
        this.dropChance = dropChance;
        this.guaranteedDrops = guaranteedDrops;
        this.idleSound = idleSound;
        this.fightAISupplier = fightAISupplier;
        this.idleAISupplier = idleAISupplier;
        this.transitionAISupplier = transitionAISupplier;
        this.collideDamage = collideDamage;
        this.collideCooldown = collideCooldown;
        this.damageType = damageType;
    }

    public Entity build(Point position) throws IOException {
        Entity monster = new Entity(name);
        monster.add(new PositionComponent(position));
        monster.add(buildDrawComponent());
        monster.add(buildVelocityComponent());
        monster.add(new CollideComponent());
        monster.add(buildSpikeComponent());
        monster.add(buildAIComponent());
        monster.add(buildHealthComponent());
        monster.add(buildInventoryComponent());

        buildIdleSoundComponent().ifPresent(c -> monster.add(c));

        return monster;
    }

    private InventoryComponent buildInventoryComponent() {
        InventoryComponent ic = new InventoryComponent(drops.size() + guaranteedDrops.size());

        // 1. Always drop guaranteed items
        for (Class<? extends Item> clazz : guaranteedDrops) {
            ic.add(buildItem(clazz));
        }

        // 2. Chance-based drops
        if (!drops.isEmpty() && RANDOM.nextFloat() < dropChance) {
            ic.add(buildItem(drops.stream()
                    .skip(RANDOM.nextInt(drops.size()))
                    .findFirst()
                    .orElse(null)));
        }

        return ic;
    }

    private Item buildItem(Class<? extends Item> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Item instance", e);
        }
    }

    private SpikyComponent buildSpikeComponent() {
        return new SpikyComponent(collideDamage, damageType, collideCooldown);
    }

    private AIComponent buildAIComponent() {
        return new AIComponent(
                fightAISupplier.get(), idleAISupplier.get(), transitionAISupplier.get());
    }

    private VelocityComponent buildVelocityComponent() {
        return new VelocityComponent(speed, mass, onWallHit, canEnterOpenPits);
    }

    private HealthComponent buildHealthComponent() {
        Consumer<Entity> constructedOnDeath = entity -> {
            onDeath.accept(entity);
            playDeathSoundIfNearby(deathSound.sound(), entity);

            entity.fetch(InventoryComponent.class).ifPresent(inventoryComponent -> {
                new DropItemsInteraction().accept(entity, null);
            });

            if (removeOnDeath)
                Game.remove(entity);
        };

        return new HealthComponent(health, constructedOnDeath);
    }

    private DrawComponent buildDrawComponent() throws IOException {
        return new DrawComponent(texture);
    }

    private Optional<IdleSoundComponent> buildIdleSoundComponent() {
        if (idleSound == null || idleSound.path().pathString().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new IdleSoundComponent(idleSound.path()));
    }

    private static void playMonsterDieSound(Sound sound) {
        if (sound == null) {
            return;
        }
        long soundID = sound.play();
        sound.setLooping(soundID, false);
        sound.setVolume(soundID, 0.35f);
    }

    private static void playDeathSoundIfNearby(Sound deathSound, Entity e) {
        if (Game.hero().isEmpty()) return;
        Entity hero = Game.hero().get();
        PositionComponent pc =
                hero.fetch(PositionComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
        PositionComponent monsterPc =
                e.fetch(PositionComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));
        if (pc.position().distance(monsterPc.position()) < MAX_DISTANCE_FOR_DEATH_SOUND) {
            playMonsterDieSound(deathSound);
        }
    }

    // --- Builder ---
    public static class MonsterBuilder {
        private final Monster type;
        private Point spawnPoint = new Point(0, 0);

        MonsterBuilder(Monster type) {
            this.type = type;
        }

        public MonsterBuilder spawn(Point spawnPoint) {
            this.spawnPoint = spawnPoint;
            return this;
        }

        public MonsterBuilder addGuaranteedDrop(Class<? extends Item> itemClass) {
            this.type.guaranteedDrops.add(itemClass);
            return this;
        }

        public Entity build() throws IOException {
            return type.build(spawnPoint);
        }
    }

    public MonsterBuilder builder() {
        return new MonsterBuilder(this);
    }
}
