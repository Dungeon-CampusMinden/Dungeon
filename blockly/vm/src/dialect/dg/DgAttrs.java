package dialect.dg;

import core.Dialect;
import core.ir.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public sealed interface DgAttrs {
  abstract class DungeonAttr extends Attribute {
    @Override
    public @NotNull String getNamespace() {
      return "dg";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return DungeonDialect.class;
    }

    protected DungeonAttr() {}
  }

  final class TurnDirectionAttr extends DungeonAttr implements DgAttrs {
    public enum Direction {
      LEFT,
      RIGHT
    }

    @Override
    public @NotNull String getIdent() {
      return "dg.turnDirection";
    }

    private @NotNull Direction direction;

    private TurnDirectionAttr() {
      this(Direction.LEFT);
    }

    public TurnDirectionAttr(@NonNull Direction direction) {
      this.direction = direction;
    }

    @Override
    public @NotNull Direction getStorage() {
      return direction;
    }

    public @NotNull Direction getDirection() {
      return direction;
    }

    public void setDirection(@NotNull Direction direction) {
      this.direction = direction;
    }
  }

  final class UseDirectionAttr extends DungeonAttr implements DgAttrs {
    public enum Direction {
      HERE,
      UP,
      DOWN,
      LEFT,
      RIGHT
    }

    @Override
    public @NotNull String getIdent() {
      return "dg.direction";
    }

    private @NotNull Direction direction;

    private UseDirectionAttr() {
      this(Direction.UP);
    }

    public UseDirectionAttr(@NonNull Direction direction) {
      this.direction = direction;
    }

    @Override
    public @NotNull Direction getStorage() {
      return direction;
    }

    public @NotNull Direction getDirection() {
      return direction;
    }

    public void setDirection(@NotNull Direction direction) {
      this.direction = direction;
    }
  }

  final class DropTypeAttr extends DungeonAttr implements DgAttrs {
    public enum DropType {
      CLOVER,
      BREADCRUMBS,
    }

    @Override
    public @NotNull String getIdent() {
      return "dg.dropType";
    }

    private @NotNull DropType dropType;

    private DropTypeAttr() {
      this(DropType.CLOVER);
    }

    public DropTypeAttr(@NonNull DropType dropType) {
      this.dropType = dropType;
    }

    @Override
    public @NotNull DropType getStorage() {
      return dropType;
    }

    public @NotNull DropType getDropType() {
      return dropType;
    }

    public void setDropType(@NonNull DropType dropType) {
      this.dropType = dropType;
    }
  }
}
