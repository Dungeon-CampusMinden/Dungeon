package petriNet;

import core.Component;

/**
 * Represents a transition in a Petri net.
 *
 * <p>A transition can be connected to input and output places. It may fire when all of its input
 * places have at least one token, producing tokens in its output places.
 *
 * <p>Note: This cannot be a record because a record's identity is based on its fields.
 * TransitionComponent has no fields, so all instances of a record would be considered equal, which
 * breaks usage as a unique key in {@link PetriNetSystem}. Using a regular class ensures each
 * instance is unique by reference.
 */
public class TransitionComponent implements Component {}
