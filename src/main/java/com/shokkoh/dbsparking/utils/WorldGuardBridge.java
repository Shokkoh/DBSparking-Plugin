package com.shokkoh.dbsparking.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardBridge {

	private WorldGuardPlugin worldGuard = null;
	private boolean enabled = false;

	public WorldGuardBridge() {
		Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (wgPlugin instanceof WorldGuardPlugin) {
			this.worldGuard = (WorldGuardPlugin) wgPlugin;
			this.enabled = true;
		}
	}
	public boolean isPvpDenied(Player attacker, Player victim) {
		if (!enabled) {
			return false;
		}
		return isPvpDeniedAt(attacker.getLocation()) || isPvpDeniedAt(victim.getLocation());
	}

	private boolean isPvpDeniedAt(Location loc) {
		World world = loc.getWorld();
		if (world == null) return false;

		RegionManager regionManager = worldGuard.getRegionManager(world);
		if (regionManager == null) return false;

		ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(loc);
		return applicableRegions.queryState(null, DefaultFlag.PVP) == StateFlag.State.DENY;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
