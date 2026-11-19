package feature.prefabs;

import feature.entities.deco.Deco;

/** Property types supported by prefab definitions and the level editor. */
public enum PrefabPropertyType {
  STRING,
  INTEGER,
  FLOAT,
  NUMBER_SLIDER,
  BOOLEAN,
  ENUM,
  /** A decoration, stored by its {@link Deco} name. */
  DECO,
  POINT,
  REGION,
  VECTOR2,
  COLOR,
  SHADER_PARAMETERS,
  IMAGE_PUZZLE_SETTINGS
}
