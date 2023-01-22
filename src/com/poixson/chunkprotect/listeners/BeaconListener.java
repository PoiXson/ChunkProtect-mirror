package com.poixson.chunkprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.poixson.chunkprotect.ChunkProtectPlugin;


public class BeaconListener implements Listener {

	protected final ChunkProtectPlugin plugin;



	public BeaconListener(final ChunkProtectPlugin plugin) {
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
	public void onBeaconChange(final BeaconEvent event) {
		final BeaconEventType type = event.getType();
		final BeaconDAO dao = event.getDAO();
		final StringBuilder msg = new StringBuilder();
		switch (type) {
		case ACTIVATED:
		case TIER_CHANGED:
			msg.append("This area is now protected (");
			final int radius = this.plugin.getProtectedAreaRadius(dao.tier);
			final int diameter = radius * 2;
			if (diameter % 16 == 0) {
				final int diam = diameter / 16;
				msg.append(String.format(
					"%dx%d chunks",
					Integer.valueOf(diam),
					Integer.valueOf(diam)
				));
			} else {
				msg.append(String.format(
					"%dx%d blocks",
					Integer.valueOf(diameter),
					Integer.valueOf(diameter)
				));
			}
			msg.append(')');
			break;
		default: break;
		}
		if (!msg.isEmpty())
			dao.sendOwnerMessage(ChatColor.AQUA + msg.toString());
	}



}
