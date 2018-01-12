package me.mrCookieSlime.QuickSell.boosters;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

public class PrivateBooster extends Booster {

	@Deprecated
	public PrivateBooster(String owner, double multiplier, int minutes) {
		super(owner, multiplier, minutes);
	}
	
	@Deprecated
	public PrivateBooster(BoosterType type, String owner, double multiplier, int minutes) {
		super(type, owner, multiplier, minutes);
	}
	
	public PrivateBooster(BoosterType type, UUID owner, double multiplier, int minutes) {
		super(type, owner, multiplier, minutes);
	}
	
	public PrivateBooster(int id) throws ParseException {
		super(id);
	}
	
	@Override
	@Deprecated
	public List<String> getAppliedPlayers() {
		return Arrays.asList(Bukkit.getOfflinePlayer(this.owner).getName());
	}
	
	public List<UUID> getAppliedIds() {
		return Arrays.asList(this.owner);
	}
	
	@Override
	public String getMessage() {
		return "messages.pbooster-use." + type.toString();
	}

}
