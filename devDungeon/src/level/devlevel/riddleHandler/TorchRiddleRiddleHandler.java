package level.devlevel.riddleHandler;
import components.TorchComponent;
import core.Entity;
import core.Game;
import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import level.utils.ITickable;

public class TorchRiddleRiddleHandler implements ITickable {

    public static final int UPPER_RIDDLE_BOUND = 15;
    public static final int LOWER_RIDDLE_BOUND = 5;

    private final Entity riddleSign; // Optional: für spätere Dialoge
    private final TileLevel level;
    private final Coordinate riddleDoor;
    private final Coordinate[] riddleRoomBounds;
    private final Coordinate riddleCenter;
    private int riddleSearchedSum;
    private boolean rewardGiven = false;
    private final Random random = new Random();

    public TorchRiddleRiddleHandler(List<Coordinate> customPoints, TileLevel level) {
        this.level = level;
        this.riddleDoor = customPoints.getFirst();
        this.riddleRoomBounds = new Coordinate[] {customPoints.get(1), customPoints.get(2)};
        this.riddleCenter = customPoints.get(3);
        this.riddleSign = null;

        this.riddleSearchedSum =
            IntStream.range(0, random.nextInt(3) + 1)
                .map(i -> random.nextInt(UPPER_RIDDLE_BOUND - LOWER_RIDDLE_BOUND))
                .sum()
                + LOWER_RIDDLE_BOUND;
    }

    public void setRiddleSolution(List<Integer> numbers) {
        this.riddleSearchedSum = numbers.stream()
            .mapToInt(Integer::intValue)
            .sum();
    }

    public int getSumOfLitTorches() {
        return Game.entityStream()
            .filter(e -> e.isPresent(TorchComponent.class))
            .map(e -> e.fetch(TorchComponent.class))
            .flatMap(Optional::stream)
            .filter(TorchComponent::lit)
            .mapToInt(TorchComponent::value)
            .sum();
    }

    @Override
    public void onTick(boolean isPaused) {
        int sum = getSumOfLitTorches();
        System.out.println("Fackelsumme: " + sum + " | Ziel: " + riddleSearchedSum);

        if (!rewardGiven && sum == riddleSearchedSum) {
            System.out.println("Rätsel gelöst – Tür öffnet sich");
            level.tileAt(riddleDoor);
            rewardGiven = true;
        }
    }
}
