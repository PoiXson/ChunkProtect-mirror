package com.poixson.chunkprotect;

import org.bukkit.potion.PotionEffect;


public class Utils {
	private Utils() {}



	public static boolean PotionEffectEquals(final PotionEffect effectA, final PotionEffect effectB) {
		if (effectA == null || effectB == null) {
			return (effectA == null && effectB == null);
		}
		return effectA.equals(effectB);
	}



}
