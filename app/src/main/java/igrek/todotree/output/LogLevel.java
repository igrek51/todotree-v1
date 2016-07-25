package igrek.todotree.output;

public enum LogLevel {

    OFF(0),

    ERROR(1),

    WARN(2),

    INFO(3),

    DEBUG(4);

    private int levelNumber;

    LogLevel(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getLevelNumber() {
        return levelNumber;
    }
}
