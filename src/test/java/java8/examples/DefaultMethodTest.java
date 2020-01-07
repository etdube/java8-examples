package java8.examples;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DefaultMethodTest {

	private interface Crunchable {
		String crunch();

		default String describe() {
			return "A delicious crunchy thing";
		}
	}

	private interface Chewable {
		String chew();

		default String describe() {
			return "A delectable chewy thing";
		}
	}

	public static class Peanut implements Crunchable {
		@Override
		public String crunch() {
			return "You now have peanut chunks in your teeth";
		}

		@Override
		public String describe() {
			return "I'm actually a bean, not a nut";
		}
	}

	@Test
	public void testWithPeanut() {
		Peanut peanut = new Peanut();

		assertThat(peanut.crunch(), is("You now have peanut chunks in your teeth"));
		assertThat(peanut.describe(), is("I'm actually a bean, not a nut"));
	}

	public static class PotatoChip implements Crunchable {
		@Override
		public String crunch() {
			return "This would be better with beer";
		}
	}

	@Test
	public void testWithPotatoChip() {
		PotatoChip potatoChip = new PotatoChip();

		assertThat(potatoChip.crunch(), is("This would be better with beer"));
		assertThat(potatoChip.describe(), is("A delicious crunchy thing"));
	}

	public abstract static class ChewingGum {
		public String describe() {
			return "Chew until it doesn't taste anything";
		}
	}

	public static class BubbleGum extends ChewingGum implements Chewable {
		@Override
		public String chew() {
			return "Don't forget to blow a bubble";
		}
	}

	@Test
	public void testWithBubbleGum() {
		BubbleGum bubbleGum = new BubbleGum();

		assertThat(bubbleGum.chew(), is("Don't forget to blow a bubble"));

		// Method from abstract base class takes precedence over interface default method:
		assertThat(bubbleGum.describe(), is("Chew until it doesn't taste anything"));
	}

	public static class PeanutWithBubbleGum implements Crunchable, Chewable {
		@Override
		public String chew() {
			return "Don't swallow it";
		}

		@Override
		public String crunch() {
			return "Yuck";
		}

		// Mandatory, otherwise compile fails since it doesn't know which of the two default
		// methods with the same name and signature to use...
		@Override
		public String describe() {
			return "Who thought that this would be a good idea...";

			// If we want to delegate to one of the default method implementations:
			// return Crunchable.super.describe();
		}
	}

	@Test
	public void testWithPeanutWithBubbleGum() {
		PeanutWithBubbleGum peanutWithBubbleGum = new PeanutWithBubbleGum();

		assertThat(peanutWithBubbleGum.chew(), is("Don't swallow it"));
		assertThat(peanutWithBubbleGum.crunch(), is("Yuck"));
		assertThat(peanutWithBubbleGum.describe(), is("Who thought that this would be a good idea..."));
	}
}
