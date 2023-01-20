package com.poixson.chunkprotect;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.chunkprotect.listeners.AreaShape;
import com.poixson.chunkprotect.listeners.BeaconDAO;
import com.poixson.chunkprotect.listeners.BeaconHandler;
import com.poixson.chunkprotect.listeners.BeaconListener;
import com.poixson.chunkprotect.listeners.PlayerMoveListener;
import com.poixson.chunkprotect.listeners.ProtectedAreaHandler;


public class ChunkProtectPlugin extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String LOG_PREFIX = "[ChunkProtect] ";

	public static final AreaShape DEFAULT_AREA_SHAPE = AreaShape.CIRCLE;
	public static final int DEFAULT_SPAWN_RADIUS = 56;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER1 = 1 * 16;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER2 = 2 * 16;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER3 = 5 * 16;
	public static final int DEFAULT_PROTECTED_RADIUS_TIER4 = 9 * 16;

	// configs
	protected final AtomicReference<FileConfiguration> config     = new AtomicReference<FileConfiguration>(null);
	protected final AtomicReference<AreaShape>         areaShape  = new AtomicReference<AreaShape>(null);
	protected final AtomicReference<int[]>             areaSizes  = new AtomicReference<int[]>(null);
	protected final AtomicReference<FileConfiguration> cfgBeacons = new AtomicReference<FileConfiguration>(null);

	// listeners
	protected final AtomicReference<BeaconHandler>        beaconHandler  = new AtomicReference<BeaconHandler>(null);
	protected final AtomicReference<BeaconListener>       beaconListener = new AtomicReference<BeaconListener>(null);
	protected final AtomicReference<PlayerMoveListener>   moveListener   = new AtomicReference<PlayerMoveListener>(null);
	protected final AtomicReference<ProtectedAreaHandler> protectHandler = new AtomicReference<ProtectedAreaHandler>(null);



	public ChunkProtectPlugin() {
		super();
		ConfigurationSerialization.registerClass(BeaconDAO.class);
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
	}



	@Override
	public void onDisable() {
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		// save configs
		this.saveConfigs();
		this.config.set(null);
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
	}
	protected void configDefaults(final FileConfiguration cfg) {
		cfg.addDefault("Area Shape", DEFAULT_AREA_SHAPE.toString());
		cfg.addDefault("Spawn Radius", Integer.valueOf(DEFAULT_SPAWN_RADIUS));
		cfg.addDefault("Protect Area Tier 1", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER1));
		cfg.addDefault("Protect Area Tier 2", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER2));
		cfg.addDefault("Protect Area Tier 3", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER3));
		cfg.addDefault("Protect Area Tier 4", Integer.valueOf(DEFAULT_PROTECTED_RADIUS_TIER4));
	}



	public AreaShape getAreaShape() {
		return this.areaShape.get();
	}

	public int getProtectedAreaRadius(final int tier) {
		if (tier < 0 || tier > 4) throw new RuntimeException("Tier value is out of range: " + Integer.toString(tier));
		final int[] sizes = this.areaSizes.get();
		if (sizes == null) {
			log.warning(LOG_PREFIX + "protected area sizes not set!");
			return 0;
		}
		return sizes[tier];
	}



	// -------------------------------------------------------------------------------
	// protected areas



	public BeaconDAO getBeaconDAO(final Location loc) {
		return this.beaconHandler.get()
				.beacons.get(loc);
	}

	public BeaconDAO getProtectedArea(final Location loc) {
		final AreaShape shape = this.getAreaShape();
		int distance;
		final Iterator<Entry<Location, BeaconDAO>> it =
			this.beaconHandler.get()
				.beacons.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Location, BeaconDAO> entry = it.next();
			final BeaconDAO dao = entry.getValue();
			distance = this.getProtectedAreaRadius(dao.tier);
			if (dao.isProtectedArea(loc, shape, distance))
				return dao;
		}
		return null;
	}



}
