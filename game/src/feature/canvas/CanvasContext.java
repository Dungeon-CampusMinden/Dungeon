package feature.canvas;

/**
 * Runtime context supplied while a canvas creates its default nodes.
 *
 * @param canvasId the id of the canvas being created
 * @param heroId the entity id of the hero that opened the canvas
 * @param prototypeRun whether nodes are being created client-side as callback-bearing prototypes;
 *     suppliers must return every node that can carry runtime behavior during such a run, while the
 *     server-provided snapshot remains authoritative about which nodes are displayed
 */
public record CanvasContext(String canvasId, int heroId, boolean prototypeRun) {}
