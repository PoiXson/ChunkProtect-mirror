package com.poixson.chunkprotect.listeners.beacon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.chunkprotect.ChunkProtectPlugin;
import com.poixson.chunkprotect.Utils;
import com.poixson.chunkprotect.exceptions.BeaconValidateException;


public class BeaconHandler extends BukkitRunnable implements Listener {

	protected final ChunkProtectPlugin plugin;
	protected final PluginManager pm;

	protected final HashMap<Location, BeaconDAO> beacons = new HashMap<Location, BeaconDAO>();



	public BeaconHandler(final ChunkProtectPlugin plugin) {
		this.plugin = plugin;
		this.pm = Bukkit.getPluginManager();
	}



	public void start() {
		this.runTaskTimer(this.plugin, 20L, 20L);
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void stop() {
		this.cancel();
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Block block = event.getBlock();
		final Material type = block.getType();
		if (Material.BEACON.equals(type)) {
			final Player player = event.getPlayer();
			try {
				final Location loc = block.getLocation();
				final BeaconDAO dao = new BeaconDAO(loc);
				if (this.runBeacon(dao)) {
					this.beacons.put(loc, dao);
					this.pm.callEvent(new BeaconEvent(BeaconEventType.Placed, dao));
				}
			} catch (BeaconValidateException e) {
				if (e.hasMessage()) {
					player.sendMessage(e.getMessage());
				} else {
					e.printStackTrace();
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final Material type = block.getType();
		if (Material.BEACON.equals(type)) {
			final Location loc = block.getLocation();
			final BeaconDAO dao = this.beacons.get(loc);
			if (dao != null) {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.Broken, dao));
				this.beacons.remove(loc);
			}
		}
	}



	@Override
	public void run() {
		final Iterator<Entry<Location, BeaconDAO>> it = this.beacons.entrySet().iterator();
		Entry<Location, BeaconDAO> entry;
		BeaconDAO dao;
		while (it.hasNext()) {
			entry = it.next();
			dao = entry.getValue();
			if (!this.runBeacon(dao)) {
				it.remove();
				this.pm.callEvent(new BeaconEvent(BeaconEventType.Broken, dao));
			}
		}
	}



	public boolean runBeacon(final BeaconDAO dao) {
		try {
			dao.update();
		} catch (BeaconValidateException e) {
			return false;
		}
		// tier changed
		if (dao.tier != dao.tierLast) {
			if (dao.tier == 0 && dao.tierLast > 0) {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.Activated, dao));
			} else
			if (dao.tier > 0 && dao.tierLast == 0) {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.Deactivated, dao));
			} else {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.TierChanged, dao));
			}
		}
		// primary effect changed
		if (!Utils.PotionEffectEquals(dao.primary, dao.primaryLast))
			this.pm.callEvent(new BeaconEvent(BeaconEventType.PrimaryChanged, dao));
		// secondary effect changed
		if (!Utils.PotionEffectEquals(dao.secondary, dao.secondaryLast))
			this.pm.callEvent(new BeaconEvent(BeaconEventType.SecondaryChanged, dao));
		return true;
	}



}
