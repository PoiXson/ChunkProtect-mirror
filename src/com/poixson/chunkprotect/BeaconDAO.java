package com.poixson.chunkprotect;

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


public class BeaconDAO implements ConfigurationSerializable {

	public final Location loc;
	public final UUID owner;

	public           int tier     = 0;
	public transient int tierLast = 0;

	public PotionEffect primary   = null;
	public PotionEffect secondary = null;

	public transient PotionEffect lastPrimary   = null;
	public transient PotionEffect lastSecondary = null;



	public BeaconDAO(final Location loc, final UUID owner) {
		this.loc   = loc;
		this.owner = owner;
	}



	public boolean update() {
		final Block block = this.loc.getBlock();
		if (!Material.BEACON.equals(block.getType()))
			return false;
		this.tierLast      = this.tier;
		this.lastPrimary   = this.primary;
		this.lastSecondary = this.secondary;
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
