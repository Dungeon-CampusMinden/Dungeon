package minigame;

public class Minigame {

    private static final String[] ANIMATION_FRAMES = {};
    private static final long PICKING_STATES = 0x555aa55500000000l;
    private static final byte FINISHED = 0b111;
    private static final byte PICKING_STATES_AMOUNT = 32;
    private byte statePointer;
    private byte animationPointer;
    private byte picked;
    private byte pickedCount;

    public void up() {
        statePointer = (byte) (++statePointer % PICKING_STATES_AMOUNT);
        animationPointer = (byte) (++animationPointer % ANIMATION_FRAMES.length);
    }

    public void down() {
        statePointer = (byte) ((PICKING_STATES_AMOUNT + --statePointer) % PICKING_STATES_AMOUNT);
        animationPointer = (byte) ((ANIMATION_FRAMES.length + --animationPointer) % ANIMATION_FRAMES.length);
    }

    public byte getStatePointer() {
        return statePointer;
    }

    public String getAnimationFrame() {
        return ANIMATION_FRAMES[animationPointer];
    }

    public void randomizeStatePointer() {
        statePointer = (byte) (Math.random() * (PICKING_STATES_AMOUNT / 2));
    }

    public void push() {
        picked = (byte) (picked | ((((PICKING_STATES >>> (statePointer * 2)) & 0b11) == 2 ? 1 : 0) << pickedCount));
        if ((picked >>> pickedCount) == 1) {
            pickedCount++;
            randomizeStatePointer();
        } else {
            reset();
        }
    }

    public boolean isFinished() {
        return picked == FINISHED;
    }

    private void reset() {
        picked = 0;
        pickedCount = 0;
        randomizeStatePointer();
        // !: Also close the minigame
    }

    public byte finishAmount() {
        byte result = 0;
        for (byte i = FINISHED; i != 0; i = (byte) (i & (i - 1)))
            result++;
        return result;
    }

}
