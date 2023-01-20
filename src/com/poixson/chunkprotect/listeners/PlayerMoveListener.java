package com.poixson.chunkprotect.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.poixson.chunkprotect.ChunkProtectPlugin;
import com.poixson.chunkprotect.Utils;


public class PlayerMoveListener implements Listener {

	protected final ChunkProtectPlugin plugin;

	protected final HashMap<UUID, BeaconDAO> inarea = new HashMap<UUID, BeaconDAO>();



	public PlayerMoveListener(final ChunkProtectPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Location to   = event.getTo();
		final Location from = event.getFrom();
		if (from.getBlockX() != to.getBlockX()
		||  from.getBlockZ() != to.getBlockZ() ) {
			final UUID uuid = event.getPlayer().getUniqueId();
			final BeaconDAO dao = this.plugin.getProtectedArea(to);
			final BeaconDAO daoLast = this.inarea.get(uuid);
			this.inarea.put(uuid, dao);
			if (!Utils.EqualsBeaconDAO(dao, daoLast)) {
				final Player player = event.getPlayer();
				if (dao == null) {
					player.sendMessage(ChatColor.AQUA + "You left the protected area");
				} else
				if (dao.isOwner(player)) {
					player.sendMessage(ChatColor.AQUA + "Welcome home");
				} else {
					final String name = Bukkit.getOfflinePlayer(dao.owner).getName();
					player.sendMessage(ChatColor.AQUA + "You entered the area of: " + name);
				}
			}
		}
	}



}
