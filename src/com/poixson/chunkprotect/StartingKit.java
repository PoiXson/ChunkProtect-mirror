package com.poixson.chunkprotect;

import static com.poixson.chunkprotect.ChunkProtectPlugin.LOG;
import static com.poixson.chunkprotect.ChunkProtectPlugin.LOG_PREFIX;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class StartingKit implements Listener {

	protected final ChunkProtectPlugin plugin;

	public final List<UUID> players           = new LinkedList<UUID>();
	public final Map<Material, Integer> items = new HashMap<Material, Integer>();



	public StartingKit(final ChunkProtectPlugin plugin) {
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
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		if (!this.players.contains(uuid)) {
			this.players.add(uuid);
			this.giveKit(player);
		}
	}



	public void giveKit(final Player player) {
		final PlayerInventory inventory = player.getInventory();
		final Iterator<Entry<Material, Integer>> it = this.items.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Material, Integer> entry = it.next();
			final Material type = entry.getKey();
			final int qty = entry.getValue().intValue();
			final ItemStack stack = new ItemStack(type, qty);
			final HashMap<Integer, ItemStack> overflow = inventory.addItem(stack);
			// drop extra items
			if (overflow != null && !overflow.isEmpty()) {
				for (final ItemStack stk : overflow.values()) {
					player.getWorld().dropItemNaturally(player.getLocation(), stk);
				}
			}
		}
		player.updateInventory();
		LOG.info(String.format("%sGave starter kit to player: %s", LOG_PREFIX, player.getName()));
	}



}
