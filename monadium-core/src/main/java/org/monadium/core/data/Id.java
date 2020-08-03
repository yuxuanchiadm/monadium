package org.monadium.core.data;

import org.monadium.core.data.Bottom;
import static org.monadium.core.data.Bottom.*;

public abstract class Id<A, B> {
	public static final class Refl<A> extends Id<A, A> {
		Refl() {}

		public interface Case<A, B, R> { R caseRefl(); }
		@Override public <R> R caseof(Refl.Case<A, A, R> caseRefl) { return caseRefl.caseRefl(); }

		@Override public Id<A, A> sym() { return refl(); }
		@Override public <C> Id<A, C> trans(Id<A, C> id) { return id; }
		@Override public A coerce(A a) { return a; }
	}

	Id() {}

	public interface Match<A, B, R> extends Refl.Case<A, B, R> {}
	public final <R> R match(Match<A, B, R> match) { return caseof(match); }
	public abstract <R> R caseof(Refl.Case<A, B, R> caseRefl);

	public static <A> Id<A, A> refl() { return new Refl<>(); }

	public static <A, B, R> R impossible(Id<A, B> id) throws Undefined { return undefined(); }

	public abstract Id<B, A> sym();
	public abstract <C> Id<A, C> trans(Id<B, C> id);
	public abstract B coerce(A a);
}
