package com.poixson.chunkprotect.listeners.beacon;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class BeaconEvent extends Event {

	protected static final HandlerList handlers = new HandlerList();

	protected final BeaconEventType type;
	protected final BeaconDAO dao;



	public BeaconEvent(final BeaconEventType type, final BeaconDAO dao) {
		this.type = type;
		this.dao  = dao;
	}



	public BeaconEventType getType() {
		return this.type;
	}

	public Location getLocation() {
		return this.dao.loc;
	}



	public static HandlerList getHandlerList() {
		return handlers;
	}
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}



}
