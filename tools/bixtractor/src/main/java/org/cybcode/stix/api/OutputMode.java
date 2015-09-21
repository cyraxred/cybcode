package org.cybcode.stix.api;

public enum OutputMode {
	REGULAR {
		public boolean isPush() { return false; }
	},
	PUSH_ONCE,
	PUSH_MANY {
		public boolean isRepeatable() { return true; }
	};
	
	public boolean isRepeatable() { return false; }
	public boolean isPush() { return true; }
	
	public static OutputMode pushMode(boolean repeatable)
	{
		return repeatable ? PUSH_MANY : PUSH_ONCE;
	}
	
	public static OutputMode valueOf(boolean repeatable)
	{
		return repeatable ? PUSH_MANY : REGULAR;
	}
}
