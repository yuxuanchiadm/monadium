package org.monadium.core.data;

public record Unit() {
	static final Unit SINGLETON = new Unit();
	public static Unit unit() { return SINGLETON; }
}
