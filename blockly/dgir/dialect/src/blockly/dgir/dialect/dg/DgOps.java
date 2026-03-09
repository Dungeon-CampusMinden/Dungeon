package blockly.dgir.dialect.dg;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.debug.Location;
import dgir.core.ir.NamedAttribute;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.traits.INoOperands;
import dgir.core.traits.INoResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Function;

import static blockly.dgir.dialect.dg.DgAttrs.*;

/**
 * Sealed marker interface for all operations in the {@link DungeonDialect}.
 *
 * <p>Every concrete op must both extend {@link DungeonOp} and implement this interface so that
 * {@link Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface DgOps {
  /**
   * Abstract base class for all operations in the {@code dg} dialect (namespace {@code "dg"}).
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and must implement {@link DgOps} to
   * be enumerated by {@link DungeonDialect}.
   */
  abstract class DungeonOp extends Op {
    protected DungeonOp() {}

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return DungeonDialect.class;
    }

    @Override
    public @NotNull String getNamespace() {
      return "dg";
    }
  }

  /**
   * Base class for hero commands that take no operands and produce no results.
   *
   * <p>All subclasses are leaf ops that carry their meaning solely through their ident and default
   * attributes.
   */
  abstract class HeroOp extends DungeonOp implements INoOperands, INoResult {
    @Override
    public @NonNull Function<Operation, Boolean> getVerifier() {
      // No verification needed since the traits guarantee that the operation has no operands and no
      // results.
      return ignored -> true;
    }

    protected HeroOp() {}

    protected HeroOp(Location location) {
      setOperation(Operation.Create(location, this, null, null, null));
    }
  }

  /**
   * Move the hero one tile in the current view direction.
   *
   * <p>Ident: {@code dg.move}
   */
  final class MoveOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.move";
    }

    private MoveOp() {}

    public MoveOp(Location location) {
      super(location);
    }
  }

  /**
   * Turn the hero left or right.
   *
   * <p>Ident: {@code dg.turn}
   *
   * <p>Default attributes:
   *
   * <ul>
   *   <li>{@code direction}: {@link TurnDirectionAttr} (default {@code LEFT})
   * </ul>
   */
  final class TurnOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.turn";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(
          new NamedAttribute("direction", new TurnDirectionAttr(TurnDirectionAttr.TurnDir.LEFT)));
    }

    private TurnOp() {}

    public TurnOp(Location location, TurnDirectionAttr.TurnDir turnDir) {
      super(location);
      getDirectionAttribute().setDirection(turnDir);
    }

    public @NotNull TurnDirectionAttr getDirectionAttribute() {
      return getAttributeAs("direction", TurnDirectionAttr.class).orElseThrow();
    }

    public @NotNull DgAttrs.TurnDirectionAttr.TurnDir getDirection() {
      return getDirectionAttribute().getDirection();
    }
  }

  /**
   * Use an interactable relative to the hero.
   *
   * <p>Ident: {@code dg.use}
   *
   * <p>Default attributes:
   *
   * <ul>
   *   <li>{@code direction}: {@link UseDirectionAttr} (default {@code HERE})
   * </ul>
   */
  final class InteractOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.use";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(
          new NamedAttribute(
              "direction", new DgAttrs.UseDirectionAttr(UseDirectionAttr.UseDir.HERE)));
    }

    private InteractOp() {}

    public InteractOp(Location location, UseDirectionAttr.UseDir useDir) {
      super(location);
      getDirectionAttribute().setDirection(useDir);
    }

    public @NotNull UseDirectionAttr getDirectionAttribute() {
      return getAttributeAs("direction", UseDirectionAttr.class).orElseThrow();
    }

    public @NotNull DgAttrs.UseDirectionAttr.UseDir getDirection() {
      return getDirectionAttribute().getDirection();
    }
  }

  /**
   * Push an entity in front of the hero.
   *
   * <p>Ident: {@code dg.push}
   */
  final class PushOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.push";
    }

    private PushOp() {}

    public PushOp(Location location) {
      super(location);
    }
  }

  /**
   * Pull an entity in front of the hero.
   *
   * <p>Ident: {@code dg.pull}
   */
  final class PullOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.pull";
    }

    private PullOp() {}

    public PullOp(Location location) {
      super(location);
    }
  }

  /**
   * Drop an item on the hero's tile.
   *
   * <p>Ident: {@code dg.drop}
   *
   * <p>Default attributes:
   *
   * <ul>
   *   <li>{@code dropType}: {@link DropTypeAttr} (default {@code CLOVER})
   * </ul>
   */
  final class DropOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.drop";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(
          new NamedAttribute("dropType", new DropTypeAttr(DropTypeAttr.DropType.CLOVER)));
    }

    private DropOp() {}

    public DropOp(Location location) {
      super(location);
    }
  }

  /**
   * Pick up an item on the hero's tile.
   *
   * <p>Ident: {@code dg.pickup}
   */
  final class PickupOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.pickup";
    }

    private PickupOp() {}

    public PickupOp(Location location) {
      super(location);
    }
  }

  /**
   * Shoot a fireball in the current view direction.
   *
   * <p>Ident: {@code dg.fireball}
   */
  final class FireballOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.fireball";
    }

    private FireballOp() {}

    public FireballOp(Location location) {
      super(location);
    }
  }

  /**
   * Do nothing for a short time.
   *
   * <p>Ident: {@code dg.rest}
   */
  final class RestOp extends HeroOp implements DgOps {
    @Override
    public @NotNull String getIdent() {
      return "dg.rest";
    }

    private RestOp() {}

    public RestOp(Location location) {
      super(location);
    }
  }
}
