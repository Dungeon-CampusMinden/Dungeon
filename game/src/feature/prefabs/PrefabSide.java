package feature.prefabs;

/** Runtime side on which a prefab is instantiated. */
public enum PrefabSide {
  /** Created by the authoritative server and synchronized through the normal entity pipeline. */
  SERVER,
  /** Created only by visual clients and never synchronized. */
  CLIENT
}
