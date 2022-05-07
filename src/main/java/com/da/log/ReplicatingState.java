package com.da.log;

public class ReplicatingState {

    private int nextIndex;
    private int matchIndex;

    public ReplicatingState(int nextIndex) {
        this(nextIndex, 0);
    }

    ReplicatingState(int nextIndex, int matchIndex) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    /**
     * Advance next index and match index by last entry index.
     *
     * @param lastEntryIndex last entry index
     * @return true if advanced, false if no change
     */
    public boolean advance(int lastEntryIndex) {
        // changed
        boolean result = (matchIndex != lastEntryIndex || nextIndex != (lastEntryIndex + 1));

        matchIndex = lastEntryIndex;
        nextIndex = lastEntryIndex + 1;

        return result;
    }

    /**
     * Back off next index, in other word, decrease.
     *
     * @return true if decrease successfully, false if next index is less than or equal to {@code 1}
     */
    public boolean backOffNextIndex() {
        if (nextIndex > 1) {
            nextIndex--;
            return true;
        }
        return false;
    }
}
