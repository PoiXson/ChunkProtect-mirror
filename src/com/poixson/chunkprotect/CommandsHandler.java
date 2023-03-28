package com.poixson.chunkprotect;

import static com.poixson.chunkprotect.ChunkProtectPlugin.CHAT_PREFIX;
import static com.poixson.chunkprotect.ChunkProtectPlugin.CHAT_PREFIX_RED;
import static com.poixson.chunkprotect.ChunkProtectPlugin.LOG_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


public class CommandsHandler implements CommandExecutor, TabCompleter {

	public static final String MSG_NO_PERM = "You don't have permission to use this command.";

	protected final ChunkProtectPlugin plugin;

	protected final HashMap<String, PluginCommand> pcs = new HashMap<String, PluginCommand>();



	public CommandsHandler(final ChunkProtectPlugin plugin) {
		this.plugin = plugin;
	}



	@Override
	public boolean onCommand(final CommandSender sender,
			final Command command, final String label, final String[] args) {
		final Player player = (sender instanceof Player ? (Player)sender : null);
		final UUID uuid = (player == null ? null : player.getUniqueId());
		final int num = args.length;
		if (num == 2) {
			switch (args[0]) {
			case "add": {
				if (player == null) {
					sender.sendMessage(LOG_PREFIX + "Console cannot use this command");
					return true;
				}
				if (!player.hasPermission("chunkprotect.team.add")) {
					player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
					return true;
				}
				final TeamDAO team = this.plugin.getOwnTeam(uuid);
				final Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(CHAT_PREFIX_RED + "Unknown player: " + args[1]);
					return true;
				}
				final UUID u = p.getUniqueId();
				// remove from other teams
				for (final TeamDAO t : this.plugin.teams) {
					if (team.equals(t)) continue;
					t.teammates.remove(u);
				}
				// add to team
				team.addPlayer(u);
				sender.sendMessage(CHAT_PREFIX + "Added player to your team: " + args[1]);
				return true;
			}
			case "remove": {
				if (player == null) {
					sender.sendMessage(LOG_PREFIX + "Console cannot use this command");
					return true;
				}
				if (!player.hasPermission("chunkprotect.team.remove")) {
					player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
					return true;
				}
				@SuppressWarnings("deprecation")
				final OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
				if (p == null) {
					sender.sendMessage(CHAT_PREFIX_RED + "Player not found: " + args[1]);
					return true;
				}
				final TeamDAO team = this.plugin.getOwnTeam(uuid);
				if (team.removePlayer(p.getUniqueId())) {
					sender.sendMessage(CHAT_PREFIX_RED + "Removed player from your team: " + args[1]);
				} else {
					sender.sendMessage(CHAT_PREFIX_RED + "Player is not on your team: " + args[1]);
				}
				return true;
			}
			case "join": {
				if (player == null) {
					sender.sendMessage(LOG_PREFIX + "Console cannot use this command");
					return true;
				}
				if (!player.hasPermission("chunkprotect.team.join")) {
					player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
					return true;
				}
				final TeamDAO team = this.plugin.findTeam(args[1]);
				if (team == null) {
					player.sendMessage(CHAT_PREFIX_RED + "Unknown team or player: " + args[1]);
					return true;
				}
				if (team.isOnTeam(uuid)) {
					player.sendMessage(CHAT_PREFIX_RED + "You are already on this team");
					return true;
				}
				if (team.isTeamOwner(uuid)) {
					player.sendMessage(CHAT_PREFIX_RED + "Can't join your own team");
					return true;
				}
				final RequestJoinTeam request = new RequestJoinTeam(this.plugin, team, player);
				this.plugin.register(request);
				request.start();
				return true;
			}
			case "accept": {
				if (player == null) {
					sender.sendMessage(LOG_PREFIX + "Console cannot use this command");
					return true;
				}
				if (!player.hasPermission("chunkprotect.team.accept")) {
					player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
					return true;
				}
				this.plugin.acceptJoinRequest(player);
				return true;
			}
			default: break;
			}
		}
		if (num > 0) {
			switch (args[0]) {
			case "name": {
				if (player == null) {
					sender.sendMessage(LOG_PREFIX + "Console cannot use this command");
					return true;
				}
				if (num > 1) {
					if (!player.hasPermission("chunkprotect.team.name.change")) {
						player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
						return true;
					}
					final TeamDAO team = this.plugin.getOwnTeam(uuid);
					final String name = args[1].trim();
					if (!name.isEmpty()) {
						team.setTeamName(name);
						sender.sendMessage(CHAT_PREFIX + "Your team name is changed to: " + name);
					}
				} else {
					if (!player.hasPermission("chunkprotect.team.name")) {
						player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
						return true;
					}
					final TeamDAO team = this.plugin.getOwnTeam(uuid);
					final String name = team.getTeamName();
					if (name != null && !name.isEmpty()) {
						sender.sendMessage(CHAT_PREFIX + "Your team name is: " + name);
					}
				}
				return true;
			}
			case "list": {
				if (player == null) {
					sender.sendMessage(LOG_PREFIX + "Console cannot use this command");
					return true;
				}
				if (num > 1 && "all".equals(args[1])) {
					if (!player.hasPermission("chunkprotect.team.list.all")) {
						player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
						return true;
					}
//TODO
				} else {
					if (!player.hasPermission("chunkprotect.team.list.own")) {
						player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
						return true;
					}
					final TeamDAO team = this.plugin.findTeam(uuid);
					if (team == null) {
						sender.sendMessage(CHAT_PREFIX_RED + "You're not on a team");
					} else {
						final String ownerName = team.getOwnerName();
						final String[] playerNames = team.getTeamPlayerNames();
						final StringBuilder msg = new StringBuilder();
						msg.append(CHAT_PREFIX)
							.append("Team leader: ")
							.append(ownerName);
						for (final String name : playerNames) {
							msg.append("\n  ")
								.append(name);
						}
						sender.sendMessage(msg.toString());
					}
				}
				return true;
			}
			default: break;
			}
		}
		// team name
		{
			if (!player.hasPermission("chunkprotect.team.name")) {
				player.sendMessage(CHAT_PREFIX_RED + MSG_NO_PERM);
				return true;
			}
			final String name = this.plugin.getTeamName(player.getUniqueId());
			if (name == null || name.isEmpty()) {
				sender.sendMessage(CHAT_PREFIX + "Your team doesn't have a name");
			} else {
				sender.sendMessage(CHAT_PREFIX + "Your team name is: " + name);
			}
		}
		return true;
	}



	public void register() {
		this.register("teams");
		this.register("team");
	}
	protected void register(final String label) {
		this.unregister(label);
		final PluginCommand pc = this.plugin.getCommand(label);
		pc.setExecutor(this);
		pc.setTabCompleter(this);
		this.pcs.put(label, pc);
	}

	public void unregister() {
		this.unregister("teams");
		this.unregister("team");
	}
	public void unregister(final String label) {
		final PluginCommand pc = this.pcs.get(label);
		if (pc != null) {
			this.pcs.remove(label);
			pc.setExecutor(null);
			pc.setTabCompleter(null);
		}
	}



	@Override
	public List<String> onTabComplete(final CommandSender sender,
			final Command command, final String label, final String[] args) {
		final List<String> result = new ArrayList<String>();
		final int num = args.length;
		switch (num) {
		case 1:
			if ("add".startsWith(   args[0])) result.add("add"   );
			if ("remove".startsWith(args[0])) result.add("remove");
			if ("join"  .startsWith(args[0])) result.add("join"  );
			if ("accept".startsWith(args[0])) result.add("accept");
			if ("name".startsWith(  args[0])) result.add("name"  );
			if ("list".startsWith(  args[0])) result.add("list"  );
			break;
		case 2:
			switch (args[0]) {
			case "add":
			case "remove":
				for (final Player p : Bukkit.getOnlinePlayers()) {
					final String name = p.getName();
					if (name.startsWith(args[1]))
						result.add(name);
				}
				break;
			case "join":
				for (final TeamDAO team : this.plugin.teams) {
					final String name = team.getTeamName();
					if (name != null) {
						if (name.startsWith(args[1]))
							result.add(name);
					}
				}
				for (final Player p : Bukkit.getOnlinePlayers()) {
					final String name = p.getName();
					if (name.startsWith(args[1]))
						result.add(name);
				}
			case "list": {
				if ("all".startsWith(args[1])) result.add("all");
				break;
			}
			default: break;
			}
			break;
		}
		return result;
	}



}
