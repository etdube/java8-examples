package java8.examples.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomCharGenerator {
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

	private RandomCharGenerator() {
		throw new AssertionError();
	}

	public static char randomChar() {
		return ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length()));
	}

	public static String randomCharString(int length) {
		return IntStream.range(0, length).mapToObj(i -> String.valueOf(randomChar())).collect(Collectors.joining());
	}
}
