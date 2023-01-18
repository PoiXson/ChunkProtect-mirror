package com.poixson.chunkprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.poixson.chunkprotect.ChunkProtectPlugin;
import com.poixson.chunkprotect.listeners.beacon.BeaconEvent;


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
//TODO
System.out.println(event.getType().toString());
	}



}
