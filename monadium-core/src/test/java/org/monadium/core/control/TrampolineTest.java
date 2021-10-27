package org.monadium.core.control;

import java.time.Duration;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.monadium.core.control.Trampoline;
import static org.monadium.core.control.Trampoline.*;
import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;

import static org.monadium.core.Notation.*;
import static org.monadium.core.control.Trampoline.Notation.*;

public class TrampolineTest {
	@Test public void testBasic() {
		assertEquals(1, new Object() {
			Trampoline<Integer> one() {
				return done(1);
			}
		}.one().run());
		assertEquals(2, new Object() {
			Trampoline<Integer> two() {
				return more(() -> done(2));
			}
		}.two().run());
	}
	@Test public void testRecursion() {
		assertEquals(832040, new Object() {
			Trampoline<Integer> fib(int i) {
				return i <= 1 ? done(i) : $do(
				$(	more(() -> fib(i - 1))	, x ->
				$(	more(() -> fib(i - 2))	, y ->
				$(	done(x + y)				)))
				);
			}
		}.fib(30).run());
		assertEquals(2147450880, new Object() {
			Trampoline<Integer> sum(int i) {
				return i <= 0 ? done(i) : $do(
				$(	more(() -> sum(i - 1))	, x ->
				$(	done(i + x)				))
				);
			}
		}.sum(65535).run());
		assertTimeout(Duration.ofSeconds(1), () -> new Object() {
			Trampoline<Integer> fibTail(int i, int a, int b) {
				return i <= 0 ? done(a)
					: i <= 1 ? done(b)
					: more(() -> fibTail(i - 1, b, a + b));
			}
		}.fibTail(65535, 0, 1).run());
	}
	@Test public void testInterrupt() {
		Thread.currentThread().interrupt();
		assertThrows(InterruptedException.class, () -> new Object() {
			Trampoline<Bottom> loop() {
				return more(() -> loop());
			}
		}.loop().interruptibleRun());
	}
}
