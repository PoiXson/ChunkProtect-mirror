package com.poixson.chunkprotect;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.chunkprotect.listeners.BeaconHandler;
import com.poixson.chunkprotect.listeners.BeaconListener;
import com.poixson.chunkprotect.listeners.PlayerMoveListener;
import com.poixson.chunkprotect.listeners.ProtectedAreaHandler;


public class ChunkProtectPlugin extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String LOG_PREFIX = "[ChunkProtect] ";

//TODO: use arrays for layer block types and radius
	public static final AreaShape DEFAULT_AREA_SHAPE = AreaShape.SQUARE;
	public static final int DEFAULT_SPAWN_RADIUS = 56;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER1 = 8;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER2 = 24;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER3 = 40;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER4 = 56;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER5 = 72;
	public static final Map<String, Integer> DEFAULT_STARTING_KIT = Map.of(
		"BEACON",     Integer.valueOf(1),
		"IRON_BLOCK", Integer.valueOf(9)
	);

	// configs
	protected final AtomicReference<FileConfiguration> config     = new AtomicReference<FileConfiguration>(null);
	protected final AtomicReference<AreaShape>         areaShape  = new AtomicReference<AreaShape>(null);
	protected final AtomicReference<int[]>             areaSizes  = new AtomicReference<int[]>(null);
	protected final AtomicReference<FileConfiguration> cfgBeacons = new AtomicReference<FileConfiguration>(null);

	// listeners
	protected final AtomicReference<CommandsHandler>      commands       = new AtomicReference<CommandsHandler>(null);
	protected final AtomicReference<BeaconHandler>        beaconHandler  = new AtomicReference<BeaconHandler>(null);
	protected final AtomicReference<BeaconListener>       beaconListener = new AtomicReference<BeaconListener>(null);
	protected final AtomicReference<PlayerMoveListener>   moveListener   = new AtomicReference<PlayerMoveListener>(null);
	protected final AtomicReference<ProtectedAreaHandler> protectHandler = new AtomicReference<ProtectedAreaHandler>(null);

	// starting kit
	protected final AtomicReference<StartingKit> kits = new AtomicReference<StartingKit>(null);

	// teams
	protected final CopyOnWriteArraySet<TeamDAO> teams = new CopyOnWriteArraySet<TeamDAO>();



	public ChunkProtectPlugin() {
		super();
		ConfigurationSerialization.registerClass(BeaconDAO.class);
	}



	@Override
	public void onEnable() {
		// starting kit handler
		{
			final StartingKit kit = new StartingKit(this);
			final StartingKit previous = this.kits.getAndSet(kit);
			if (previous != null)
				previous.unregister();
		}
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
		// load beacons
		{
			final FileConfiguration cfg = this.cfgBeacons.get();
			if (cfg != null) {
				this.beaconHandler.get()
					.load(cfg);
				this.cfgBeacons.set(null);
			}
		}
		// protected area handler
		{
			final ProtectedAreaHandler handler = new ProtectedAreaHandler(this);
			final ProtectedAreaHandler previous = this.protectHandler.getAndSet(handler);
			if (previous != null)
				previous.unregister();
			handler.register();
		}
		// player move listener
		{
			final PlayerMoveListener listener = new PlayerMoveListener(this);
			final PlayerMoveListener previous = this.moveListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// starting kits
		this.kits.get().register();
		// commands
		{
			final CommandsHandler handler = new CommandsHandler(this);
			final CommandsHandler previous = this.commands.getAndSet(handler);
			if (previous != null)
				previous.unregister();
			handler.register();
		}
	}



	@Override
	public void onDisable() {
		// commands
		{
			final CommandsHandler handler = this.commands.getAndSet(null);
			if (handler != null)
				handler.unregister();
		}
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		// save configs
		this.saveConfigs();
		this.config.set(null);
		// starting kits
		{
			final StartingKit kit = this.kits.getAndSet(null);
			if (kit != null)
				kit.unregister();
		}
		// player move listener
		{
			final PlayerMoveListener listener = this.moveListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// protected area handler
		{
			final ProtectedAreaHandler handler = this.protectHandler.getAndSet(null);
			if (handler != null)
				handler.unregister();
		}
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
		// stop listeners
		HandlerList.unregisterAll(this);
	}



	// -------------------------------------------------------------------------------
	// configs



	protected void loadConfigs() {
		final StartingKit kit_handler = this.kits.get();
		if (kit_handler == null) throw new NullPointerException("kit handler not set");
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
			// kits
			{
				final ConfigurationSection c = cfg.getConfigurationSection("Starting Kit");
				if (c != null) {
					final Set<String> keys = c.getKeys(false);
					for (final String key : keys) {
						try {
							final Material type = Material.getMaterial(key);
							if (type == null) {
								log.severe(LOG_PREFIX + "Unknown kit item: " + key);
								continue;
							}
							final int qty = c.getInt(key);
							if (qty <= 0) {
								log.severe(LOG_PREFIX + "Invalid qty for kit item: " + key);
								continue;
							}
							kit_handler.items.put(type, Integer.valueOf(qty));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					log.info(String.format(
						"%sLoaded %d kit stacks",
						LOG_PREFIX,
						Integer.valueOf(kit_handler.items.size())
					));
				}
			}
			// area shape
			{
				final AreaShape shape = AreaShape.Get(cfg.getString("Area Shape"));
				if (shape == null) {
					log.warning("Area shape not set or invalid! Using default: " + DEFAULT_AREA_SHAPE.toString());
					this.areaShape.set(DEFAULT_AREA_SHAPE);
				} else {
					this.areaShape.set(shape);
				}
			}
			// protected area radius
			{
				final int[] sizes = new int[] {
					0,
					cfg.getInt("Protect Area Tier 1"),
					cfg.getInt("Protect Area Tier 2"),
					cfg.getInt("Protect Area Tier 3"),
					cfg.getInt("Protect Area Tier 4"),
					cfg.getInt("Protect Area Tier 5"),
				};
				this.areaSizes.set(sizes);
			}
		}
		// beacons.yml
		{
			final File file = new File(this.getDataFolder(), "beacons.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			this.cfgBeacons.set(cfg);
		}
		// player-kits.yml - received kit
		{
			final File file = new File(this.getDataFolder(), "player-kits.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			final List<String> list = cfg.getStringList("Players");
			if (list != null && !list.isEmpty()) {
				UUID uuid;
				for (final String str : list) {
					try {
						uuid = UUID.fromString(str);
						kit_handler.players.add(uuid);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		// teams.yml
		{
			final File file = new File(this.getDataFolder(), "teams.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			for (final String key : cfg.getKeys(false)) {
				try {
					final ConfigurationSection c = cfg.getConfigurationSection(key);
					final UUID owner = UUID.fromString(key);
					final TeamDAO team = new TeamDAO(owner);
					final String teamName = c.getString("Team Name");
					team.name.set(teamName);
					for (final String str : c.getStringList("Teammates")) {
						final UUID uuid = UUID.fromString(str);
						team.teammates.add(uuid);
					}
					this.teams.add(team);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	protected void saveConfigs() {
		// config.yml
		super.saveConfig();
		// beacons.yml
		{
			final File file = new File(this.getDataFolder(), "beacons.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			this.beaconHandler.get()
				.save(cfg);
			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// player-kits.yml - received kit
		{
			final StartingKit kit = this.kits.get();
			final File file = new File(this.getDataFolder(), "player-kits.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			final LinkedList<String> list = new LinkedList<String>();
			for (final UUID uuid : kit.players) {
				list.add(uuid.toString());
			}
			cfg.set("Players", list);
			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// teams.yml
		{
			final File file = new File(this.getDataFolder(), "teams.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			for (final TeamDAO team : this.teams) {
				final HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("Team Name", team.name.get());
				final Set<String> teammates = new HashSet<String>();
				for (final UUID uuid : team.teammates) {
					teammates.add(uuid.toString());
				}
				map.put("Teammates", teammates);
				cfg.set(team.owner.toString(), map);
			}
			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	protected void configDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Starting Kit", DEFAULT_STARTING_KIT);
		cfg.addDefault("Area Shape", DEFAULT_AREA_SHAPE.toString());
		cfg.addDefault("Spawn Radius", Integer.valueOf(DEFAULT_SPAWN_RADIUS));
		cfg.addDefault("Protect Area Tier 1", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER1));
		cfg.addDefault("Protect Area Tier 2", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER2));
		cfg.addDefault("Protect Area Tier 3", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER3));
		cfg.addDefault("Protect Area Tier 4", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER4));
		cfg.addDefault("Protect Area Tier 5", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER5));
	}



	// -------------------------------------------------------------------------------
	// protected areas



	public boolean isSpawnArea(final Location loc) {
		final AreaShape shape = this.getAreaShape();
		final int radius = this.config.get().getInt("Spawn Radius");
		Location spawn = loc.getWorld().getSpawnLocation();
		if (spawn == null)
			spawn = loc.getWorld().getBlockAt(0, 0, 0).getLocation();
		return Utils.WithinArea(shape, radius, spawn, loc);
	}
	public boolean isSpawnAreaNear(final Location loc) {
		final AreaShape shape = this.getAreaShape();
		final int radius = this.config.get().getInt("Spawn Radius") + this.getMaxAreaSize();
		Location spawn = loc.getWorld().getSpawnLocation();
		if (spawn == null)
			spawn = loc.getWorld().getBlockAt(0, 0, 0).getLocation();
		return Utils.WithinArea(shape, radius, spawn, loc);
	}

	public BeaconDAO getBeaconDAO(final Location loc) {
		return this.beaconHandler.get()
				.beacons.get(loc);
	}

	public BeaconDAO getBeaconNear(final Location loc) {
		final AreaShape shape = this.getAreaShape();
		final int radius = 2 * this.getMaxAreaSize();
		final Iterator<Entry<Location, BeaconDAO>> it =
				this.beaconHandler.get()
					.beacons.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Location, BeaconDAO> entry = it.next();
			final BeaconDAO dao = entry.getValue();
			if (Utils.WithinArea(shape, radius, dao.loc, loc))
				return dao;
		}
		return null;
	}

	public BeaconDAO getBeaconArea(final Location loc) {
		final AreaShape shape = this.getAreaShape();
		final Iterator<Entry<Location, BeaconDAO>> it =
			this.beaconHandler.get()
				.beacons.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Location, BeaconDAO> entry = it.next();
			final BeaconDAO dao = entry.getValue();
			final int radius = this.getProtectedAreaRadius(dao.tier);
			if (Utils.WithinArea(shape, radius, dao.loc, loc))
				return dao;
		}
		return null;
	}



	public AreaShape getAreaShape() {
		return this.areaShape.get();
	}

	public int getProtectedAreaRadius(final int tier) {
		if (tier < 0 || tier > 5) throw new RuntimeException("Tier value is out of range: " + Integer.toString(tier));
		final int[] sizes = this.areaSizes.get();
		if (sizes == null) {
			log.warning(LOG_PREFIX + "protected area sizes not set!");
			return 0;
		}
		return sizes[tier];
	}

	public int getMaxAreaSize() {
		int distance = 0;
		for (final int dist : this.areaSizes.get()) {
			if (distance < dist)
				distance = dist;
		}
		return distance;
	}



	// -------------------------------------------------------------------------------
	// teams



	public TeamDAO getOwnTeam(final UUID uuid) {
		// existing dao
		for (final TeamDAO team : this.teams) {
			if (Utils.EqualsUUID(uuid, team.owner))
				return team;
		}
		// new dao
		{
			final TeamDAO team = new TeamDAO(uuid);
			this.teams.add(team);
			return team;
		}
	}
	public String getTeamName(final UUID uuid) {
		final TeamDAO team = this.getOwnTeam(uuid);
		if (team == null) return null;
		return team.getTeamName();
	}
	public TeamDAO findTeam(final UUID uuid) {
		// team owner
		{
			TeamDAO team = this.getOwnTeam(uuid);
			if (team != null)
				return team;
		}
		// teammates
		for (final TeamDAO team : this.teams) {
			team.isOnTeam(uuid);
			if (team != null)
				return team;
		}
		return null;
	}



}
