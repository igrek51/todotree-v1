package igrek.todotree.logger;

enum LogLevel {
	
	OFF(0), //only for settings
	
	FATAL(10),
	
	ERROR(20),
	
	WARN(30),
	
	INFO(40),
	
	DEBUG(50),
	
	TRACE(60),
	
	ALL(1000); //only for settings
	
	/** lower number - higher priority (more important) */
	private int levelNumber;
	
	LogLevel(int levelNumber) {
		this.levelNumber = levelNumber;
	}
	
	public boolean lowerOrEqual(LogLevel level2) {
		return levelNumber <= level2.levelNumber;
	}
	
	public boolean higherOrEqual(LogLevel level2) {
		return levelNumber >= level2.levelNumber;
	}
	
}
