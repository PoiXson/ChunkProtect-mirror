package com.poixson.chunkprotect;


public enum AreaShape {

	CIRCLE,
	SQUARE;



	public static AreaShape Get(final String str) {
		if (str == null)
			return null;
		final String lower = str.toLowerCase();
		switch (lower) {
		case "circle": return CIRCLE;
		case "square": return SQUARE;
		}
		if (lower.startsWith("c")) return CIRCLE;
		if (lower.startsWith("s")) return SQUARE;
		return null;
	}



	@Override
	public String toString() {
		switch(this) {
		case CIRCLE: return "circle";
		case SQUARE: return "square";
		}
		return null;
	}



}
