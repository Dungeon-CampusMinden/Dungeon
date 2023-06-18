package minigame;

import java.util.logging.Logger;

public class Minigame {

    private final Logger minigameLogger = Logger.getLogger(Minigame.class.getName());

    private static final String[] ANIMATION_FRAMES = {
            "minigame/Lock_0.png",
            "minigame/Lock_1.png",
            "minigame/Lock_2.png",
            "minigame/Lock_3.png",
            "minigame/Lock_4.png",
            "minigame/Lock_5.png",
            "minigame/Lock_6.png",
            "minigame/Lock_7.png",
            "minigame/Lock_8.png",
            "minigame/Lock_9.png",
            "minigame/Lock_10.png",
            "minigame/Lock_11.png",
            "minigame/Lock_12.png",
    };
    private static final long PICKING_STATES = 0x5565550000000000l;
    private static final byte FINISHED = 0b111;
    private static final byte PICKING_STATES_AMOUNT = 32;
    private byte statePointer;
    private byte animationPointer;
    private byte picked;
    private byte pickedCount;

    /**
     * Creates a new {@code Minigame} with randomized starting state
     */
    public Minigame() {
        randomizeStatePointer();
        minigameLogger.info("Initial state pointer is: " + statePointer);
    }

    /**
     * Moves the state pointer "up"
     */
    public void up() {
        statePointer = (byte) (++statePointer % PICKING_STATES_AMOUNT);
        minigameLogger.info("State pointer is: " + statePointer);
        animationPointer = (byte) (++animationPointer % ANIMATION_FRAMES.length);
        minigameLogger.info("Animation pointer is: " + animationPointer);
    }

    /**
     * Moves the state pointer "down"
     */
    public void down() {
        statePointer = (byte) ((PICKING_STATES_AMOUNT + --statePointer) % PICKING_STATES_AMOUNT);
        minigameLogger.info("State pointer is: " + statePointer);
        animationPointer = (byte) ((ANIMATION_FRAMES.length + --animationPointer) % ANIMATION_FRAMES.length);
        minigameLogger.info("Animation pointer is: " + animationPointer);
    }

    public byte getStatePointer() {
        return statePointer;
    }

    /**
     * Returns the frame at the current position
     * 
     * @return the frame at the current position
     */
    public String getAnimationFrame() {
        return ANIMATION_FRAMES[animationPointer];
    }

    public byte getPickedCount() {
        return pickedCount;
    }

    /**
     * Places the {@code statePointer} at a random {@code 0} state
     */
    public void randomizeStatePointer() {
        statePointer = (byte) (Math.random() * (PICKING_STATES_AMOUNT / 2));
        minigameLogger.info("State pointer is: " + statePointer);
    }

    /**
     * Sets the current pin if at a correct state
     * 
     * @return {@code true} if the current state is {@code 2} else {@code false}
     */
    public boolean push() {
        picked = (byte) (picked | ((((PICKING_STATES >>> (statePointer * 2)) & 0b11) == 2 ? 1 : 0) << pickedCount));
        minigameLogger.info("State :" + ((PICKING_STATES >>> (statePointer * 2)) & 0b11));
        if ((picked >>> pickedCount) == 1) {
            pickedCount++;
            randomizeStatePointer();
            minigameLogger.info("Right pick");
        } else {
            reset();
            minigameLogger.info("Wrong pick");
            return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if all pins are set correctly else {@code false}
     * 
     * @return {@code true} if all pins are set correctly else {@code false}
     */
    public boolean isFinished() {
        return picked == FINISHED;
    }

    /**
     * Resets all pins and randomizes the state
     */
    private void reset() {
        picked = 0;
        pickedCount = 0;
        randomizeStatePointer();
        minigameLogger.info("State pointer is: " + statePointer);
    }

    /**
     * Returns the amount of pins to be picked
     * 
     * @return the amount of pins to be picked
     */
    public byte finishAmount() {
        byte result = 0;
        for (byte i = FINISHED; i != 0; i = (byte) (i & (i - 1)))
            result++;
        return result;
    }

    /**
     * Retruns the current state
     * 
     * @return the current state
     */
    public byte currentState() {
        return (byte) ((PICKING_STATES >>> (statePointer * 2)) & 0b11);
    }

}
