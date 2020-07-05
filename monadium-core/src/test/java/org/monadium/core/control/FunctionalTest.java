package org.monadium.core.control;

import org.monadium.core.control.Functional;
import static org.monadium.core.control.Functional.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.stream.IntStream;

public class FunctionalTest {
	@Test public void testBasic() {
		assertThrows(NullPointerException.class, () -> fix(self -> null).apply(0));
		assertThrows(StackOverflowError.class, () -> fix(self -> self).apply(0));
		assertThrows(StackOverflowError.class, () -> fix(self -> { self.apply(0); return self; }).apply(0));
		assertTimeout(Duration.ofSeconds(1), () -> fix(self -> null));
		assertTimeout(Duration.ofSeconds(1), () -> fix(self -> self));
		assertTimeout(Duration.ofSeconds(1), () -> fix(self -> { self.apply(0); return self; }));
		assertEquals(IntStream.rangeClosed(0, 16).sum(), Functional.<Integer, Integer> fix(self -> x ->
			x >= 0 ? x + self.apply(x - 1) : 0
		).apply(16));
	}
}
