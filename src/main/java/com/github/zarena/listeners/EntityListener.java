package com.github.zarena.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.github.customentitylibrary.entities.CustomEntityWrapper;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;

import com.github.zarena.GameHandler;
import com.github.zarena.PlayerStats;
import com.github.zarena.ZArena;
import com.github.zarena.entities.ZEntityType;
import com.github.zarena.utils.ChatHelper;
import com.github.zarena.utils.Constants;
import com.github.zarena.utils.Message;
import com.github.zarena.utils.Utils;

public class EntityListener implements Listener
{
	private ZArena plugin;
	private GameHandler gameHandler;

	public EntityListener()
	{
		plugin = ZArena.getInstance();
		gameHandler = plugin.getGameHandler();
	}

	public void registerEvents(PluginManager pm, ZArena plugin)
	{
		pm.registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if(plugin.getConfig().getBoolean(Constants.WORLD_EXCLUSIVE))
		{
			if(event.getLocation().getWorld().getName().equals(plugin.getConfig().getString(Constants.GAME_WORLD)) && event.getSpawnReason() != SpawnReason.CUSTOM)
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event)
	{
		Entity ent = event.getEntity();
		if(ent instanceof Player)
		{
            //Set the player as dead, and sub the amount of money that the config defines
			Player player = (Player) event.getEntity();
			PlayerStats stats = gameHandler.getPlayerStats().get(player.getName());
			if(stats != null)
			{
				if(stats.isAlive())
				{
					stats.setAlive(false);
					stats.subMoney(stats.getMoney() * plugin.getConfig().getDouble(Constants.MONEY_LOST));
					stats.registerDeath();
                    //Broadcast the players death
                    if(gameHandler.getAliveCount() > 0)
                    {
                        ChatHelper.broadcastMessage(Message.ON_PLAYER_DEATH_GLOBAL.formatMessage(player.getName(),
                                gameHandler.getAliveCount()), gameHandler.getBroadcastPlayers());
                    }
				}
			}
		}
		else if(CustomEntityWrapper.instanceOf(ent))
		{
			if(plugin.getConfig().getBoolean(Constants.XP_BAR_IS_MONEY))
				event.setDroppedExp(0);
			CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(ent);
			ZEntityType type = (ZEntityType) customEnt.getType();

			double moneyModifier = type.getWorthModifier();

			Player bestAttacker = customEnt.getBestAttacker();
			if(bestAttacker != null)
			{
				PlayerStats stats = gameHandler.getPlayerStats().get(customEnt.getBestAttacker().getName());

				//Give the killer money/points
				if(stats != null && stats.isAlive())
				{
					stats.addMoney(plugin.getConfig().getInt(Constants.KILL_MONEY) * moneyModifier);
					stats.addPoints(1);
					stats.messageStats();
					if(moneyModifier > 1)
						ChatHelper.sendMessage(Message.BONUS_MONEY_KILL.formatMessage(moneyModifier, type.toString()), stats.getPlayer());

					//If the gamemode is no buying, there is a chance for the player to get an item drop from the killed zombie
					if(gameHandler.getGameMode().isScavenger())
					{
						double itemChance = type.getWorthModifier() / 10.0;
						double weight = type.getWorthModifier();

						if(Utils.getWeaponCount(stats.getPlayer().getInventory()) == 0)
							itemChance *= 4;
						else if(Utils.getWeaponCount(stats.getPlayer().getInventory()) == 1)
							itemChance *= 3;

						if(Math.random() <= itemChance)
							giveRandomItem(stats.getPlayer(), weight);
					}
				}
			}

			//Give the assister half money/points
			Player assister = customEnt.getAssistAttacker();
			if(assister != null)
			{
				PlayerStats stats = gameHandler.getPlayerStats().get(assister.getName());
				if(stats != null && stats.isAlive())
				{
					stats.addMoney(((double) plugin.getConfig().getInt(Constants.KILL_MONEY)) / 2 * moneyModifier);
					stats.messageStats();
					ChatHelper.sendMessage(Message.ASSIST_KILL.formatMessage(moneyModifier/2, type.toString()), stats.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void clearDrops(EntityDeathEvent event)
	{
		if(CustomEntityWrapper.instanceOf(event.getEntity()))
			event.getDrops().clear();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if(event.isCancelled())
			return;
		Entity ent = event.getEntity();
		Entity damager = event.getDamager();
		if(CustomEntityWrapper.instanceOf(damager))
			event.setDamage((int) (event.getDamage() * gameHandler.getGameMode().getDamageModifier()));
        else if (damager instanceof Projectile)
		{
			Projectile pj = (Projectile) event.getDamager();
			damager = pj.getShooter();
			if(CustomEntityWrapper.instanceOf(damager) && CustomEntityWrapper.instanceOf(ent))	//If a custom mob shot another custom mob
			{
				event.setCancelled(true);
				return;
			}
			if(ent instanceof Player)
			{
				if(((Player) ent).isBlocking())
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFoodLevelChange(FoodLevelChangeEvent event)
	{
		if(plugin.getConfig().getBoolean(Constants.DISABLE_HUNGER) && plugin.getGameHandler().getPlayers().contains(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityRegainHealth(EntityRegainHealthEvent event)
	{
		if(plugin.getConfig().getBoolean(Constants.DISABLE_HUNGER) && plugin.getGameHandler().getPlayers().contains(event.getEntity()) && event.getRegainReason() == RegainReason.SATIATED)
			event.setCancelled(true);
	}

	//TODO the methods involving getting item drops require simplification.
	//For use in the below method, giveRandomItem
	private boolean chooseItem(Map<ItemStack, Integer> itemRarity, Player p, int weight)
	{
		Random rnd = new Random();
		for(Entry<ItemStack, Integer> e:itemRarity.entrySet())
		{
			ItemStack i = e.getKey();
			int rarity = e.getValue();
			if(Math.abs(weight - rarity) > 4)
				continue;
			double diff = Math.abs(weight - rarity) * 2;
			double chance = (1d + (diff/5d) * 2) * itemRarity.size();
			if(rnd.nextInt((int)chance) == 0)
			{
				p.getInventory().addItem(i);
				ChatHelper.sendMessage(Message.ITEM_DROP.formatMessage(), p);
				if(rnd.nextInt(10) == 0)
					p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 16));
				return true;
			}
		}
		return false;
	}

	/**
	 * Gives a player a random item drop.
	 * @param p the player to recieve the drop
	 * @param weight the value of the drop
	 */
	private void giveRandomItem(Player p, double weight)
	{
		Map<ItemStack, Integer> itemRarity = new HashMap<ItemStack, Integer>();
		PlayerInventory pm = p.getInventory();
		boolean hasBow = pm.contains(Material.BOW);
		int weaponCount = Utils.getWeaponCount(pm);
		//Increases the chances of getting a weapon if you only have one.
		int multiplier = 1;
		if(weaponCount == 1)
			multiplier = 3;
		for(int i = 0; i < multiplier; i++)
		{
			ItemStack woodSword = new ItemStack(Material.WOOD_SWORD);
			itemRarity.put(woodSword, 2);
			ItemStack stoneSword = new ItemStack(Material.STONE_SWORD);
			itemRarity.put(stoneSword, 3);
			ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
			itemRarity.put(ironSword, 5);
			ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
			itemRarity.put(diamondSword, 10);
			ItemStack woodAxe = new ItemStack(Material.WOOD_AXE);
			itemRarity.put(woodAxe, 1);
			ItemStack stoneAxe = new ItemStack(Material.STONE_AXE);
			itemRarity.put(stoneAxe, 2);
		}
		//If the player has no weapons, he can ONLY get weapons. IE none of the stuff below.
		if(weaponCount > 1)
		{
			ItemStack lh = new ItemStack(Material.LEATHER_HELMET);
			itemRarity.put(lh, 1);
			ItemStack lc = new ItemStack(Material.LEATHER_CHESTPLATE);
			itemRarity.put(lc, 2);
			ItemStack ll = new ItemStack(Material.LEATHER_LEGGINGS);
			itemRarity.put(ll, 2);
			ItemStack lb = new ItemStack(Material.LEATHER_BOOTS);
			itemRarity.put(lb, 1);
			ItemStack ih = new ItemStack(Material.IRON_HELMET);
			itemRarity.put(ih, 3);
			ItemStack ic = new ItemStack(Material.IRON_CHESTPLATE);
			itemRarity.put(ic, 5);
			ItemStack il = new ItemStack(Material.IRON_LEGGINGS);
			itemRarity.put(il, 5);
			ItemStack ib = new ItemStack(Material.IRON_BOOTS);
			itemRarity.put(ib, 3);
			ItemStack dh = new ItemStack(Material.DIAMOND_HELMET);
			itemRarity.put(dh, 7);
			ItemStack dc = new ItemStack(Material.DIAMOND_CHESTPLATE);
			itemRarity.put(dc, 8);
			ItemStack dl = new ItemStack(Material.DIAMOND_LEGGINGS);
			itemRarity.put(dl, 8);
			ItemStack db = new ItemStack(Material.DIAMOND_BOOTS);
			itemRarity.put(db, 7);
			if(pm.contains(Material.ARROW))
			{
				int index = pm.first(Material.ARROW);
				if(pm.getItem(index).getAmount() > 32 && !(pm.contains(Material.BOW)))
				{
					for(int a = 0; a < 3; a++)
					{
						ItemStack bow = new ItemStack(Material.BOW);
						itemRarity.put(bow, 4);
					}
				}
			}
			ItemStack bow = new ItemStack(Material.BOW);
			itemRarity.put(bow, 5);
			double arrowMax = (hasBow) ? 10.5 : 3.5;
			for(double i = 1; i < arrowMax; i+=.5)
			{
				ItemStack arrow = new ItemStack(Material.ARROW, (int) i * 4);
				itemRarity.put(arrow, (int) Math.floor(i/1.5+1));
			}
			ItemStack healingPotion = new ItemStack(Material.POTION, 1, (short) 8229);
			itemRarity.put(healingPotion, 2);
			ItemStack regenerationPotion = new ItemStack(Material.POTION, 1, (short) 8193);
			itemRarity.put(regenerationPotion, 2);
			ItemStack strengthPotion = new ItemStack(Material.POTION, 1, (short) 8233);
			itemRarity.put(strengthPotion, 1);
			ItemStack swiftnessPotion = new ItemStack(Material.POTION, 1, (short) 8194);
			itemRarity.put(swiftnessPotion, 1);
			ItemStack healingSplashPotion = new ItemStack(Material.POTION, 1, (short) 16421);
			itemRarity.put(healingSplashPotion, 3);
			ItemStack fireSword = new ItemStack(Material.IRON_SWORD);
			fireSword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
			itemRarity.put(fireSword, 10);
			ItemStack sharpSword = new ItemStack(Material.IRON_SWORD);
			sharpSword.addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
			itemRarity.put(sharpSword, 10);
			ItemStack godSword = new ItemStack(Material.DIAMOND_SWORD);
			godSword.addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
			godSword.addEnchantment(Enchantment.KNOCKBACK, 1);
			godSword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
			itemRarity.put(godSword, 14);
		}
		boolean chosen = false;
		while(!chosen)
		{
			chosen = chooseItem(itemRarity, p, (int) weight);
		}
	}
}