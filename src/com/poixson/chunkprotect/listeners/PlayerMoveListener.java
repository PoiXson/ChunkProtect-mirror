package com.poixson.chunkprotect.listeners;

import java.util.HashMap;
import java.util.HashSet;
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

import com.poixson.chunkprotect.BeaconDAO;
import com.poixson.chunkprotect.ChunkProtectPlugin;
import com.poixson.chunkprotect.TeamDAO;
import com.poixson.chunkprotect.Utils;


public class PlayerMoveListener implements Listener {

	protected final ChunkProtectPlugin plugin;

	protected final HashMap<UUID, BeaconDAO> inarea  = new HashMap<UUID, BeaconDAO>();
	protected final HashSet<UUID>            inspawn = new HashSet<UUID>();



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
			final Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			// spawn area
			{
				final boolean inSpawn = this.plugin.isSpawnArea(to);
				final boolean lastInSpawn = this.inspawn.contains(uuid);
				if (inSpawn != lastInSpawn) {
					if (inSpawn) {
						this.inspawn.add(uuid);
						player.sendMessage(ChatColor.AQUA + "You've entered the spawn area");
					} else {
						this.inspawn.remove(uuid);
						player.sendMessage(ChatColor.AQUA + "You left the spawn area");
					}
				}
			}
			// beacon area
			{
				final BeaconDAO dao = this.plugin.getProtectedArea(to);
				final BeaconDAO daoLast = this.inarea.get(uuid);
				if (dao == null) this.inarea.remove(uuid);
				else             this.inarea.put(uuid, dao);
				if (!Utils.EqualsBeaconDAO(dao, daoLast)) {
					if (dao == null) {
						player.sendMessage(ChatColor.AQUA + "You left the protected area");
					} else
						if (dao.isOwner(player)) {
							player.sendMessage(ChatColor.AQUA + "Welcome home");
						} else {
							final String name = dao.getOwnerName();
						if (name == null || name.isEmpty()) {
							player.sendMessage(ChatColor.AQUA + "You entered a protected area");
						} else {
							player.sendMessage(ChatColor.AQUA + "You entered the area of: " + name);
						}
					}
				}
			}
		}
	}



}
