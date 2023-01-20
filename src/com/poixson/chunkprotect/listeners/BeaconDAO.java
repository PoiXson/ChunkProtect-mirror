package com.poixson.chunkprotect.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.poixson.chunkprotect.Utils;


public class BeaconDAO implements ConfigurationSerializable {

	public final Location loc;
	public final UUID owner;

	public           int tier     = 0;
	public transient int tierLast = 0;

	public PotionEffect primary   = null;
	public PotionEffect secondary = null;

	public transient PotionEffect primaryLast   = null;
	public transient PotionEffect secondaryLast = null;



	public BeaconDAO(final Location loc, final UUID owner) {
		this.loc   = loc;
		this.owner = owner;
	}



	public boolean update() {
		final Block block = this.loc.getBlock();
		if (!Material.BEACON.equals(block.getType()))
			return false;
		this.tierLast      = this.tier;
		this.primaryLast   = this.primary;
		this.secondaryLast = this.secondary;
		final Beacon beacon = (Beacon) block.getState();
		this.tier      = beacon.getTier();
		this.primary   = beacon.getPrimaryEffect();
		this.secondary = beacon.getSecondaryEffect();
		return true;
	}



	@Override
	public Map<String, Object> serialize() {
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Location", this.loc);
		map.put("Owner",    this.owner.toString());
		return map;
	}
	public static BeaconDAO deserialize(final Map<String, Object> map) {
		final Location loc = (Location) map.get("Location");
		final UUID owner = UUID.fromString( (String)map.get("Owner") );
		return new BeaconDAO(loc, owner);
	}



	public boolean isProtectedArea(final Location loc,
			final AreaShape shape, final int distance) {
		if (!Utils.EqualsWorld(this.loc, loc))
			return false;
		switch (shape) {
		case CIRCLE: {
			final int x = this.loc.getBlockX() - loc.getBlockX();
			final int z = this.loc.getBlockZ() - loc.getBlockZ();
			final int dist = (int) Math.sqrt( Math.pow(x, 2) + Math.pow(z, 2) );
			return (dist <= distance);
		}
		case SQUARE: {
			final int distX = Math.abs( this.loc.getBlockX() - loc.getBlockX() );
			final int distZ = Math.abs( this.loc.getBlockZ() - loc.getBlockZ() );
			return (distX <= distance || distZ <= distance);
		}
		default: throw new RuntimeException("Unknown area shape: " + shape.toString());
		}
	}

	public boolean isBuildAllowed(final Player player) {
		return this.isBuildAllowed(player.getUniqueId());
	}
	public boolean isBuildAllowed(final UUID uuid) {
		return Utils.EqualsUUID(this.owner, uuid);
	}



	public boolean isOwner(final Player player) {
		return this.isOwner(player.getUniqueId());
	}
	public boolean isOwner(final UUID uuid) {
		return Utils.EqualsUUID(this.owner, uuid);
	}
	public String getOwnerName() {
		final OfflinePlayer player = Bukkit.getOfflinePlayer(this.owner);
		if (player == null) return null;
		return player.getName();
	}
	public void sendOwnerMessage(final String msg) {
		final Player owner = Bukkit.getPlayer(this.owner);
		if (owner != null)
			owner.sendMessage(msg);
	}



}
