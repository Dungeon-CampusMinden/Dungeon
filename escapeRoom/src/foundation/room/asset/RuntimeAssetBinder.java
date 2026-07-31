package foundation.room.asset;

import com.badlogic.gdx.graphics.Pixmap;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import foundation.room.model.VerifiedAsset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/** Synchronously binds already verified Foundation custom assets before entity construction. */
public final class RuntimeAssetBinder {
  private final BiConsumer<String, byte[]> registrar;

  /**
   * Creates a render-capable binder with an injected synchronous registrar.
   *
   * @param registrar render-thread asset registrar accepting a logical path and defensive bytes
   */
  public RuntimeAssetBinder(final BiConsumer<String, byte[]> registrar) {
    this.registrar = Objects.requireNonNull(registrar, "registrar");
  }

  /**
   * Creates the production render-thread binder backed by Dungeon's texture map.
   *
   * @return render-capable binder
   */
  public static RuntimeAssetBinder rendering() {
    return new RuntimeAssetBinder(RuntimeAssetBinder::registerPixmap);
  }

  /**
   * Synchronously binds assets in exact logical-path order before custom entities are created.
   *
   * @param assets already verified immutable custom assets
   */
  public void bind(final List<VerifiedAsset> assets) {
    List<VerifiedAsset> ordered =
        List.copyOf(Objects.requireNonNull(assets, "assets")).stream()
            .sorted(Comparator.comparing(VerifiedAsset::logicalPath))
            .toList();
    requireUniquePaths(ordered);
    ordered.forEach(asset -> registrar.accept(asset.logicalPath(), asset.bytes()));
  }

  private static void requireUniquePaths(final List<VerifiedAsset> assets) {
    Set<String> paths = new HashSet<>();
    for (VerifiedAsset asset : assets) {
      Objects.requireNonNull(asset, "asset");
      if (!paths.add(asset.logicalPath())) {
        throw new IllegalArgumentException(
            "duplicate Foundation asset path: " + asset.logicalPath());
      }
    }
  }

  private static void registerPixmap(final String logicalPath, final byte[] bytes) {
    TextureMap textureMap = TextureMap.instance();
    SimpleIPath path = new SimpleIPath(logicalPath);
    Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
    try {
      textureMap.putPixmap(path, pixmap, false);
    } catch (RuntimeException | Error failure) {
      if (!pixmap.isDisposed()) {
        pixmap.dispose();
      }
      throw failure;
    }
  }
}
