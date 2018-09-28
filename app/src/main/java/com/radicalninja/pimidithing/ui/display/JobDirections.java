package com.radicalninja.pimidithing.ui.display;

public class JobDirections {

    public static final JobDirection NONE = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            return currentIndex;
        }
    };

    public static final JobDirection FORWARD = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return lastIndex + 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            return (currentIndex == lastIndex) ? 0 : currentIndex + 1;
        }
    };

    public static final JobDirection REVERSE = new JobDirection() {
        @Override
        public int totalIndexes(final int lastIndex) {
            return lastIndex + 1;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            return (currentIndex == 0) ? lastIndex : currentIndex - 1;
        }
    };

    public static final JobDirection PINGPONG = new JobDirection() {
        private int offset = 1;

        @Override
        public int totalIndexes(final int lastIndex) {
            offset = 1;
            return (lastIndex + 1) * 2;
        }

        @Override
        public int nextIndex(final int currentIndex, final int lastIndex) {
            if (lastIndex == 0) {
                return currentIndex;
            }
            final int nextIndex = currentIndex + offset;
            if (nextIndex > lastIndex) {
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