package featureide.fm.util;

public class TimedWoker {
	StopWatch stopWatch = new StopWatch();

	public void run() {
		stopWatch.start();
		excute();
		stopWatch.stop();
	}

	public StopWatch getStopWatch() {
		return stopWatch;
	}

	public void excute() {
	}

}