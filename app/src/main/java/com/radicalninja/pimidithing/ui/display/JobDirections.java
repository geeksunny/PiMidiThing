package com.radicalninja.pimidithing.ui.display;

/**
 * Predefined JobDirection handlers.
 */
@SuppressWarnings("WeakerAccess")
public class JobDirections {

    /**
     * No job movement will take place.
     */
    public static final JobDirection NONE = new JobDirection() {
        @Override
        public int getCycleLength(final int itemCount) {
            return 1;
        }

        @Override
        public int getFirstIndex(final int itemCount) {
            return 0;
        }

        @Override
        public int getStepLength() {
            return 0;
        }

        @Override
        public int nextIndex(final int currentIndex, final int itemCount) {
            return currentIndex;
        }
    };

    /**
     * Default JobDirection. Job steps move forward.
     */
    public static final JobDirection FORWARD = new JobDirection() {
        @Override
        public int getCycleLength(final int itemCount) {
            return itemCount;
        }

        @Override
        public int getFirstIndex(final int itemCount) {
            return 0;
        }

        @Override
        public int getStepLength() {
            return 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int itemCount) {
            return (currentIndex == itemCount) ? 0 : currentIndex + 1;
        }
    };

    /**
     * Job steps will move backwards in reverse order.
     */
    public static final JobDirection REVERSE = new JobDirection() {
        @Override
        public int getCycleLength(final int itemCount) {
            return itemCount;
        }

        @Override
        public int getFirstIndex(final int itemCount) {
            return itemCount - 1;
        }

        @Override
        public int getStepLength() {
            return -1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int itemCount) {
            return (currentIndex == 0) ? itemCount : currentIndex - 1;
        }
    };

    /**
     * Job steps move forward. When the job hits the end, it will reverse direction.
     */
    public static final JobDirection PINGPONG = new JobDirection() {
        private int offset = 1;

        @Override
        public int getCycleLength(final int itemCount) {
            offset = 1;
            return itemCount * 2;
        }

        @Override
        public int getFirstIndex(final int itemCount) {
            return 0;
        }

        @Override
        public int getStepLength() {
            return offset;
        }

        @Override
        public int nextIndex(final int currentIndex, final int itemCount) {
            if (itemCount == 0) {
                return currentIndex;
            }
            final int nextIndex = currentIndex + offset;
            if (nextIndex > itemCount) {
                offset = -1;
                return currentIndex - 1;
            } else if (nextIndex < 0) {
                offset = 1;
                return 1;
            } else {
                return nextIndex;
            }
        }
    };

}