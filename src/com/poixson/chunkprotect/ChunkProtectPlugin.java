package com.poixson.chunkprotect;

import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.chunkprotect.listeners.BeaconHandler;
import com.poixson.chunkprotect.listeners.BeaconListener;
import com.poixson.chunkprotect.listeners.BlockPlaceBreakListener;


public class ChunkProtectPlugin extends JavaPlugin {

	public static final int DEFAULT_SPAWN_RADIUS = 56;

	protected final AtomicReference<FileConfiguration> config = new AtomicReference<FileConfiguration>(null);

	// listeners
	protected final AtomicReference<BeaconHandler>  beaconHandler  = new AtomicReference<BeaconHandler>(null);
	protected final AtomicReference<BeaconListener> beaconListener = new AtomicReference<BeaconListener>(null);
	protected final AtomicReference<BlockPlaceBreakListener> blockListener = new AtomicReference<BlockPlaceBreakListener>(null);



	public ChunkProtectPlugin() {
		super();
	}



	@Override
	public void onEnable() {
		// load configs
		this.loadConfigs();
		// beacon handler
		{
			final BeaconHandler handler = new BeaconHandler(this);
			final BeaconHandler previous = this.beaconHandler.getAndSet(handler);
			if (previous != null)
				previous.stop();
			handler.start();
		}
		// beacon listener
		{
			final BeaconListener listener = new BeaconListener(this);
			final BeaconListener previous = this.beaconListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// block place/break listener
		{
			final BlockPlaceBreakListener listener = new BlockPlaceBreakListener(this);
			final BlockPlaceBreakListener previous = this.blockListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
	}



	@Override
	public void onDisable() {
		// beacon handler
		{
			final BeaconHandler handler = this.beaconHandler.getAndSet(null);
			if (handler != null)
				handler.stop();
		}
		// beacon listener
		{
			final BeaconListener listener = this.beaconListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// block place/break listener
		{
			final BlockPlaceBreakListener listener = this.blockListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// stop listeners
		HandlerList.unregisterAll(this);
		// save configs
		this.saveConfigs();
		this.config.set(null);
	}



	// -------------------------------------------------------------------------------
	// configs



	protected void loadConfigs() {
		// plugin dir
		{
			final File path = this.getDataFolder();
			if (!path.isDirectory()) {
				if (!path.mkdir())
					throw new RuntimeException("Failed to create directory: " + path.toString());
				log.info(LOG_PREFIX + "Created directory: " + path.toString());
			}
		}
		// config.yml
		{
			final FileConfiguration cfg = this.getConfig();
			this.config.set(cfg);
			this.configDefaults(cfg);
			cfg.options().copyDefaults(true);
			super.saveConfig();
		}
	}
	protected void saveConfigs() {
		// config.yml
		super.saveConfig();
	}
	protected void configDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Spawn Radius", Integer.valueOf(DEFAULT_SPAWN_RADIUS));
	}




}
