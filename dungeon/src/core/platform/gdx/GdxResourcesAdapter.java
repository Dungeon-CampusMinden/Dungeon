package core.platform.gdx;

import com.badlogic.gdx.Gdx;
import core.platform.ResourcesAdapter;
import java.io.IOException;
import java.io.InputStream;

public final class GdxResourcesAdapter implements ResourcesAdapter {

  @Override
  public boolean exists(String path) {
    return Gdx.files != null && Gdx.files.internal(path).exists();
  }

  @Override
  public InputStream open(String path) throws IOException {
    if (Gdx.files == null) {
      throw new IOException("libGDX backend not available (Gdx.files is null): " + path);
    }
    return Gdx.files.internal(path).read();
  }
}
