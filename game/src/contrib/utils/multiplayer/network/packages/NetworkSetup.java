package contrib.utils.multiplayer.network.packages;

import static java.util.Objects.requireNonNull;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import contrib.components.*;
import contrib.utils.components.ai.AITools;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.MeleeAI;
import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.ai.idle.PatrouilleWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.collision.DefaultCollider;
import contrib.utils.components.collision.ItemCollider;
import contrib.utils.components.health.*;
import contrib.utils.components.interaction.*;
import contrib.utils.components.item.*;
import contrib.utils.components.skill.*;
import contrib.utils.components.stats.DamageModifier;
import contrib.utils.multiplayer.network.packages.event.GameStateUpdateEvent;
import contrib.utils.multiplayer.network.packages.event.MovementEvent;
import contrib.utils.multiplayer.network.packages.event.OnAuthenticatedEvent;
import contrib.utils.multiplayer.network.packages.request.*;
import contrib.utils.multiplayer.network.packages.response.*;
import contrib.utils.multiplayer.network.packages.serializer.*;
import contrib.utils.multiplayer.network.packages.serializer.ItemDataSerializer;
import contrib.utils.multiplayer.network.packages.serializer.components.*;
import contrib.utils.multiplayer.network.packages.serializer.gamesession.*;
import contrib.utils.multiplayer.network.packages.serializer.java.BiConsumerSerializer;
import contrib.utils.multiplayer.network.packages.serializer.java.CoordinateSerializer;
import contrib.utils.multiplayer.network.packages.serializer.java.FunctionSerializer;
import contrib.utils.multiplayer.network.packages.serializer.java.TriConsumerSerializer;
import contrib.utils.multiplayer.network.packages.serializer.level.ILevelSerializer;
import contrib.utils.multiplayer.network.packages.serializer.level.TileSerializer;
import contrib.utils.multiplayer.network.packages.serializer.utils.PointSerializer;

import core.Component;
import core.Entity;
import core.components.*;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** Used to configure network endpoints (server and client). */
public class NetworkSetup {

    /**
     * Register all Classes which should or might be sent and receive by the given endpoint.
     *
     * <p>Note: For communication, both participants (client and server) must be configured in the
     * same way. This function must be executed accordingly with both instances.
     *
     * @param endPoint Endpoint that should be configured, like {@link
     *     com.esotericsoftware.kryonet.Client} and {@link
     *     com.esotericsoftware.kryonet.Server}. @See {@link
     *     com.esotericsoftware.kryonet.Client} @See {@link com.esotericsoftware.kryonet.Server}
     */
    public static void registerCommunicationClasses(EndPoint endPoint) {
        final Kryo kryo = endPoint.getKryo();

        registerDefaultSerializer(kryo);
        registerJavaUtils(kryo);
        registerComponentSerializer(kryo);
        registerMapSpecifics(kryo);
        registerEntityGenerals(kryo);
        registerGameLogicSpecifics(kryo);
        registerAiSpecifics(kryo);
        registerInteractionSpecifics(kryo);

        /* TODO: not implemented yet because of cyclic dependencies
         * kryo.register(ProtectOnApproach.class, new ProtectOnApproachSerializer());
         * kryo.register(ProtectOnAttack.class, new ProtectOnAttackSerializer());
         */
    }

    private static void registerDefaultSerializer(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.addDefaultSerializer(Tile.class, new TileSerializer());
        kryo.addDefaultSerializer(ILevel.class, new ILevelSerializer());
    }

    private static void registerJavaUtils(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(Class.class);
        kryo.register(HashMap.class);
        kryo.register(Set.class);
        kryo.register(HashSet.class);
        kryo.register(ArrayList.class);
        kryo.register(Consumer.class, new ConsumerSerializer());
        kryo.register(BiConsumer.class, new BiConsumerSerializer());
        kryo.register(TriConsumer.class, new TriConsumerSerializer());
        kryo.register(Function.class, new FunctionSerializer());
    }

    private static void registerComponentSerializer(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(Component.class);
        kryo.register(AIComponent.class, new AIComponentSerializer());
        kryo.register(CollideComponent.class, new CollideComponentSerializer());
        kryo.register(HealthComponent.class, new HealthComponentSerializer());
        kryo.register(InteractionComponent.class, new InteractionComponentSerializer());
        kryo.register(InventoryComponent.class, new InventoryComponentSerializer());
        kryo.register(ItemComponent.class, new ItemComponentSerializer());
        kryo.register(
                MultiplayerSynchronizationComponent.class,
                new MultiplayerSynchronizationComponentSerializer());
        kryo.register(ProjectileComponent.class, new ProjectileComponentSerializer());
        kryo.register(StatsComponent.class, new StatsComponentSerializer());
        kryo.register(XPComponent.class, new XPComponentSerializer());
        kryo.register(CameraComponent.class, new CameraComponentSerializer());
        kryo.register(DrawComponent.class, new DrawComponentSerializer());
        kryo.register(PlayerComponent.class, new PlayerComponentSerializer());
        kryo.register(PositionComponent.class, new PositionComponentSerializer());
        kryo.register(VelocityComponent.class, new VelocityComponentSerializer());
    }

    private static void registerMapSpecifics(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(Coordinate.class, new CoordinateSerializer());
        kryo.register(Point.class, new PointSerializer());
        kryo.register(ILevel.class);
        kryo.register(Tile[].class);
        kryo.register(Tile[][].class);
        kryo.register(TileLevel.class);
        kryo.register(TileHeuristic.class);
        kryo.register(ExitTile.class);
        kryo.register(DoorTile.class);
        kryo.register(FloorTile.class);
        kryo.register(WallTile.class);
        kryo.register(HoleTile.class);
        kryo.register(SkipTile.class);
        kryo.register(DesignLabel.class);
        kryo.register(LevelElement.class);

        kryo.register(Painter.class);
        kryo.register(PainterConfig.class);
        kryo.register(TextureHandler.class);
        kryo.register(TextureMap.class);
        kryo.register(Animation.class, new AnimationSerializer());
    }

    private static void registerEntityGenerals(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(Entity.class, new EntitySerializer());
    }

    private static void registerGameLogicSpecifics(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(AuthenticationResponse.class, new AuthenticationResponseSerializer());
        kryo.register(AuthenticationRequest.class, new AuthenticationRequestSerializer());
        kryo.register(LoadMapRequest.class, new LoadMapRequestSerializer());
        kryo.register(LoadMapResponse.class, new LoadMapResponseSerializer());
        kryo.register(JoinSessionRequest.class, new JoinSessionRequestSerializer());
        kryo.register(JoinSessionResponse.class, new JoinSessionResponseSerializer());
        kryo.register(GameStateUpdateEvent.class, new GameStateUpdateEventSerializer());
        kryo.register(GameStateUpdate.class, new GameStateUpdateSerializer());
        kryo.register(GameState.class, new GameStateSerializer());
        kryo.register(MovementEvent.class, new MovementEventSerializer());
        kryo.register(ChangeMapRequest.class);
        kryo.register(ChangeMapResponse.class);
        kryo.register(OnAuthenticatedEvent.class, new OnAuthenticatedEventSerializer());
        kryo.register(Version.class, new VersionSerializer());
    }

    private static void registerAiSpecifics(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(AITools.class);
        kryo.register(CollideAI.class, new CollideAISerializer());
        kryo.register(MeleeAI.class, new MeleeAISerializer());
        kryo.register(RangeAI.class, new RangeAiSerializer());
        kryo.register(PatrouilleWalk.class, new PatrouilleWalkSerializer());
        kryo.register(RadiusWalk.class, new RadiusWalkSerializer());
        kryo.register(StaticRadiusWalk.class, new StaticRadiusWalkSerializer());
    }

    private static void registerInteractionSpecifics(final Kryo kryo) {
        requireNonNull(kryo);
        kryo.register(RangeTransition.class, new RangeTransitionSerializer());
        kryo.register(SelfDefendTransition.class, new SelfDefendTransitionSerializer());
        kryo.register(Damage.class, new DamageSerializer());
        kryo.register(DamageType.class);
        kryo.register(DefaultOnDeath.class);
        kryo.register(DropLoot.class, new DropLootSerializer());
        kryo.register(DefaultOnDeath.class, new DefaultOnDeathSerializer());
        kryo.register(ControlPointReachable.class, new ControlPointReachableSerializer());
        kryo.register(DropItemsInteraction.class, new DropItemsInteractionSerializer());
        kryo.register(DefaultInteraction.class, new DefaultInteractionSerializer());
        kryo.register(ItemData.class, new ItemDataSerializer());
        kryo.register(DefaultDrop.class, new DefaultDropSerializer());
        kryo.register(DefaultCollect.class, new DefaultCollectSerializer());
        kryo.register(DefaultUseCallback.class, new DefaultUseCallbackSerializer());
        kryo.register(ItemType.class);
        kryo.register(FireballSkill.class, new FireballSkillSerializer());
        kryo.register(Skill.class, new SkillSerializer());
        kryo.register(SkillTools.class);
        kryo.register(DamageModifier.class);
        kryo.register(DefaultCollider.class, new DefaultColliderSerializer());
        kryo.register(ItemCollider.class, new ItemColliderSerializer());
    }
}
