package java8.examples.util;

import java.time.Duration;

public class Chronometer {
	private long start;
	private long finish;
	private boolean started = false;

	public void start() {
		start = System.nanoTime();
		started = true;
	}

	public void stop() {
		if (!started) {
			throw new IllegalStateException("Not started");
		}

		finish = System.nanoTime();
		started = false;
	}

	public Duration getDuration() {
		return Duration.ofNanos(finish - start);
	}
}