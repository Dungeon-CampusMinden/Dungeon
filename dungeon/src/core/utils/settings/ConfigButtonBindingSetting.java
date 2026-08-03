package core.utils.settings;

import core.configuration.ConfigKey;
import java.util.Objects;
import java.util.function.BiConsumer;

/** A button binding setting that persists changes through a {@link ConfigKey}. */
public class ConfigButtonBindingSetting extends ButtonBindingSetting {

  private final ConfigKey<Integer> configKey;
  private final BiConsumer<Integer, Integer> onConfigChange;

  /**
   * Creates an editable button binding setting backed by the given config key.
   *
   * @param translationKey the translation key of the setting label
   * @param configKey the config key to read from and write to
   */
  public ConfigButtonBindingSetting(String translationKey, ConfigKey<Integer> configKey) {
    this(translationKey, configKey, (oldValue, newValue) -> {});
  }

  /**
   * Creates an editable button binding setting backed by the given config key.
   *
   * @param translationKey the translation key of the setting label
   * @param configKey the config key to read from and write to
   * @param onConfigChange callback invoked after a new key was persisted
   */
  public ConfigButtonBindingSetting(
      String translationKey,
      ConfigKey<Integer> configKey,
      BiConsumer<Integer, Integer> onConfigChange) {
    super(translationKey, Objects.requireNonNull(configKey, "configKey").value(), true);
    this.configKey = configKey;
    this.onConfigChange = Objects.requireNonNull(onConfigChange, "onConfigChange");
  }

  @Override
  public void value(Integer value) {
    int oldValue = value();
    super.value(value);
    configKey.value(value);
    onConfigChange.accept(oldValue, value);
  }
}
