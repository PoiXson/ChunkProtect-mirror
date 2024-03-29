package com.poixson.chunkprotect.listeners;

import static com.poixson.chunkprotect.ChunkProtectPlugin.CHAT_PREFIX_RED;

import java.util.UUID;

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

import com.poixson.chunkprotect.BeaconDAO;
import com.poixson.chunkprotect.ChunkProtectPlugin;
import com.poixson.chunkprotect.TeamDAO;
import com.poixson.utils.Utils;


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
		final Location loc  = block.getLocation();
		// place/break beacon
		if (Material.BEACON.equals(type)) {
			// place beacon
			if (placebreak) {
				if (this.plugin.isSpawnAreaNear(loc)) {
					player.sendMessage(CHAT_PREFIX_RED + "Can't place a beacon in the spawn area");
					return true;
				}
				// check protected area
				final BeaconDAO dao = this.plugin.getBeaconNear(loc);
				if (dao != null) {
					player.sendMessage(CHAT_PREFIX_RED + "Can't place a beacon here");
					return true;
				}
			// break beacon
			} else {
				final BeaconDAO dao = this.plugin.getBeaconDAO(loc);
				if (dao == null)
					return false;
				dao.sendOwnerMessage(CHAT_PREFIX_RED + "Your beacon has been broken!\nThe area is now unprotected!");
				{
					final TeamDAO team = this.plugin.getOwnTeam(dao.owner);
					if (team != null)
						team.sendTeamMessage(CHAT_PREFIX_RED + "Your team beacon has been broken!\nThe area is now unprotected!");
				}
				if (!dao.isOwner(player))
					player.sendMessage(CHAT_PREFIX_RED + "Area protection broken!");
			}
			return false;
		}
		// check protected area
		if (!player.hasPermission("chunkprotect.bypass")) {
			final BeaconDAO dao = this.plugin.getBeaconArea(loc);
			if (dao != null) {
				final UUID uuid = player.getUniqueId();
				if (!Utils.EqualsUUID(dao.owner, uuid)) {
					final TeamDAO team = this.plugin.findTeam(uuid);
					if (!team.isOnTeam(uuid)) {
						player.sendMessage(CHAT_PREFIX_RED + "You cannot build here");
						return true;
					}
				}
			}
		}
		return false;
	}



}
