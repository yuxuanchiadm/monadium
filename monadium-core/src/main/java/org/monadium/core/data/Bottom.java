package org.monadium.core.data;

public enum Bottom {
	;

	static <A> A absurd(Bottom b) { return switch (b) { case default -> undefined(); }; }

	static <A, B> B unsafeCoerce(A a) { @SuppressWarnings("unchecked") B b = (B) a; return b; }
	static class Undefined extends RuntimeException {}
	static <A> A undefined() throws Undefined { throw new Undefined(); }
}
