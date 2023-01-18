package com.poixson.chunkprotect;

import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.chunkprotect.listeners.BeaconListener;
import com.poixson.chunkprotect.listeners.BlockPlaceBreakListener;
import com.poixson.chunkprotect.listeners.beacon.BeaconHandler;


public class ChunkProtectPlugin extends JavaPlugin {

	// listeners
	protected final AtomicReference<BeaconHandler>  beaconHandler  = new AtomicReference<BeaconHandler>(null);
	protected final AtomicReference<BeaconListener> beaconListener = new AtomicReference<BeaconListener>(null);
	protected final AtomicReference<BlockPlaceBreakListener> blockListener = new AtomicReference<BlockPlaceBreakListener>(null);



	public ChunkProtectPlugin() {
		super();
	}



	@Override
	public void onEnable() {
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
	}




}
