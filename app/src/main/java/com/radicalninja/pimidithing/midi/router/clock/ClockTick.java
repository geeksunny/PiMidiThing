package com.radicalninja.pimidithing.midi.router.clock;

public class ClockTick {

    public static final int DEFAULT_PPQN = 24;
    public static final int DEFAULT_PATTERN_LENGTH = 16;

    private final int position;
    private final int ppqn;
    private final int patternLength;

    /**
     * Create a ClockTick with a PPQN of 24 and Pattern Length of 16 quarternotes.
     * @param position - Position in the sequence.
     */
    public ClockTick(final int position) {
        this(position, DEFAULT_PPQN, DEFAULT_PATTERN_LENGTH);
    }

    /**
     * Create a ClockTick with a Pattern Length of 16 quarternotes.
     * @param position - Position in the sequence.
     * @param ppqn - Clock resolution in Pulses Per Quarter Note.
     */
    public ClockTick(final int position, final int ppqn) {
        this(position, ppqn, DEFAULT_PATTERN_LENGTH);
    }

    /**
     *
     * @param position - Position in the sequence.
     * @param ppqn - Clock resolution in Pulses Per Quarter Note.
     * @param patternLength - Number of quarternotes per pattern.
     */
    public ClockTick(final int position, final int ppqn, final int patternLength) {
        this.position = position;
        this.ppqn = ppqn;
        this.patternLength = patternLength;
    }

    public int getPulse() {
        return position % ppqn;
    }

    public int getWholeNote() {
        return position / (ppqn * 4);
    }

    public boolean isWholeNote() {
        return (position % (ppqn * 4)) == 0;
    }

    public int getPatternWholeNote() {
        // TODO!! Use patternLength here
        return 0;
    }

    public int getHalfNote() {
        return position / (ppqn * 2);
    }

    public boolean isHalfNote() {
        return (position % (ppqn * 2)) == 0;
    }

    public int getPatternHalfNote() {
        // TODO!! Use patternLength here
        return 0;
    }

    public int getQuarterNote() {
        return position / ppqn;
    }

    public boolean isQuarterNote() {
        return (position % ppqn) == 0;
    }

    public int getPatternQuarterNote() {
        // TODO!! Use patternLength here
        return 0;
    }

    public int getEighthNote() {
        return position / (ppqn / 2);
    }

    public boolean isEighthNote() {
        return (position % (ppqn / 2)) == 0;
    }

    public int getPatternEighthNote() {
        // TODO!! Use patternLength here
        return 0;
    }

    public int getSixteenthNote() {
        return position / (ppqn / 4);
    }

    public boolean isSixteenthNote() {
        return (position % (ppqn / 4)) == 0;
    }

    public int getPatternSixteenthNote() {
        // TODO!! Use patternLength here
        return 0;
    }

}
