package wizard.runner.room;

import escaperoom.foundation.room.model.ComponentPlacement;
import escaperoom.foundation.room.model.RiddleDoor;
import escaperoom.foundation.room.model.RiddlePlacement;
import escaperoom.foundation.room.model.RoomDecoration;
import escaperoom.foundation.room.model.RoomLayout;
import escaperoom.foundation.room.model.RoomPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.AssignmentInput;
import wizard.runner.model.ProjectDefinition.ChoiceInput;
import wizard.runner.model.ProjectDefinition.CollectionInput;
import wizard.runner.model.ProjectDefinition.GraphNode;
import wizard.runner.model.ProjectDefinition.GraphNodeKind;
import wizard.runner.model.ProjectDefinition.Input;
import wizard.runner.model.ProjectDefinition.NumericInput;
import wizard.runner.model.ProjectDefinition.Riddle;
import wizard.runner.room.RoomTemplate.DoorSlot;
import wizard.runner.room.RoomTemplate.Kind;
import wizard.runner.room.RoomTemplate.Side;
import wizard.runner.room.RoomTemplate.SlotKind;

/**
 * Deterministic seeded level generator built from the hand-made templates of the project theme.
 *
 * <p>Every riddle gets a main room whose slots hold its stations; stations that do not fit move to
 * side rooms behind the main room, keeping the chests of one assignment together. Players spawn in
 * the room of a riddle that is active from the session start. Every other room hangs, directly or
 * through a corridor, behind a door in exactly one room of a direct predecessor riddle and opens
 * with its own riddle. The exit alcove sits behind the outer wall of a direct end predecessor's
 * room. A failed arrangement is retried with seeds derived from the project seed.
 */
final class LevelGenerator {
  /** Number of seeds tried before the project is reported as unplaceable. */
  static final int MAX_ATTEMPTS = 32;

  /** Longest corridor chain between two rooms. */
  private static final int MAX_CORRIDORS = 2;

  private static final long SEED_STEP = 0x9E3779B97F4A7C15L;

  private LevelGenerator() {}

  /**
   * Generates the level of one validated project.
   *
   * @param project validated project
   * @return canonical riddle order and generated layout
   * @throws UnplaceableLevelException when no attempt finds a collision-free arrangement
   */
  static Plan plan(final ProjectDefinition project) {
    Objects.requireNonNull(project, "project");
    List<String> riddleIds = stableRiddleOrder(project);
    List<RoomTemplate> templates = WizardThemeCatalog.templates(project.scenario().themeId());
    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      Random random = new Random(project.seed() + attempt * SEED_STEP);
      Optional<RoomLayout> layout = new Attempt(project, riddleIds, templates, random).run();
      if (layout.isPresent()) {
        return new Plan(riddleIds, layout.orElseThrow());
      }
    }
    throw new UnplaceableLevelException();
  }

  private static List<String> stableRiddleOrder(final ProjectDefinition project) {
    Map<String, GraphNode> nodes = indexNodes(project);
    Map<String, Set<String>> outgoing = outgoing(project);
    Map<String, Integer> indegrees = new HashMap<>();
    Map<String, Integer> distances = new HashMap<>();
    nodes.keySet().forEach(id -> indegrees.put(id, 0));
    project.riddleGraph().edges().forEach(edge -> indegrees.merge(edge.to(), 1, Integer::sum));
    PriorityQueue<String> ready = new PriorityQueue<>(nodeOrder(nodes));
    indegrees.forEach(
        (id, count) -> {
          if (count == 0) {
            ready.add(id);
          }
        });
    distances.put(nodeId(nodes, GraphNodeKind.START), 0);
    while (!ready.isEmpty()) {
      String current = ready.remove();
      for (String successor : outgoing.getOrDefault(current, Set.of())) {
        distances.merge(successor, distances.get(current) + 1, Math::max);
        if (indegrees.merge(successor, -1, Integer::sum) == 0) {
          ready.add(successor);
        }
      }
    }
    return nodes.values().stream()
        .filter(node -> node.kind() == GraphNodeKind.RIDDLE)
        .sorted(
            Comparator.comparingInt((GraphNode node) -> distances.get(node.id()))
                .thenComparing(node -> node.riddleId().orElseThrow())
                .thenComparing(GraphNode::id))
        .map(node -> node.riddleId().orElseThrow())
        .toList();
  }

  private static String nodeId(
      final Map<String, GraphNode> nodes, final GraphNodeKind expectedKind) {
    return nodes.values().stream()
        .filter(node -> node.kind() == expectedKind)
        .map(GraphNode::id)
        .findFirst()
        .orElseThrow();
  }

  private static Map<String, GraphNode> indexNodes(final ProjectDefinition project) {
    Map<String, GraphNode> nodes = new TreeMap<>();
    project.riddleGraph().nodes().forEach(node -> nodes.put(node.id(), node));
    return nodes;
  }

  private static Map<String, Set<String>> outgoing(final ProjectDefinition project) {
    Map<String, Set<String>> outgoing = new TreeMap<>();
    project
        .riddleGraph()
        .edges()
        .forEach(
            edge ->
                outgoing.computeIfAbsent(edge.from(), ignored -> new TreeSet<>()).add(edge.to()));
    return outgoing;
  }

  private static Comparator<String> nodeOrder(final Map<String, GraphNode> nodes) {
    return Comparator.comparing((String id) -> nodes.get(id).riddleId().orElse(""))
        .thenComparing(Comparator.naturalOrder());
  }

  /**
   * Stations of one riddle in canonical placement order.
   *
   * @param riddle riddle
   * @return sources first, then input stations
   */
  private static List<Station> stations(final Riddle riddle) {
    List<Station> stations = new ArrayList<>();
    riddle
        .informationSources()
        .forEach(
            source ->
                stations.add(new Station(source.id(), source.surfaceId(), SlotKind.CONTAINER, "")));
    for (Input input : riddle.inputs()) {
      switch (input) {
        case CollectionInput ignored -> {}
        case NumericInput numeric ->
            stations.add(new Station(numeric.id(), numeric.surfaceId(), SlotKind.KEYPAD, ""));
        case ChoiceInput choice ->
            stations.add(new Station(choice.id(), choice.surfaceId(), SlotKind.TERMINAL, ""));
        case AssignmentInput assignment -> {
          assignment
              .containers()
              .forEach(
                  container ->
                      stations.add(
                          new Station(
                              container.id(),
                              assignment.surfaceId(),
                              SlotKind.CHEST,
                              assignment.id())));
          assignment
              .items()
              .forEach(
                  item ->
                      stations.add(
                          new Station(item.id(), assignment.surfaceId(), SlotKind.ITEM, "")));
        }
      }
    }
    return List.copyOf(stations);
  }

  /**
   * Canonical riddle order and generated layout.
   *
   * @param riddleIds riddles ordered by longest start distance, then riddle id
   * @param layout generated layout with placements in the same order
   */
  record Plan(List<String> riddleIds, RoomLayout layout) {}

  /**
   * One surface-bound riddle station.
   *
   * @param componentId source, input, chest, or item identifier
   * @param surfaceId authored surface
   * @param slotKind required slot kind
   * @param group non-empty for chests that must share one room
   */
  private record Station(String componentId, String surfaceId, SlotKind slotKind, String group) {}

  /**
   * Room template filled with stations.
   *
   * @param template chosen template
   * @param stations slot tile by component identifier
   * @param hint hint slot tile when this is a main room of a riddle with hints
   */
  private record Fill(
      RoomTemplate template, Map<String, RoomPoint> stations, Optional<RoomPoint> hint) {}

  /**
   * Global level cell; coordinates may be negative until the level is normalized.
   *
   * @param x column
   * @param y row from the top
   */
  private record Cell(int x, int y) {
    Cell plus(final RoomPoint point) {
      return new Cell(x + point.x(), y + point.y());
    }

    Cell step(final Side side) {
      return new Cell(x + side.dx, y + side.dy);
    }
  }

  /**
   * Template stamped into the level.
   *
   * @param template stamped template
   * @param origin global cell of the template's top-left tile
   * @param corridors corridors that lead away from this room and open together with it
   */
  private record Placed(RoomTemplate template, Cell origin, List<Placed> corridors) {
    Placed(final RoomTemplate template, final Cell origin) {
      this(template, origin, new ArrayList<>());
    }
  }

  /** One seeded generation attempt. */
  private static final class Attempt {
    private final ProjectDefinition project;
    private final List<String> riddleIds;
    private final List<RoomTemplate> rooms;
    private final List<RoomTemplate> corridors;
    private final List<RoomTemplate> exits;
    private final Random random;
    private final Map<Cell, Character> grid = new HashMap<>();
    private final Map<String, Placed> mainRooms = new LinkedHashMap<>();
    private final Map<RoomTemplate, Integer> uses = new HashMap<>();
    private final Map<String, Cell> stations = new HashMap<>();
    private final Map<String, Cell> hints = new HashMap<>();
    private final List<Map.Entry<String, Cell>> riddleDoors = new ArrayList<>();
    private final List<Map.Entry<RoomDecoration, Cell>> decorations = new ArrayList<>();
    private Cell exitDoor;
    private Cell exit;

    Attempt(
        final ProjectDefinition project,
        final List<String> riddleIds,
        final List<RoomTemplate> templates,
        final Random random) {
      this.project = project;
      this.riddleIds = riddleIds;
      this.random = random;
      rooms = templates.stream().filter(template -> template.kind() == Kind.ROOM).toList();
      corridors = templates.stream().filter(template -> template.kind() == Kind.CORRIDOR).toList();
      exits = templates.stream().filter(template -> template.kind() == Kind.EXIT).toList();
    }

    Optional<RoomLayout> run() {
      Map<String, Riddle> riddles = new TreeMap<>();
      project.riddles().forEach(riddle -> riddles.put(riddle.id(), riddle));
      Map<String, String> riddleByNode = new HashMap<>();
      String endNode = "";
      for (GraphNode node : project.riddleGraph().nodes()) {
        switch (node.kind()) {
          case START -> {}
          case END -> endNode = node.id();
          case RIDDLE -> riddleByNode.put(node.id(), node.riddleId().orElseThrow());
        }
      }
      Map<String, List<String>> predecessors = new HashMap<>();
      Map<String, Boolean> initial = new HashMap<>();
      Map<String, Integer> successors = new HashMap<>();
      List<String> endPredecessors = new ArrayList<>();
      for (var edge : project.riddleGraph().edges()) {
        String to = riddleByNode.get(edge.to());
        String from = riddleByNode.get(edge.from());
        if (from != null) {
          successors.merge(from, 1, Integer::sum);
        }
        if (edge.to().equals(endNode)) {
          if (from != null) {
            endPredecessors.add(from);
          }
        } else if (from == null) {
          initial.putIfAbsent(to, true);
        } else {
          predecessors.computeIfAbsent(to, ignored -> new ArrayList<>()).add(from);
          initial.put(to, false);
        }
      }
      List<String> initialRiddles =
          riddleIds.stream().filter(id -> initial.getOrDefault(id, false)).toList();

      Map<String, List<Fill>> fills = new HashMap<>();
      for (String riddleId : riddleIds) {
        Optional<List<Fill>> riddleFills =
            allocate(riddles.get(riddleId), 1 + successors.getOrDefault(riddleId, 0));
        if (riddleFills.isEmpty()) {
          return Optional.empty();
        }
        fills.put(riddleId, riddleFills.orElseThrow());
      }

      String spawnRiddle = initialRiddles.get(random.nextInt(initialRiddles.size()));
      List<String> order = new ArrayList<>(List.of(spawnRiddle));
      riddleIds.stream().filter(id -> !id.equals(spawnRiddle)).forEach(order::add);
      for (String riddleId : order) {
        List<Fill> riddleFills = fills.get(riddleId);
        Placed main;
        if (riddleId.equals(spawnRiddle)) {
          main = stamp(riddleFills.getFirst(), new Cell(0, 0), riddleId);
        } else {
          List<String> parents =
              initial.getOrDefault(riddleId, false)
                  ? initialRiddles.stream().filter(mainRooms::containsKey).toList()
                  : sorted(predecessors.get(riddleId));
          Optional<Placed> attached =
              attach(
                  riddleFills.getFirst(),
                  riddleId,
                  shuffled(parents.stream().map(mainRooms::get).toList()));
          if (attached.isEmpty()) {
            return Optional.empty();
          }
          main = attached.orElseThrow();
        }
        mainRooms.put(riddleId, main);
        List<Placed> riddleRooms = new ArrayList<>(List.of(main));
        for (Fill side : riddleFills.subList(1, riddleFills.size())) {
          Optional<Placed> placed = attach(side, riddleId, shuffled(riddleRooms));
          if (placed.isEmpty()) {
            return Optional.empty();
          }
          riddleRooms.add(placed.orElseThrow());
        }
      }

      List<String> exitRooms =
          endPredecessors.isEmpty() ? initialRiddles : sorted(new ArrayList<>(endPredecessors));
      if (!attachExit(shuffled(exitRooms.stream().map(mainRooms::get).toList()))) {
        return Optional.empty();
      }
      Placed spawn = mainRooms.get(spawnRiddle);
      return Optional.of(layout(riddles, spawn.origin().plus(spawn.template().start())));
    }

    /**
     * Splits the stations of one riddle over a main room and as many side rooms as needed.
     *
     * <p>The main room prefers templates with a door slot for the entrance and for every graph
     * successor. Among the templates that fit, the least used ones are preferred so that rooms
     * vary.
     *
     * @param riddle riddle to allocate
     * @param doors door slots the main room should offer
     * @return main room first, or empty when some station fits no template
     */
    private Optional<List<Fill>> allocate(final Riddle riddle, final int doors) {
      List<Station> remaining = new ArrayList<>(stations(riddle));
      boolean hint = !riddle.hints().isEmpty();
      List<Fill> fills = new ArrayList<>();
      while (fills.isEmpty() || !remaining.isEmpty()) {
        Map<SlotKind, Integer> needs = new EnumMap<>(SlotKind.class);
        remaining.forEach(station -> needs.merge(station.slotKind(), 1, Integer::sum));
        if (hint && fills.isEmpty()) {
          needs.put(SlotKind.HINT, 1);
        }
        List<RoomTemplate> fitting =
            rooms.stream()
                .filter(
                    template ->
                        needs.entrySet().stream()
                            .allMatch(need -> template.capacity(need.getKey()) >= need.getValue()))
                .toList();
        if (fitting.isEmpty()) {
          int best =
              rooms.stream().mapToInt(template -> heldCount(template, needs)).max().orElse(0);
          fitting = rooms.stream().filter(template -> heldCount(template, needs) == best).toList();
        }
        if (fills.isEmpty()
            && fitting.stream().anyMatch(template -> template.doors().size() >= doors)) {
          fitting = fitting.stream().filter(template -> template.doors().size() >= doors).toList();
        }
        int fewestUses =
            fitting.stream().mapToInt(candidate -> uses.getOrDefault(candidate, 0)).min().orElse(0);
        List<RoomTemplate> leastUsed =
            fitting.stream()
                .filter(candidate -> uses.getOrDefault(candidate, 0) == fewestUses)
                .toList();
        RoomTemplate template = leastUsed.get(random.nextInt(leastUsed.size()));
        uses.merge(template, 1, Integer::sum);
        Map<String, RoomPoint> filled = new LinkedHashMap<>();
        Map<SlotKind, Integer> used = new EnumMap<>(SlotKind.class);
        for (Station station : List.copyOf(remaining)) {
          SlotKind kind = station.slotKind();
          int index = used.getOrDefault(kind, 0);
          if (!station.group().isEmpty() && !filled.containsKey(station.componentId())) {
            List<Station> group =
                remaining.stream().filter(other -> other.group().equals(station.group())).toList();
            if (index + group.size() > template.capacity(kind)) {
              continue;
            }
          }
          if (index < template.capacity(kind)) {
            filled.put(station.componentId(), template.slots().get(kind).get(index));
            used.put(kind, index + 1);
            remaining.remove(station);
          }
        }
        Optional<RoomPoint> hintSlot =
            hint && fills.isEmpty()
                ? Optional.of(template.slots().get(SlotKind.HINT).getFirst())
                : Optional.empty();
        if (filled.isEmpty() && !fills.isEmpty()) {
          return Optional.empty();
        }
        fills.add(new Fill(template, filled, hintSlot));
      }
      return Optional.of(List.copyOf(fills));
    }

    private static int heldCount(final RoomTemplate template, final Map<SlotKind, Integer> needs) {
      return needs.entrySet().stream()
          .mapToInt(need -> Math.min(template.capacity(need.getKey()), need.getValue()))
          .sum();
    }

    /**
     * Connects a filled room to the first parent that has room for it.
     *
     * <p>A room connects to a free door slot of the parent or of a corridor that already leads away
     * from the parent; both open together with the parent.
     *
     * @param fill room to place
     * @param riddleId riddle whose activation opens the connecting door
     * @param parents candidate parents in trial order
     * @return placed room, or empty when no parent has room
     */
    private Optional<Placed> attach(
        final Fill fill, final String riddleId, final List<Placed> parents) {
      for (Placed parent : parents) {
        List<Placed> pieces = new ArrayList<>(List.of(parent));
        pieces.addAll(shuffled(parent.corridors()));
        for (Placed piece : pieces) {
          Optional<Placed> placed =
              connect(parent, piece, fill.template(), riddleId, MAX_CORRIDORS);
          if (placed.isPresent()) {
            Placed room = placed.orElseThrow();
            fill.stations()
                .forEach(
                    (componentId, slot) -> stations.put(componentId, room.origin().plus(slot)));
            fill.hint().ifPresent(slot -> hints.put(riddleId, room.origin().plus(slot)));
            return placed;
          }
        }
      }
      return Optional.empty();
    }

    private boolean attachExit(final List<Placed> rooms) {
      for (Placed room : rooms) {
        for (RoomTemplate template : shuffled(exits)) {
          if (connect(room, room, template, null, 0).isPresent()) {
            return true;
          }
        }
      }
      return false;
    }

    /**
     * Connects a template to a free door slot of a piece, directly or through corridors.
     *
     * @param owner room that new corridors belong to
     * @param piece owner room or one of its corridors
     * @param template template to connect
     * @param riddleId riddle that opens the door, or null for the exit door
     * @param corridorsLeft corridors that may still sit between piece and template
     * @return placed template, or empty when it fits nowhere
     */
    private Optional<Placed> connect(
        final Placed owner,
        final Placed piece,
        final RoomTemplate template,
        final String riddleId,
        final int corridorsLeft) {
      for (int door : shuffled(freeDoors(piece))) {
        DoorSlot slot = piece.template().doors().get(door);
        Optional<Placed> placed =
            reach(
                owner,
                piece.origin().plus(slot.point()),
                slot.side(),
                template,
                riddleId,
                corridorsLeft);
        if (placed.isPresent()) {
          return placed;
        }
      }
      return Optional.empty();
    }

    /**
     * Places a template behind a junction tile, possibly after a chain of corridors.
     *
     * @param owner room that new corridors belong to
     * @param junction wall tile shared with the piece in front
     * @param side side of the piece in front that the junction faces
     * @param template template to place
     * @param riddleId riddle that opens the door, or null for the exit door
     * @param corridorsLeft corridors that may still be inserted
     * @return placed template, or empty when no arrangement fits
     */
    private Optional<Placed> reach(
        final Placed owner,
        final Cell junction,
        final Side side,
        final RoomTemplate template,
        final String riddleId,
        final int corridorsLeft) {
      List<Optional<RoomTemplate>> connectors = new ArrayList<>();
      connectors.add(Optional.empty());
      if (corridorsLeft > 0) {
        corridors.forEach(corridor -> connectors.add(Optional.of(corridor)));
      }
      for (Optional<RoomTemplate> connector : shuffled(connectors)) {
        if (connector.isEmpty()) {
          Optional<Placed> placed = place(template, junction, side, riddleId);
          if (placed.isPresent()) {
            setDoor(junction, riddleId);
            return placed;
          }
          continue;
        }
        RoomTemplate corridor = connector.orElseThrow();
        for (int entry : shuffled(doorsFacing(corridor, side.opposite()))) {
          Cell corridorOrigin = origin(junction, corridor.doors().get(entry));
          if (!fits(corridor, corridorOrigin)) {
            continue;
          }
          Map<Cell, Character> undo = stampTiles(corridor, corridorOrigin);
          Placed placedCorridor = new Placed(corridor, corridorOrigin);
          Optional<Placed> placed =
              connect(owner, placedCorridor, template, riddleId, corridorsLeft - 1);
          if (placed.isPresent()) {
            grid.put(junction, '.');
            addDecorations(corridor, corridorOrigin);
            owner.corridors().add(placedCorridor);
            return placed;
          }
          undo.forEach(
              (cell, previous) -> {
                if (previous == ' ') {
                  grid.remove(cell);
                } else {
                  grid.put(cell, previous);
                }
              });
        }
      }
      return Optional.empty();
    }

    private Optional<Placed> place(
        final RoomTemplate template, final Cell junction, final Side side, final String riddleId) {
      for (int door : shuffled(doorsFacing(template, side.opposite()))) {
        Cell origin = origin(junction, template.doors().get(door));
        if (fits(template, origin)) {
          stampTiles(template, origin);
          addDecorations(template, origin);
          return Optional.of(new Placed(template, origin));
        }
      }
      return Optional.empty();
    }

    private Placed stamp(final Fill fill, final Cell origin, final String riddleId) {
      stampTiles(fill.template(), origin);
      addDecorations(fill.template(), origin);
      fill.stations().forEach((componentId, slot) -> stations.put(componentId, origin.plus(slot)));
      fill.hint().ifPresent(slot -> hints.put(riddleId, origin.plus(slot)));
      return new Placed(fill.template(), origin);
    }

    private void addDecorations(final RoomTemplate template, final Cell origin) {
      template
          .decorations()
          .forEach(
              decoration -> decorations.add(Map.entry(decoration, origin.plus(decoration.tile()))));
    }

    private void setDoor(final Cell junction, final String riddleId) {
      grid.put(junction, 'D');
      if (riddleId == null) {
        exitDoor = junction;
      } else {
        riddleDoors.add(Map.entry(riddleId, junction));
      }
    }

    private boolean fits(final RoomTemplate template, final Cell origin) {
      List<String> rows = template.rows();
      for (int y = 0; y < rows.size(); y++) {
        for (int x = 0; x < rows.get(y).length(); x++) {
          char tile = rows.get(y).charAt(x);
          if (tile == ' ') {
            continue;
          }
          char existing = grid.getOrDefault(new Cell(origin.x() + x, origin.y() + y), ' ');
          if (existing != ' ' && !(existing == '#' && tile == '#')) {
            return false;
          }
        }
      }
      return true;
    }

    /**
     * Stamps template tiles into the level.
     *
     * @param template template to stamp
     * @param origin global cell of the template's top-left tile
     * @return previous content of every changed cell, for rollback
     */
    private Map<Cell, Character> stampTiles(final RoomTemplate template, final Cell origin) {
      Map<Cell, Character> previous = new HashMap<>();
      List<String> rows = template.rows();
      for (int y = 0; y < rows.size(); y++) {
        for (int x = 0; x < rows.get(y).length(); x++) {
          char tile = rows.get(y).charAt(x);
          if (tile == ' ') {
            continue;
          }
          Cell cell = new Cell(origin.x() + x, origin.y() + y);
          previous.put(cell, grid.getOrDefault(cell, ' '));
          grid.put(cell, tile);
          if (tile == 'E') {
            exit = cell;
          }
        }
      }
      return previous;
    }

    private static Cell origin(final Cell junction, final DoorSlot slot) {
      return new Cell(junction.x() - slot.point().x(), junction.y() - slot.point().y());
    }

    private List<Integer> freeDoors(final Placed placed) {
      List<Integer> free = new ArrayList<>();
      List<DoorSlot> doors = placed.template().doors();
      for (int index = 0; index < doors.size(); index++) {
        DoorSlot slot = doors.get(index);
        Cell outside = placed.origin().plus(slot.point()).step(slot.side());
        if (grid.getOrDefault(outside, ' ') == ' ') {
          free.add(index);
        }
      }
      return free;
    }

    private static List<Integer> doorsFacing(final RoomTemplate template, final Side side) {
      List<Integer> facing = new ArrayList<>();
      for (int index = 0; index < template.doors().size(); index++) {
        if (template.doors().get(index).side() == side) {
          facing.add(index);
        }
      }
      return facing;
    }

    private List<String> sorted(final List<String> riddles) {
      return riddleIds.stream().filter(riddles::contains).toList();
    }

    private <T> List<T> shuffled(final List<T> values) {
      List<T> result = new ArrayList<>(values);
      Collections.shuffle(result, random);
      return result;
    }

    private RoomLayout layout(final Map<String, Riddle> riddles, final Cell start) {
      int minX = grid.keySet().stream().mapToInt(Cell::x).min().orElseThrow();
      int minY = grid.keySet().stream().mapToInt(Cell::y).min().orElseThrow();
      int maxX = grid.keySet().stream().mapToInt(Cell::x).max().orElseThrow();
      int maxY = grid.keySet().stream().mapToInt(Cell::y).max().orElseThrow();
      List<String> rows = new ArrayList<>();
      for (int y = minY; y <= maxY; y++) {
        StringBuilder row = new StringBuilder();
        for (int x = minX; x <= maxX; x++) {
          row.append(grid.getOrDefault(new Cell(x, y), ' '));
        }
        rows.add(row.toString());
      }
      List<RiddlePlacement> placements = new ArrayList<>();
      for (String riddleId : riddleIds) {
        Riddle riddle = riddles.get(riddleId);
        placements.add(
            new RiddlePlacement(
                riddleId,
                stations(riddle).stream()
                    .map(
                        station ->
                            new ComponentPlacement(
                                station.componentId(),
                                station.surfaceId(),
                                point(stations.get(station.componentId()), minX, minY)))
                    .toList(),
                Optional.ofNullable(hints.get(riddleId)).map(cell -> point(cell, minX, minY))));
      }
      return new RoomLayout(
          rows,
          point(start, minX, minY),
          placements,
          riddleDoors.stream()
              .map(door -> new RiddleDoor(door.getKey(), point(door.getValue(), minX, minY)))
              .toList(),
          point(exitDoor, minX, minY),
          point(exit, minX, minY),
          decorations.stream()
              .map(
                  entry ->
                      new RoomDecoration(
                          entry.getKey().deco(),
                          point(entry.getValue(), minX, minY),
                          entry.getKey().offsetX(),
                          entry.getKey().offsetY()))
              .toList());
    }

    private static RoomPoint point(final Cell cell, final int minX, final int minY) {
      return new RoomPoint(cell.x() - minX, cell.y() - minY);
    }
  }
}
