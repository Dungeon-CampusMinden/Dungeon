package core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import java.util.HashMap;
import java.util.Map;

public class FontHelper {

  private static final Map<FontEntry, BitmapFont> fontStorage = new HashMap<>();

  public static BitmapFont getFont(String path, int size) {
    FontEntry entry = new FontEntry(path, size);
    if (!fontStorage.containsKey(entry)) {
      FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
      FreeTypeFontGenerator.FreeTypeFontParameter params =
          new FreeTypeFontGenerator.FreeTypeFontParameter();

      params.size = size;
      params.color = Color.WHITE;
      params.borderWidth = 1;
      params.borderColor = Color.BLACK;

      // enable mipmaps for scaling
      params.genMipMaps = true;

      BitmapFont font = generator.generateFont(params);
      fontStorage.put(entry, font);
    }
    return fontStorage.get(entry);
  }

  private record FontEntry(String path, int size) {}
}
