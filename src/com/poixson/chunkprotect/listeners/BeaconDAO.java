package com.poixson.chunkprotect.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;

import com.poixson.chunkprotect.exceptions.BeaconValidateException;


public class BeaconDAO {

	public final Location loc;

	public int tier = 0;
	public int tierLast = 0;

	public PotionEffect primary   = null;
	public PotionEffect secondary = null;

	public PotionEffect primaryLast   = null;
	public PotionEffect secondaryLast = null;



	public BeaconDAO(final Location loc) throws BeaconValidateException {
		this.loc = loc;
		this.update();
	}



	public void update() throws BeaconValidateException {
		final Block block = this.loc.getBlock();
		if (!Material.BEACON.equals(block.getType()))
			throw new BeaconValidateException();
		this.tierLast      = this.tier;
		this.primaryLast   = this.primary;
		this.secondaryLast = this.secondary;
		final Beacon beacon = (Beacon) block.getState();
		this.tier      = beacon.getTier();
		this.primary   = beacon.getPrimaryEffect();
		this.secondary = beacon.getSecondaryEffect();
	}



}
