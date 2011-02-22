package featureide.fm.util;

public class StopWatch {
	enum State {
		UnStarted, Running, Stoped
	}

	private long start;
	private long stop;
	private State state;

	public StopWatch() {
		state = State.UnStarted;
	}

	public void start() {
		state = State.Running;
		start = System.nanoTime();
	}

	public void stop() {
		state = State.Stoped;
		stop = System.nanoTime();
	}

	public void reset() {
		state = State.UnStarted;
		start = stop = 0;
	}

	/**
	 * in milliseconds
	 */
	public long getElapsedTime() {
		if (state != State.Stoped)
			throw new RuntimeException("StopWatch is still running!");
		return (stop - start);
	}

	public String getElapsedTimeString() {
		if (state != State.Stoped)
			throw new RuntimeException("StopWatch is still running!");
		long time = stop - start;
		return getTimeLongString(time);
	}

	public static String getTimeString(long dur) {
		if (dur < 1000000)
			return dur + "nsec";
		dur /= 1000000;
		if (dur < 1000)
			return dur + "msec";
		dur /= 1000;
		if (dur < 60)
			return dur + "sec";
		dur /= 60;
		if (dur < 60)
			return dur + "min";
		dur /= 60;
		return dur + "h";
	}

	public static String getTimeLongString(long dur) {
		long ns, ms, sec, min, hour;

		ns = dur % 1000000;
		dur /= 1000000;
		ms = dur % 1000;
		dur /= 1000;
		sec = dur % 60;
		dur /= 60;
		min = dur % 60;
		dur /= 60;
		hour = dur;

		return String.format("%02d:%02d:%02dsec %d.%06dms", hour, min, sec, ms,
				ns);
	}
}
