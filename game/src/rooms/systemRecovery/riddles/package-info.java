/**
 * Level-owned System Recovery riddles in gameplay order.
 *
 * <ul>
 *   <li>1: {@link rooms.systemRecovery.riddles.EnergyRiddle} materializes the battery reward.
 *   <li>2: {@link rooms.systemRecovery.riddles.ModuleStorageRiddle} manages sockets and modules.
 *   <li>3: {@link rooms.systemRecovery.riddles.InventoryScannerRiddle} counts surviving modules.
 *   <li>4: {@link rooms.systemRecovery.riddles.TransportStorageRiddle} collects conveyor packages.
 *   <li>5: {@link rooms.systemRecovery.riddles.ManualSortingRiddle} teaches pairwise comparisons.
 *   <li>6: {@link rooms.systemRecovery.riddles.BubbleSortRiddle} runs the programmed sorting chip.
 * </ul>
 *
 * <p>Each controller keeps its setup, mutable state, interactions and scheduled actions together.
 * Controllers are constructed with their owning level; only the transport scanner is shared between
 * riddles 4 and 6. Terminal callbacks forward accepted steps to these controllers. The interpreter
 * module remains independent of rooms, entities and callbacks.
 *
 * <p>The server owns progression, items, doors and scheduling. Network translators export entity
 * state and comparison IDs; clients render the associated visual effects. Never load textures or
 * construct rendering-only shaders on the headless server. The manual sorting introduction is
 * triggered per player, and explicitly suppressed in level editor mode.
 */
package rooms.systemRecovery.riddles;
