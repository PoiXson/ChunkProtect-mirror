package com.poixson.chunkprotect;

import static com.poixson.chunkprotect.ChunkProtectPlugin.CHAT_PREFIX;
import static com.poixson.chunkprotect.ChunkProtectPlugin.CHAT_PREFIX_RED;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class RequestJoinTeam extends BukkitRunnable {

	public static final long TIMEOUT_TICKS = 1200L; // 1 minute

	protected final ChunkProtectPlugin plugin;

	protected final TeamDAO team;
	protected final Player player;
	protected final UUID uuid;

	protected final AtomicBoolean running = new AtomicBoolean(false);

	protected final long timeStart;



	public RequestJoinTeam(final ChunkProtectPlugin plugin,
			final TeamDAO team, final Player player) {
		this.plugin = plugin;
		this.team   = team;
		this.player = player;
		this.uuid   = player.getUniqueId();
		this.timeStart = System.currentTimeMillis();
	}



	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.runTaskLater(this.plugin, TIMEOUT_TICKS);
			this.plugin.register(this);
			this.team.sendOwnerMessage(String.format(
				"%sPlayer is requesting to join your team: %s\nTo accept this request: /team accept",
				CHAT_PREFIX,
				this.player.getName()
			));
		}
	}
	public void stop() {
		this.running.set(false);
		this.plugin.unregister(this);
		try {
			this.cancel();
		} catch (IllegalStateException ignore) {}
	}



	// accept join request
	public boolean accept() {
		final long since = System.currentTimeMillis() - this.timeStart;
		if (since <= 1000L) {
			this.team.sendOwnerMessage(CHAT_PREFIX_RED + "Accepted too quickly. Try again if you're sure.");
			return false;
		}
		if (!this.running.getAndSet(false))
			return false;
		this.stop();
		String teamName = this.team.getTeamName();
		if (teamName == null || teamName.isEmpty())
			teamName = this.team.getOwnerName();
		this.player.sendMessage(String.format(
			"%sYour request to join team %s has been accepted",
			CHAT_PREFIX,
			teamName
		));
		// remove from other teams
		for (final TeamDAO t : this.plugin.teams) {
			if (this.team.equals(t)) continue;
			t.teammates.remove(this.uuid);
		}
		// add to team
		this.team.addPlayer(this.uuid);
		this.team.sendTeamMessage(String.format(
			"%sPlayer joined the team: %s",
			CHAT_PREFIX,
			this.player.getName()
		));
		return true;
	}



	// timeout
	@Override
	public void run() {
		if (this.running.getAndSet(false)) {
			this.plugin.unregister(this);
			this.player.sendMessage(CHAT_PREFIX + "Join request failed");
		}
	}



	public boolean isTeamOwner(final Player player) {
		return this.isTeamOwner(player.getUniqueId());
	}
	public boolean isTeamOwner(final UUID uuid) {
		return uuid.equals(this.team.owner);
	}



}
