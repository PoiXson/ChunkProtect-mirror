package com.poixson.chunkprotect;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;


public final class Utils {
	private Utils() {}



	public static boolean EqualsPlayer(final Player playerA, final Player playerB) {
		if (playerA == null || playerB == null)
			return (playerA == null && playerB == null);
		return EqualsUUID(playerA.getUniqueId(), playerB.getUniqueId());
	}
	public static boolean EqualsUUID(final UUID uuidA, final UUID uuidB) {
		if (uuidA == null || uuidB == null) {
			return (uuidA == null && uuidB == null);
		}
		return uuidA.equals(uuidB);
	}
	public static boolean EqualsPotionEffect(final PotionEffect effectA, final PotionEffect effectB) {
		if (effectA == null || effectB == null) {
			return (effectA == null && effectB == null);
		}
		return effectA.equals(effectB);
	}



}
