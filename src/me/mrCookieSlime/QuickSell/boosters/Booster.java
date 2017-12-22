package me.mrCookieSlime.QuickSell.boosters;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Clock;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.QuickSell.QuickSell;

public class Booster {
	
	public static List<Booster> active = new ArrayList<Booster>();
	
	BoosterType type;
	int id;
	int minutes;
	public UUID owner;
	double multiplier;
	Date timeout;
	Config cfg;
	boolean silent, infinite;
	Map<UUID, Integer> contributors = new HashMap<UUID, Integer>();
	
	public Booster(double multiplier, boolean silent, boolean infinite) {
		this(BoosterType.MONETARY, multiplier, silent, infinite);
	}
	
	@SuppressWarnings("deprecation")
	public Booster(BoosterType type, double multiplier, boolean silent, boolean infinite) {
		this.type = type;
		this.multiplier = multiplier;
		this.silent = silent;
		this.infinite = infinite;
		if (infinite) {
			this.minutes = Integer.MAX_VALUE;
			this.timeout = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
		}
		this.owner = Bukkit.getOfflinePlayer("internal").getUniqueId();
		
		active.add(this);
	}
	
	@Deprecated
	public Booster(String owner, double multiplier, int minutes) {
		this(BoosterType.MONETARY, owner, multiplier, minutes);
	}
	
	@Deprecated // TODO - undeprecate, this is just a reminder (?)
	public Booster(BoosterType type, String owner, double multiplier, int minutes) {
		this(type, Bukkit.getOfflinePlayer(owner).getUniqueId(), multiplier, minutes);
	}
	
	public Booster(BoosterType type, UUID owner, double multiplier, int minutes) {
		this.type = type;
		this.minutes = minutes;
		this.multiplier = multiplier;
		this.owner = owner;
		this.timeout = new Date(System.currentTimeMillis() + minutes * 60 * 1000);
		this.silent = false;
		this.infinite = false;
		
		contributors.put(owner, minutes);
	}

	@SuppressWarnings("deprecation")
	public Booster(int id) throws ParseException {
		active.add(this);
		this.id = id;
		this.cfg = new Config(new File("data-storage/QuickSell/boosters/" + id + ".booster"));
		if (cfg.contains("type")) this.type = BoosterType.valueOf(cfg.getString("type"));
		else {
			cfg.setValue("type", BoosterType.MONETARY.toString());
			cfg.save();
			this.type = BoosterType.MONETARY;
		}
		
		this.minutes = cfg.getInt("minutes");
		this.multiplier = (Double) cfg.getValue("multiplier");
		
		try {
			this.owner = cfg.getUUID("owner");
		} catch (IllegalArgumentException x) {
			this.owner = Bukkit.getOfflinePlayer(cfg.getString("owner")).getUniqueId();
			cfg.setValue("owner", this.owner.toString());
			cfg.save();
		}
		
		this.timeout = new SimpleDateFormat("yyyy-MM-dd-HH-mm").parse(cfg.getString("timeout"));
		this.silent= false;
		this.infinite = false;
		
		if (cfg.contains("contributors." + owner)) {
			for (String key: cfg.getKeys("contributors")) {
				try {
					contributors.put(UUID.fromString(key), cfg.getInt("contributors." + key));
				} catch (IllegalArgumentException x) {
					UUID cid = Bukkit.getOfflinePlayer(key).getUniqueId();
					contributors.put(cid, cfg.getInt("contributors." + key));
					cfg.setValue("contributors." + cid, contributors.get(cid));
					cfg.setValue("contributors." + key, null);
					cfg.save();
				}
			}
		}
		else {
			contributors.put(owner, minutes);
			writeContributors();
		}
	}
	
	private void writeContributors() {
		for (Map.Entry<UUID, Integer> entry: contributors.entrySet()) {
			cfg.setValue("contributors." + entry.getKey(), entry.getValue());
		}
		
		cfg.save();
	}

	public void activate() {
		if (QuickSell.cfg.getBoolean("boosters.extension-mode")) {
			for (Booster booster: active) {
				if (booster.getType().equals(this.type) && Double.compare(booster.getMultiplier(), getMultiplier()) == 0) {
					if ((this instanceof PrivateBooster && booster instanceof PrivateBooster) || (!(this instanceof PrivateBooster) && !(booster instanceof PrivateBooster))) {
						booster.extend(this);
						if (!silent) {
							if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwnerId()) != null) QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwnerId()), "pbooster.extended." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
							else {
								for (String message: QuickSell.local.getTranslation("booster.extended." + type.toString())) {
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier()))));
								}
							}
						}
						return;
					}
				}
			}
		}
		
		if (!infinite) {
			for (int i = 0; i < 1000; i++) {
				if (!new File("data-storage/QuickSell/boosters/" + i + ".booster").exists()) {
					this.id = i;
					break;
				}
			}
			this.cfg = new Config(new File("data-storage/QuickSell/boosters/" + id + ".booster"));
			cfg.setValue("type", type.toString());
			cfg.setValue("owner", getOwnerId());
			cfg.setValue("multiplier", multiplier);
			cfg.setValue("minutes", minutes);
			cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
			cfg.setValue("private", this instanceof PrivateBooster ? true: false);
			
			writeContributors();
		}
		
		active.add(this);
		if (!silent) {
			if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwner()), "pbooster.activate." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
			else {
				for (String message: QuickSell.local.getTranslation("booster.activate." + type.toString())) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier()))));
				}
			}
		}
	}
	// TODO - fix
	public void extend(Booster booster) {
		addTime(booster.getDuration());
		
		int minutes = contributors.containsKey(booster.getOwnerId()) ? contributors.get(booster.getOwnerId()): 0;
		minutes = minutes + booster.getDuration();
		contributors.put(booster.getOwnerId(), minutes);
		
		writeContributors();
	}

	public void deactivate() {
		if (!silent) {
			if (this instanceof PrivateBooster) 
				if (Bukkit.getPlayer(getOwnerId()) != null) QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwnerId()), "pbooster.deactivate." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
			else {
				for (String message: QuickSell.local.getTranslation("booster.deactivate." + type.toString())) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier()))));
				}
			}
		}
		if (!infinite) new File("data-storage/QuickSell/boosters/" + getID() + ".booster").delete();
		active.remove(this);
	}
	
	public static Iterator<Booster> iterate() {
		return active.iterator();
	}
	
	@Deprecated //TODO - undeprecate, this is just a reminder;
	public String getOwner() {
		return Bukkit.getOfflinePlayer(this.owner).getName();
	}
	
	public UUID getOwnerId() {
		return this.owner;
	}
	
	
	public Double getMultiplier()	{			return this.multiplier;			}
	public int getDuration()		{			return this.minutes;			}
	public Date getDeadLine() 		{			return this.timeout;			}
	public int getID()				{			return this.id;					}

	public long formatTime() {
		return ((getDeadLine().getTime() - Clock.getCurrentDate().getTime()) / (1000 * 60));
	}
	
	public void addTime(int minutes) {
		timeout = new Date(timeout.getTime() + minutes * 60 * 1000);
		cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
		cfg.save();
	}
	
	public static void update() {
		Iterator<Booster> boosters = Booster.iterate();
		while(boosters.hasNext()) {
			Booster booster = boosters.next();
			if (new Date().after(booster.getDeadLine())) {
				boosters.remove();
				booster.deactivate();
			}
		}
	}

	@Deprecated
	public static Double getMultiplier(String p) {
		return getMultiplier(p, BoosterType.MONETARY);
	}
	
	@Deprecated 
	public static List<Booster> getBoosters(String player) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();
		
		for (Booster booster: active) {
			if (booster.getAppliedPlayers().contains(player)) boosters.add(booster);
		}
		return boosters;
	}
	
	public static List<Booster> getBoosters(UUID id) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();
		
		for (Booster booster : active) {
			if (booster.getAppliedIds().contains(id)) boosters.add(booster);
		}
		
		return boosters;
	}
	
	@Deprecated
	public static List<Booster> getBoosters(String player, BoosterType type) {
		return Booster.getBoosters(Bukkit.getOfflinePlayer(player).getUniqueId(), type);
	}
	
	public static List<Booster> getBoosters(UUID id, BoosterType type) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();
		
		for (Booster booster : active) {
			if (booster.getAppliedIds().contains(id) && booster.getType().equals(type)) boosters.add(booster);
		}
		
		return boosters;
	}
	
	@Deprecated 
	public List<String> getAppliedPlayers() {
		List<String> players = new ArrayList<String>();
		for (Player p: Bukkit.getOnlinePlayers()) {
			players.add(p.getName());
		}
		return players;
	}
	
	public List<UUID> getAppliedIds() {
		List<UUID> players = new ArrayList<UUID>();
		for (Player p: Bukkit.getOnlinePlayers()) {
			players.add(p.getUniqueId());
		}
		return players;
		
	}
	
	public String getMessage() {
		return "messages.booster-use." + type.toString();
	}

	@Deprecated
	public static long getTimeLeft(String player) {
		long timeleft = 0;
		for (Booster booster: getBoosters(player)) {
			timeleft = timeleft + booster.formatTime();
		}
		return timeleft;
	}
	
	public BoosterType getType() {
		return this.type;
	}

	public boolean isSilent() {
		return silent;
	}
	
	public String getUniqueName() {
		switch(type) {
		case EXP:
			return "Booster (Experience)";
		case MCMMO:
			return "Booster (mcMMO)";
		case MONETARY:
			return "Booster (Money)";
		case PRISONGEMS:
			return "Booster (Gems)";
		default:
			return "Booster";
		}
	}
	
	@Deprecated // ?
	public static double getMultiplier(String name, BoosterType type) {
		double multiplier = 1.0;
		for (Booster booster: getBoosters(name, type)) {
			multiplier = multiplier * booster.getMultiplier();
		}
		return DoubleHandler.fixDouble(multiplier, 2);
	}
	
	public static double getMultiplier(UUID id, BoosterType type) {
		double multi = 1.0;
		for (Booster booster : getBoosters(id, type)) {
			multi = multi * booster.getMultiplier();
		}
		
		return DoubleHandler.fixDouble(multi, 2);
	}
	
	public boolean isPrivate() {
		return this instanceof PrivateBooster;
	}
	
	public boolean isInfinite() {
		return this.infinite;
	}

	public Map<UUID, Integer> getContributors() {
		return this.contributors;
	}
	
	public void sendMessage(Player p, Variable... variables) {
		List<String> messages = QuickSell.local.getTranslation(getMessage());
		if (messages.isEmpty()) return;
		try {
			String message = ChatColor.translateAlternateColorCodes('&', messages.get(0).replace("%multiplier%", String.valueOf(this.multiplier)).replace("%minutes%", String.valueOf(this.formatTime())));
			for (Variable v: variables) {
				message = v.apply(message);
			}
			new TellRawMessage()
			.addText(message)
			.addClickEvent(ClickAction.RUN_COMMAND, "/boosters")
			.addHoverEvent(HoverAction.SHOW_TEXT, BoosterMenu.getTellRawMessage(this))
			.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}