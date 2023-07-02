package mp.packages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import contrib.components.*;
import contrib.utils.components.ai.AITools;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.MeleeAI;
import contrib.utils.components.ai.idle.PatrouilleWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.collision.DefaultCollider;
import contrib.utils.components.health.*;
import contrib.utils.components.interaction.*;
import contrib.utils.components.item.*;
import contrib.utils.components.skill.*;
import contrib.utils.components.stats.DamageModifier;
import core.Component;
import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.draw.*;
import mp.packages.request.*;
import mp.packages.response.*;
import mp.packages.event.GameStateUpdateEvent;
import mp.packages.serializer.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NetworkSetup {

    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        kryo.addDefaultSerializer(Tile.class, new TileSerializer());
        kryo.addDefaultSerializer(ILevel.class, new ILevelSerializer());

        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(InitServerRequest.class);
        kryo.register(InitServerResponse.class, new InitServerResponseSerializer());
        kryo.register(LoadMapRequest.class, new LoadMapRequestSerializer());
        kryo.register(LoadMapResponse.class, new LoadMapResponseSerializer());
        kryo.register(JoinSessionRequest.class);
        kryo.register(JoinSessionResponse.class, new JoinSessionResponseSerializer());
        kryo.register(ArrayList.class);
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
        kryo.register(UpdateOwnPositionRequest.class, new UpdateOwnPositionRequestSerializer());
        kryo.register(Class.class);
        kryo.register(HashMap.class);
        kryo.register(Set.class, new SetSerializer());
        kryo.register(HashSet.class);
        kryo.register(Entity.class, new EntitySerializer());
        kryo.register(Component.class);

        kryo.register(AIComponent.class, new AIComponentSerializer());
        kryo.register(CollideComponent.class, new CollideComponentSerializer());
        kryo.register(HealthComponent.class, new HealthComponentSerializer());
        kryo.register(InteractionComponent.class, new InteractionComponentSerializer());
        kryo.register(InventoryComponent.class, new InventoryComponentSerializer());
        kryo.register(ItemComponent.class, new ItemComponentSerializer());
        kryo.register(MultiplayerComponent.class, new MultiplayerComponentSerializer());
        kryo.register(ProjectileComponent.class, new ProjectileComponentSerializer());
        kryo.register(StatsComponent.class, new StatsComponentSerializer());
        kryo.register(XPComponent.class, new XPComponentSerializer());
        kryo.register(DrawComponent.class, new DrawComponentSerializer());
        kryo.register(PositionComponent.class, new PositionComponentSerializer());
        kryo.register(VelocityComponent.class, new VelocityComponentSerializer());

        kryo.register(Animation.class, new AnimationSerializer());
        kryo.register(Painter.class);
        kryo.register(PainterConfig.class);
        kryo.register(TextureHandler.class);
        kryo.register(TextureMap.class);

        kryo.register(CollideAI.class, new CollideAISerializer());
        kryo.register(MeleeAI.class, new MeleeAISerializer());

        kryo.register(PatrouilleWalk.class, new PatrouilleWalkSerializer());
        kryo.register(RadiusWalk.class, new RadiusWalkSerializer());
        kryo.register(StaticRadiusWalk.class, new StaticRadiusWalkSerializer());

        kryo.register(RangeTransition.class, new RangeTransitionSerializer());
        kryo.register(SelfDefendTransition.class, new SelfDefendTransitionSerializer());

        kryo.register(AITools.class);

        kryo.register(Damage.class, new DamageSerializer());
        kryo.register(DamageType.class);

        kryo.register(DropLoot.class, new DropLootSerializer());
        kryo.register(DefaultOnDeath.class, new DefaultOnDeathSerializer());

        kryo.register(ControlPointReachable.class);

        kryo.register(InteractionTool.class);

        kryo.register(DropItemsInteraction.class, new DropItemsInteractionSerializer());
        kryo.register(DefaultInteraction.class, new DefaultInteractionSerializer());

        kryo.register(ItemData.class);
        kryo.register(ItemDataGenerator.class);
        kryo.register(ItemType.class);

        kryo.register(FireballSkill.class, new FireballSkillSerializer());

        kryo.register(Skill.class, new SkillSerializer());
        kryo.register(SkillTools.class);

        kryo.register(DamageModifier.class);

        kryo.register(DefaultCollider.class, new DefaultColliderSerializer());


        kryo.register(GameStateUpdateEvent.class, new GameStateUpdateEventSerializer());
        kryo.register(GameState.class, new GameStateSerializer());
        kryo.register(UpdateOwnPositionResponse.class);
        kryo.register(ChangeMapRequest.class);
        kryo.register(ChangeMapResponse.class);
    }
}
