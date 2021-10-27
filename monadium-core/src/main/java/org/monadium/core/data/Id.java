package org.monadium.core.data;

public sealed interface Id<A, B> {
	record Refl<A>() implements Id<A, A> {
		@Override public Id<A, A> sym() { return refl(); }
		@Override public <C> Id<A, C> trans(Id<A, C> id) { return id; }
		@Override public A coerce(A a) { return a; }
	}

	static <A> Id<A, A> refl() { return new Refl<>(); }

	Id<B, A> sym();
	<C> Id<A, C> trans(Id<B, C> id);
	B coerce(A a);
}
