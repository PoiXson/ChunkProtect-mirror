package com.poixson.chunkprotect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;


public class TeamDAO {

	public final UUID owner;
	public final List<UUID> teammates = new ArrayList<UUID>();

	public final AtomicReference<String> name = new AtomicReference<String>(null);



	public TeamDAO(final UUID owner) {
		this.owner = owner;
	}



	public String getTeamName() {
		return this.name.get();
	}
	public void setTeamName(final String name) {
		this.name.set(name);
	}



	public void addPlayer(final UUID uuid) {
		this.teammates.add(uuid);
	}
	public boolean isOnTeam(final UUID uuid) {
		return this.teammates.contains(uuid);
	}
	public boolean removePlayer(final UUID uuid) {
		return this.teammates.remove(uuid);
	}



	public String getOwnerName() {
		final OfflinePlayer player = Bukkit.getOfflinePlayer(this.owner);
		if (player == null)
			return null;
		return player.getName();
	}
	public String[] getTeamPlayerNames() {
		final List<String> list = new ArrayList<String>();
		for (final UUID uuid : this.teammates) {
			final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			if (player != null)
				list.add(player.getName());
		}
		return list.toArray(new String[0]);
	}



}
