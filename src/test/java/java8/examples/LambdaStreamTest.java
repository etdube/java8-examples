package java8.examples;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;

import java8.examples.util.Chronometer;
import java8.examples.util.RandomCharGenerator;

public class LambdaStreamTest {
	private static final Logger logger = LoggerFactory.getLogger(LambdaStreamTest.class);

	// *** Lambda function ***

	@Test
	public void testLambdaFunction() {
		Function<String, String> helloFunc = name -> String.format("Hello %s!", name);

		assertThat(helloFunc.apply("Lambda"), is("Hello Lambda!"));
	}

	// *** Lambda functions and streams ***

	@Test
	public void testLambdaWithListStream() {
		Function<String, String> helloFunc = name -> String.format("Hello %s!", name);

		final List<String> names = ImmutableList.of("Athos", "Porthos", "Aramis");

		List<String> helloNames = names.stream().map(helloFunc).collect(Collectors.toList());

		assertThat(helloNames, contains("Hello Athos!", "Hello Porthos!", "Hello Aramis!"));

		// The lambda syntax is equivalent to the following anonymous class definition:
		Function<String, String> helloFuncAnonClass = new Function<String, String>() {
			@Override
			public String apply(String name) {
				return String.format("Hello %s!", name);
			}
		};

		helloNames = names.stream().map(helloFuncAnonClass).collect(Collectors.toList());

		assertThat(helloNames, contains("Hello Athos!", "Hello Porthos!", "Hello Aramis!"));

		// It is common to specify the lambda "in-line" as parameter of the map() operation:
		helloNames = names.stream().map(name -> String.format("Hello %s!", name)).collect(Collectors.toList());

		assertThat(helloNames, contains("Hello Athos!", "Hello Porthos!", "Hello Aramis!"));

		// Equivalent procedural example:
		helloNames = new ArrayList<>();
		for (String name : names) {
			helloNames.add(String.format("Hello %s!", name));
		}

		assertThat(helloNames, contains("Hello Athos!", "Hello Porthos!", "Hello Aramis!"));

		// More fancy example with multiple operations:
		String concatenatedHellos = names.stream()
				.filter(name -> name.startsWith("A"))
				.sorted()
				.map(name -> String.format("Hello %s!", name))
				.collect(Collectors.joining(" ; "));

		assertThat(concatenatedHellos, is("Hello Aramis! ; Hello Athos!"));

		// Equivalent procedural example:
		List<String> filteredNames = new ArrayList<>();
		for (String name : names) {
			if (name.startsWith("A")) {
				filteredNames.add(name);
			}
		}

		Collections.sort(filteredNames); // original list is modified

		List<String> helloNames2 = new ArrayList<>();
		for (String name : filteredNames) {
			helloNames2.add(String.format("Hello %s!", name));
		}

		// String.join is new in Java 8, the old way (without Guava) would have been even longer:
		concatenatedHellos = String.join(" ; ", helloNames2.toArray(new String[] {}));

		assertThat(concatenatedHellos, is("Hello Aramis! ; Hello Athos!"));
	}

	// *** Method references ***

	private static final class StringQuoter {

		private final String quoteSymbol;

		public StringQuoter(char quoteChar) {
			this.quoteSymbol = String.valueOf(quoteChar);
		}

		public String quote(String input) {
			return quoteSymbol + input.replace(quoteSymbol, "\\" + quoteSymbol) + quoteSymbol;
		}

		public static String quoteWithDoubleQuotes(String input) {
			return "\"" + input.replace("\"", "\\\"") + "\"";
		}
	}

	@Test
	public void testMethodReference() {
		final List<String> incantations = ImmutableList.of("hocus pocus", "abracadabra", "devil's song");

		final StringQuoter quoter = new StringQuoter('\'');

		List<String> singleQuotedIncantations = incantations.stream().map(quoter::quote).collect(Collectors.toList());

		assertThat(singleQuotedIncantations, contains("'hocus pocus'", "'abracadabra'", "'devil\\'s song'"));

		// This is equivalent to the lambda notation:
		singleQuotedIncantations = incantations.stream().map(s -> quoter.quote(s)).collect(Collectors.toList());

		assertThat(singleQuotedIncantations, contains("'hocus pocus'", "'abracadabra'", "'devil\\'s song'"));

		// Also works with static methods:
		final List<String> doubleQuotedIncantations = incantations.stream()
				.map(StringQuoter::quoteWithDoubleQuotes)
				.collect(Collectors.toList());

		assertThat(doubleQuotedIncantations, contains("\"hocus pocus\"", "\"abracadabra\"", "\"devil's song\""));
	}

	// *** Collectors ***

	private static final class Musketeer {

		private final String _name;
		private final double _height;
		private final String _sword;

		public Musketeer(String name, double height, String sword) {
			_name = name;
			_height = height;
			_sword = sword;
		}

		public String getName() {
			return _name;
		}

		@Override
		public int hashCode() {
			return Objects.hash(_name, _height, _sword);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Musketeer)) {
				return false;
			}
			final Musketeer rhs = (Musketeer) o;
			return Objects.equals(_name, rhs._name) && Objects.equals(_height, rhs._height)
					&& Objects.equals(_sword, rhs._sword);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("name", _name)
					.add("height", _height)
					.add("sword", _sword)
					.toString();
		}
	}

	@Test
	public void testCollectingToMap() {
		final List<Musketeer> musketeers = ImmutableList.of(new Musketeer("Athos", 1.75, "Excalibur"),
				new Musketeer("Porthos", 1.6, "Needle"), new Musketeer("Aramis", 1.90, "Katana"));

		Map<String, Musketeer> musketeerByName = musketeers.stream()
				.collect(Collectors.toMap(m -> m.getName(), m -> m));

		final Map<String, Musketeer> expected = ImmutableMap.<String, Musketeer>builder()
				.put("Athos", new Musketeer("Athos", 1.75, "Excalibur"))
				.put("Porthos", new Musketeer("Porthos", 1.6, "Needle"))
				.put("Aramis", new Musketeer("Aramis", 1.90, "Katana"))
				.build();

		assertThat(musketeerByName, is(expected));

		// Guava offers something shorter (without using streams) if you don't need to transform the values:
		musketeerByName = Maps.uniqueIndex(musketeers, m -> m.getName());

		assertThat(musketeerByName, is(expected));
	}

	// *** Parallel streams ***

	@Test
	public void testParallelStream() {
		final int count = 10_000_000;
		final int stringLength = 100;

		final Chronometer chrono = new Chronometer();

		// Generate 10 000 000 random strings of 100 lowercase letters each:
		chrono.start();
		final List<String> bigRandomAlphaStrings = IntStream.range(0, count)
				.parallel()
				.mapToObj(i -> RandomCharGenerator.randomCharString(stringLength))
				.collect(Collectors.toList());
		chrono.stop();
		logger.info("Generate random strings, duration in milliseconds: {}", chrono.getDuration().toMillis());

		// Keep only strings that have all of these letters: a, b, g, h, w, z (single-threaded):
		chrono.start();
		final List<String> filteredStrings1 = bigRandomAlphaStrings.stream()
				.filter(s -> containsSubsequences(s, "a", "b", "g", "h", "w", "z"))
				.collect(Collectors.toList());
		chrono.stop();
		logger.info("Single-threaded stream, duration in milliseconds: {}", chrono.getDuration().toMillis());

		assertThat(filteredStrings1.size(), Matchers.lessThanOrEqualTo(bigRandomAlphaStrings.size()));

		// Keep only strings that have all of these letters: a, b, g, h, w, z (parallelized):
		chrono.start();
		final List<String> filteredStrings2 = bigRandomAlphaStrings.parallelStream()
				.filter(s -> containsSubsequences(s, "a", "b", "g", "h", "w", "z"))
				.collect(Collectors.toList());
		chrono.stop();
		logger.info("Parallel stream, duration in milliseconds: {}", chrono.getDuration().toMillis());

		assertThat(filteredStrings2, is(filteredStrings1));
	}

	private boolean containsSubsequences(String input, CharSequence... subsequences) {
		return Arrays.stream(subsequences).allMatch(subSeq -> input.contains(subSeq));
	}

	// Turns out to be slower for the sample data...
	private boolean containsSubsequencesParallel(String input, CharSequence... subsequences) {
		return Arrays.stream(subsequences).parallel().allMatch(subSeq -> input.contains(subSeq));
	}

	// *** Using functional interfaces ***

	private static final class VectorOp {
		public static List<Integer> crunch(List<Integer> e1, List<Integer> e2,
				BiFunction<Integer, Integer, Integer> operation) {
			return Streams.zip(e1.stream(), e2.stream(), operation).collect(Collectors.toList());
		}
	}

	private static final class Operators {
		public static Integer add(Integer a, Integer b) {
			return a + b;
		}

		public static Integer multiply(Integer a, Integer b) {
			return a * b;
		}
	}

	@Test
	public void testFunctionalInterface() {
		final List<Integer> seqA = ImmutableList.of(1, 2, 3);
		final List<Integer> seqB = ImmutableList.of(4, 5, 6);

		List<Integer> result = VectorOp.crunch(seqA, seqB, (a, b) -> a + b);

		assertThat(result, contains(5, 7, 9));

		// Also works with method references:
		result = VectorOp.crunch(seqA, seqB, Operators::add);

		assertThat(result, contains(5, 7, 9));

		result = VectorOp.crunch(seqA, seqB, Operators::multiply);

		assertThat(result, contains(4, 10, 18));
	}

}
