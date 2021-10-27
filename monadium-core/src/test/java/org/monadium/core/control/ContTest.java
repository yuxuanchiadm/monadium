package org.monadium.core.control;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.monadium.core.control.Cont;
import static org.monadium.core.control.Cont.*;

import static org.monadium.core.Notation.*;
import static org.monadium.core.control.Cont.Notation.*;

public class ContTest {
	@Test public void testBasic() {
		assertEquals(1, evalCont(new Object() {
			<R> Cont<R, Integer> one() {
				return cont(1);
			}
		}.one()));
		assertEquals(2, runCont(new Object() {
			<R> Cont<R, Integer> two() {
				return cont(2);
			}
		}.two()).apply(i -> i));
	}
	@Test public void testLabel() {
		assertEquals(12450, evalCont(new Object() {
			<R> Cont<R, Integer> test() {
				return label(exit -> $do(
				$(	exit.apply(12450)	, () ->
				$(	cont(500)			))
				));
			}
		}.test()));
	}
	@Test public void testDelimited() {
		assertEquals(100, evalCont(new Object() {
			<R> Cont<R, Integer> test() {
				return $do(
				$(	reset($do(
					$(	foo()			, x ->
					$(	cont(x + 25)	)))
					)							, x ->
				$(	cont(x + 25)				))
				);
			}
			<R> Cont<R, Integer> foo() {
				return shift(exit -> $do(
					$(	exit.apply(50)	, () ->
					$(	cont(500)		))
				));
			}
		}.test()));
	}
}
