package feature.canvas;

/**
 * Distinguishes nodes that the server provided from nodes the player created locally.
 *
 * @see NodeState
 * @see CanvasMerger
 */
public enum NodeOrigin {
  /** The node is part of the server-provided default node set of a canvas. */
  DEFAULT,

  /** The node was created locally by the player and only exists on this client. */
  LOCAL
}
