package com.poixson.chunkprotect.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

	public BeaconDAO getDAO() {
		return this.dao;
	}

	public UUID getOwner() {
		return this.dao.owner;
	}
	public Player getPlayer() {
		return Bukkit.getPlayer(this.getOwner());
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
