package com.poixson.chunkprotect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


public class CommandsHandler implements CommandExecutor, TabCompleter {

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
					sender.sendMessage("Console cannot use this command");
					return true;
				}
				final TeamDAO team = this.plugin.getOwnTeam(uuid);
				@SuppressWarnings("deprecation")
				final OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
				if (p == null) {
					sender.sendMessage(ChatColor.AQUA + "Unknown player: " + args[1]);
					return true;
				}
				team.addPlayer(p.getUniqueId());
				sender.sendMessage(ChatColor.AQUA + "Added player to your team: " + args[1]);
				return true;
			}
			case "remove": {
				if (player == null) {
					sender.sendMessage("Console cannot use this command");
					return true;
				}
				@SuppressWarnings("deprecation")
				final OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
				if (p == null) {
					sender.sendMessage(ChatColor.AQUA + "Player not found: " + args[1]);
					return true;
				}
				final TeamDAO team = this.plugin.getOwnTeam(uuid);
				if (team.removePlayer(p.getUniqueId())) {
					sender.sendMessage(ChatColor.AQUA + "Removed player from your team: " + args[1]);
				} else {
					sender.sendMessage(ChatColor.AQUA + "Player is not on your team: " + args[1]);
				}
				return true;
			}
			default: break;
			}
		}
		if (num > 0) {
			switch (args[0]) {
			case "name": {
				if (player == null) {
					sender.sendMessage("Console cannot use this command");
					return true;
				}
				if (num > 1) {
					final TeamDAO team = this.plugin.getOwnTeam(uuid);
					team.setTeamName(args[1].trim());
					sender.sendMessage(ChatColor.AQUA + "Your team name is changed to: " + args[1]);
				} else {
					final String name = this.plugin.getTeamName(player.getUniqueId());
					if (name == null || name.isEmpty()) {
						sender.sendMessage(ChatColor.AQUA + "Your team doesn't have a name");
					} else {
						sender.sendMessage(ChatColor.AQUA + "Your team name is: " + name);
					}
				}
				return true;
			}
			case "list": {
				if (player == null) {
					sender.sendMessage("Console cannot use this command");
					return true;
				}
				final TeamDAO team = this.plugin.findTeam(uuid);
				if (team == null) {
					sender.sendMessage(ChatColor.AQUA + "You're not on a team");
				} else {
					final String ownerName = team.getOwnerName();
					final String[] playerNames = team.getTeamPlayerNames();
					final StringBuilder msg = new StringBuilder();
					msg.append("Team leader: ")
						.append(ownerName);
					for (final String name : playerNames) {
						msg.append("\n  ")
							.append(name);
					}
					sender.sendMessage(msg.toString());
				}
				return true;
			}
			default: break;
			}
		}
		return false;
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
