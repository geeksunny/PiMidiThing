package com.radicalninja.pimidithing.ui.display;

import androidx.annotation.NonNull;

public class JobDirectionHandler {

    private final JobDirection jobDirection;

    // TODO: Implement logic for looping / shuffled indexes
    private boolean looping;
    private boolean shuffled;

    public JobDirectionHandler(@NonNull final JobDirection jobDirection,
                               final boolean looping,
                               final boolean shuffled) {

        if (null == jobDirection) {
            throw new IllegalArgumentException("JobDirection must not be null.");
        }
        this.jobDirection = jobDirection;
        this.looping = looping;
        this.shuffled = shuffled;
    }

    public int totalIndexes(final int lastIndex) {
        return jobDirection.totalIndexes(lastIndex);
    }

    public int nextIndex(final int currentIndex, final int lastIndex) {
        return jobDirection.nextIndex(currentIndex, lastIndex);
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(final boolean looping) {
        this.looping = looping;
    }

    public boolean isShuffled() {
        return shuffled;
    }

    public void setShuffled(final boolean shuffled) {
        this.shuffled = shuffled;
    }

}
