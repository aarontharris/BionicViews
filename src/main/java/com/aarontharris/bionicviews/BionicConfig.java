package com.aarontharris.bionicviews;

public class BionicConfig {
	private static boolean debug = true;

	public static void setDebug( boolean isDebug ) {
		BionicConfig.debug = isDebug;
	}

	public static boolean isDebug() {
		return debug;
	}
}
