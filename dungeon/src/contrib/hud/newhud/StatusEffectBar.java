package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusEffectBar extends Table implements HUDElement {

  private final Map<String, StatusEffectSlot> activeSlots = new HashMap<>();

  public StatusEffectBar(Skin skin) {
    super(skin);
    defaults().pad(5);
  }

  @Override
  public void init() {
    layoutElement();

    // Beispiel Effekte
    addStatusEffect("poison", 10);
    addStatusEffect("hp-reg", 10);
    addStatusEffect("mana-reg", 10);
  }

  @Override
  public void layoutElement() {
    setPosition(80, Gdx.graphics.getHeight() - 125);
  }

  @Override
  public void update() {
    /*
    Möglicher update ablauf, sobald ein StatusEffectComponent existiert:

    Game.player().ifPresent(player -> {

      player.fetch(StatusEffectComponent.class).ifPresent(effects -> {

        // 1. Aktive Effekte holen
        Map<String, counter> activeEffects = effects.getActiveStatusEffects();

        // 2. Neue Effekte hinzufügen
        for (var effect : activeEffects.entrySet()) {
          addStatusEffect(effect.getKey(), effect.getValue());
        }

        // 3. Entfernt (abgelaufene) Effekte
        checkForRemove(activeEffects)

      });
    });
     */
  }

  public void addStatusEffect(String effectId, int counter) {
    // Wenn Effekt schon existiert -> nur Counter updaten
    if (activeSlots.containsKey(effectId)) {
      activeSlots.get(effectId).setCounterLabel(counter);
      return;
    }

    // Neuen Slot erstellen
    StatusEffectSlot slot = new StatusEffectSlot(getSkin());
    slot.setEffectIcon(getEffectIcon(effectId));
    slot.setCounterLabel(counter);

    activeSlots.put(effectId, slot);

    add(slot).size(32, 32);

    pack();
  }

  public void updateEffectCounter(String effectId, int newCounter) {
    StatusEffectSlot slot = activeSlots.get(effectId);
    if (slot != null) {
      slot.setCounterLabel(newCounter);
    }
  }

  public void removeStatusEffect(String effectId) {
    StatusEffectSlot slot = activeSlots.remove(effectId);

    if (slot != null) {
      slot.remove();
      pack();
    }
  }

  public void checkForRemove(Map<String, Integer> activeEffects) {
    List<String> currentKeys = new ArrayList<>(activeSlots.keySet());

    for (String effectId : currentKeys) {
      if (!activeEffects.containsKey(effectId)) {
        removeStatusEffect(effectId);
      }
    }
  }

  public Texture getEffectIcon(String effectId) {
    String path;

    switch (effectId) {
      case "poison" -> path = "dungeon/assets/hud/ressourceIcons/poison_icon.png";
      case "hp-reg" -> path = "dungeon/assets/hud/ressourceIcons/hpregen_icon.png";
      case "mana-reg" -> path = "dungeon/assets/hud/ressourceIcons/manaregen_icon.png";
      default -> path = "dungeon/assets/hud/empty.png";
    }

    return new Texture(path);
  }
}
