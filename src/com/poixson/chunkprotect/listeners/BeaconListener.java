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
		switch (type) {
		case ACTIVATED: {
			this.plugin.addBeaconDAO(dao);
			dao.sendOwnerMessage(ChatColor.AQUA + "The area is now protected");
			break;
		}
		case BROKEN:
		case DEACTIVATED: this.plugin.removeBeaconDAO(dao.loc); break;
		case PLACED:            break;
		case TIER_CHANGED:      break;
		case PRIMARY_CHANGED:   break;
		case SECONDARY_CHANGED: break;
		default: throw new RuntimeException("Unknown beacon event type: " + type.toString());
		}
	}



}
