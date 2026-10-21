package feature.prefabs;

/**
 * A connection point of a prefab type that can be linked to ports of other prefab instances.
 *
 * <p>Output ports ({@link PrefabEvent events} and {@link ArtifactOutput artifact outputs}) are
 * linked to input ports ({@link PrefabAction actions} and {@link ArtifactSlot artifact slots}) of
 * the same family. Links are stored on the receiving instance, see {@link PrefabInstance#inputs()}.
 */
public sealed interface PrefabPort permits PrefabEvent, PrefabAction, ArtifactOutput, ArtifactSlot {

  /**
   * Returns the stable serialized key, unique among the ports of one prefab type.
   *
   * @return port key
   */
  String key();

  /**
   * Returns the editor-facing port name.
   *
   * @return display name
   */
  String displayName();

  /**
   * Returns the kind of this port.
   *
   * @return port kind
   */
  Kind kind();

  /** Kinds of prefab ports. */
  enum Kind {
    /** Signal sent by an instance when something happened. */
    EVENT,
    /** Behavior of an instance that runs when a linked event is sent. */
    ACTION,
    /** Value produced by an instance. */
    ARTIFACT_OUTPUT,
    /** Value consumed by an instance. */
    ARTIFACT_SLOT;

    /**
     * Returns whether ports of this kind receive links.
     *
     * @return true for actions and artifact slots
     */
    public boolean isInput() {
      return this == ACTION || this == ARTIFACT_SLOT;
    }

    /**
     * Returns whether ports of this kind belong to the event system rather than to artifacts.
     *
     * @return true for events and actions
     */
    public boolean isSignal() {
      return this == EVENT || this == ACTION;
    }
  }
}
