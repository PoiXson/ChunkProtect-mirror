package com.poixson.chunkprotect.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.chunkprotect.BeaconDAO;
import com.poixson.chunkprotect.ChunkProtectPlugin;
import com.poixson.chunkprotect.Utils;


public class BeaconHandler extends BukkitRunnable implements Listener {

	protected final ChunkProtectPlugin plugin;
	protected final PluginManager pm;

	public final HashMap<Location, BeaconDAO> beacons = new HashMap<Location, BeaconDAO>();



	public BeaconHandler(final ChunkProtectPlugin plugin) {
		this.plugin = plugin;
		this.pm = Bukkit.getPluginManager();
	}



	public void load(final FileConfiguration cfg) {
		if (cfg == null)              return;
		if (!cfg.contains("Beacons")) return;
		this.beacons.clear();
		@SuppressWarnings("unchecked")
		final List<BeaconDAO> list = (List<BeaconDAO>) cfg.getList("Beacons");
		for (final BeaconDAO dao : list) {
			if (dao != null) {
				if (dao.update())
					this.beacons.put(dao.loc, dao);
			}
		}
		ChunkProtectPlugin.log.info(String.format(
			"%sLoaded %d chunk protect beacons",
			ChunkProtectPlugin.LOG_PREFIX,
			Integer.valueOf(this.beacons.size())
		));
	}
	public void save(final FileConfiguration cfg) {
		final List<BeaconDAO> list = new ArrayList<BeaconDAO>();
		list.addAll( this.beacons.values() );
		cfg.set("Beacons", list);
	}



	public void start() {
		this.runTaskTimer(this.plugin, 20L, 20L);
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void stop() {
		try {
			this.cancel();
		} catch (IllegalStateException ignore) {}
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Block block = event.getBlock();
		final Material type = block.getType();
		if (Material.BEACON.equals(type)) {
			final UUID owner = event.getPlayer().getUniqueId();
			final Location loc = block.getLocation();
			final BeaconDAO dao = new BeaconDAO(loc, owner);
			if (dao.update()) {
				this.beacons.put(loc, dao);
				this.pm.callEvent(new BeaconEvent(BeaconEventType.PLACED, dao));
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
				this.pm.callEvent(new BeaconEvent(BeaconEventType.BROKEN, dao));
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
				this.pm.callEvent(new BeaconEvent(BeaconEventType.BROKEN, dao));
			}
		}
	}



	public boolean runBeacon(final BeaconDAO dao) {
		if (!dao.update())
			return false;
		// tier changed
		if (dao.tier != dao.tierLast) {
			if (dao.tier > 0 && dao.tierLast == 0) {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.ACTIVATED, dao));
			} else
			if (dao.tier == 0 && dao.tierLast > 0) {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.DEACTIVATED, dao));
			} else {
				this.pm.callEvent(new BeaconEvent(BeaconEventType.TIER_CHANGED, dao));
			}
		}
		// primary effect changed
		if (!Utils.EqualsPotionEffect(dao.primary, dao.lastPrimary))
			this.pm.callEvent(new BeaconEvent(BeaconEventType.PRIMARY_CHANGED, dao));
		// secondary effect changed
		if (!Utils.EqualsPotionEffect(dao.secondary, dao.lastSecondary))
			this.pm.callEvent(new BeaconEvent(BeaconEventType.SECONDARY_CHANGED, dao));
		return true;
	}



}
