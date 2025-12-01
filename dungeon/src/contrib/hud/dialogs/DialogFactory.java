package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import core.utils.logging.DungeonLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DialogFactory {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogFactory.class);
  private static final Map<String, DialogCreator> registry = new HashMap<>();

  static {
    register("OK", params -> {
      validateParams(params, 4, "OK dialog requires: Skin, text, title, resultHandler");
      try {
        Skin skin = castParam(params[0], Skin.class, "Skin");
        String text = castParam(params[1], String.class, "text");
        String title = castParam(params[2], String.class, "title");
        @SuppressWarnings("unchecked")
        BiFunction<TextDialog, String, Boolean> resultHandler = castParam(params[3], BiFunction.class, "resultHandler");
        return OkDialog.createOkDialog(skin, text, title, resultHandler);
      } catch (ClassCastException e) {
        throw new DialogCreationException("Failed to create OK dialog: invalid parameter types", e);
      }
    });
  }

  public static void register(String name, DialogCreator creator) {
    if (registry.containsKey(name)) {
      throw new DialogCreationException("Dialog type '" + name + "' is already registered");
    }
    registry.put(name, creator);
  }

  public static Dialog create(String type, Object... params) {
    DialogCreator creator = registry.get(type);
    if (creator == null) {
      throw new DialogCreationException("Unknown dialog type: " + type);
    }
    return creator.create(params);
  }

  private static void validateParams(Object[] params, int expected, String message) {
    if (params == null || params.length != expected) {
      throw new DialogCreationException(message);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T castParam(Object param, Class<T> type, String paramName) {
    if (param == null) {
      throw new DialogCreationException("Parameter '" + paramName + "' cannot be null");
    }
    if (!type.isInstance(param)) {
      throw new DialogCreationException("Parameter '" + paramName + "' must be of type " + type.getSimpleName() + ", got " + param.getClass().getSimpleName());
    }
    return (T) param;
  }
}
