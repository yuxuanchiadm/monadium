package org.monadium.core.data;

public enum Bottom {
	;

	public static <A> A absurd(Bottom b) { return switch (b) { case default -> undefined(); }; }

	public static <A, B> B unsafeCoerce(A a) { @SuppressWarnings("unchecked") B b = (B) a; return b; }
	public static class Undefined extends RuntimeException {}
	public static <A> A undefined() throws Undefined { throw new Undefined(); }
}
