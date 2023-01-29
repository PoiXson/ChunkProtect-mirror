package com.poixson.chunkprotect;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
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
		if (uuidA == null || uuidB == null)
			return (uuidA == null && uuidB == null);
		return uuidA.equals(uuidB);
	}
	public static boolean EqualsPotionEffect(final PotionEffect effectA, final PotionEffect effectB) {
		if (effectA == null || effectB == null)
			return (effectA == null && effectB == null);
		return effectA.equals(effectB);
	}

	public static boolean EqualsWorld(final Location locA, final Location locB) {
		return EqualsWorld(locA.getWorld(), locB.getWorld());
	}
	public static boolean EqualsWorld(final World worldA, final World worldB) {
		if (worldA == null || worldB == null)
			return (worldA == null && worldB == null);
		return worldA.equals(worldB);
	}

	public static boolean EqualsBeaconDAO(final BeaconDAO beaconA, final BeaconDAO beaconB) {
		if (beaconA == null || beaconB == null)
			return (beaconA == null && beaconB == null);
		return beaconA.equals(beaconB);
	}



	public static boolean WithinArea(final AreaShape shape, final int radius,
			final Location locA, final Location locB) {
		if (radius <= 0)                    return false;
		if (!Utils.EqualsWorld(locA, locB)) return false;
		if (locA == null || locB == null)   return false;
		final int distX = Math.abs( locA.getBlockX() - locB.getBlockX() );
		final int distZ = Math.abs( locA.getBlockZ() - locB.getBlockZ() );
		switch (shape) {
		case CIRCLE: return (radius >= Math.sqrt( Math.pow(distX, 2) + Math.pow(distZ, 2) ));
		case SQUARE: return (radius >= distX && radius >= distZ);
		default: throw new RuntimeException("Unknown area shape: " + shape.toString());
		}

	}



}
