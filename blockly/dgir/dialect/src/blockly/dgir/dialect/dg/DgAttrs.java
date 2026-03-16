package blockly.dgir.dialect.dg;

import dgir.core.Dialect;
import dgir.core.ir.Attribute;
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
    public enum TurnDir {
      LEFT,
      RIGHT
    }

    @Override
    public @NotNull String getIdent() {
      return "dg.turnDirection";
    }

    private @NotNull TurnDir turnDir;

    private TurnDirectionAttr() {
      this(TurnDir.LEFT);
    }

    public TurnDirectionAttr(@NonNull TurnDir turnDir) {
      this.turnDir = turnDir;
    }

    @Override
    public @NotNull TurnDir getStorage() {
      return turnDir;
    }

    public @NotNull TurnDir getDirection() {
      return turnDir;
    }

    public void setDirection(@NotNull TurnDir turnDir) {
      this.turnDir = turnDir;
    }
  }

  final class UseDirectionAttr extends DungeonAttr implements DgAttrs {
    public enum UseDir {
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

    private @NotNull UseDir useDir;

    private UseDirectionAttr() {
      this(UseDir.UP);
    }

    public UseDirectionAttr(@NonNull UseDir useDir) {
      this.useDir = useDir;
    }

    @Override
    public @NotNull UseDir getStorage() {
      return useDir;
    }

    public @NotNull UseDir getDirection() {
      return useDir;
    }

    public void setDirection(@NotNull UseDir useDir) {
      this.useDir = useDir;
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
