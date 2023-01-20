package com.poixson.chunkprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import com.poixson.chunkprotect.ChunkProtectPlugin;


public class ProtectedAreaHandler implements Listener {

	protected final ChunkProtectPlugin plugin;



	public ProtectedAreaHandler(final ChunkProtectPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (this.handleBlockEvent(true, event.getBlock(), event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBlockBreak(final BlockBreakEvent event) {
		if (this.handleBlockEvent(false, event.getBlock(), event.getPlayer()))
			event.setCancelled(true);
	}

	protected boolean handleBlockEvent(final boolean placebreak,
			final Block block, final Player player) {
		final Material type = block.getType();
		final Location loc = block.getLocation();
		// place/break beacon
		if (Material.BEACON.equals(type)) {
			// place beacon
			if (placebreak) {
				if (this.plugin.isSpawnArea(loc)) {
					player.sendMessage(ChatColor.AQUA + "Can't place a beacon in the spawn area");
					return true;
				}
				// check protected area
				final BeaconDAO dao = this.plugin.getBeaconArea(loc);
				if (dao != null) {
					player.sendMessage(ChatColor.RED + "Can't place a beacon here");
					return true;
				}
			// break beacon
			} else {
				final BeaconDAO dao = this.plugin.getBeaconDAO(loc);
				if (dao == null)
					return false;
				dao.sendOwnerMessage(ChatColor.RED + "Your beacon has been broken!\nThe area is now unprotected!");
				if (!dao.isOwner(player))
					player.sendMessage(ChatColor.RED + "Area protection broken!");
			}
			return false;
		}
		// check protected area
		{
			final BeaconDAO dao = this.plugin.getProtectedArea(loc);
			if (dao != null) {
				if (!dao.isBuildAllowed(player)) {
					player.sendMessage(ChatColor.RED + "You cannot build here");
					return true;
				}
			}
		}
		return false;
	}



}
