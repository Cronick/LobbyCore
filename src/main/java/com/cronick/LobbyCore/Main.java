package com.cronick.LobbyCore;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Main extends JavaPlugin implements Listener {

    /*
     * SpawnOnJoin
     * No Hunger
     * Double Jump
     * VoidToSpawn
     * No Drop
     * No Item Movement
     * Speed Boost
     * Jump Boost
     * No Block Placement
     * No Fall Damage
     * Give Player Head on join
     * Give Ender Chest on join
     * Give Chest on join
     * Player head command with config
     * Ender Chest command with config
     * Chest command with config
     * Reload command
     */

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("SpawnOnJoin.setYaw", Integer.valueOf(0));
        getConfig().addDefault("SpawnOnJoin.setPitch", Integer.valueOf(0));
        getConfig().addDefault("PlayerHead.Name", "&a%player%'s Head");
        getConfig().addDefault("PlayerHead.Slot", Integer.valueOf(9));
        getConfig().addDefault("PlayerHead.Command", "help");
        saveConfig();
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Integer getYaw = getConfig().getInt("SpawnOnJoin.setYaw") - 180;
        Integer getPitch = getConfig().getInt("SpawnOnJoin.setPitch");

        Location location = event.getPlayer().getWorld().getSpawnLocation().add(0.5d, 0.0d, 0.5d);
        location.setYaw(getYaw);
        location.setPitch(getPitch);
        event.getPlayer().teleport(location);

        Player player = event.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999, 3));

        GiveHead(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onFoodLevelChange (FoodLevelChangeEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerDamage (EntityDamageEvent event) {
        if (event.getEntityType () != EntityType.PLAYER) return;
        if (event.getCause () != EntityDamageEvent.DamageCause.VOID) return;
        Player player = ((Player) event.getEntity ());

        event.setCancelled (true);
        player.damage ((player.getHealth () + 1.0f));

        if (event.getEntityType () != EntityType.PLAYER) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerDeath (PlayerDeathEvent event) {
        event.getEntity ().spigot ().respawn();
    }

    private void groundUpdate (Player player) {
        Location location = player.getPlayer ().getLocation ();
        location = location.subtract (0, 1, 0);

        Block block = location.getBlock ();
        if (!block.getType ().isSolid ()) return;

        player.setAllowFlight(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerMove (PlayerMoveEvent event) {
        if (event.getPlayer ().getAllowFlight ()) return;
        this.groundUpdate(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerToggleFlight (PlayerToggleFlightEvent event) {
        if (event.getPlayer ().getGameMode () == GameMode.CREATIVE || event.getPlayer ().getGameMode () == GameMode.SPECTATOR) return;

        event.setCancelled (true);
        event.getPlayer ().setAllowFlight (false);
        event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.6d).setY(1.0d));
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void invClick(InventoryClickEvent e)
    {
        Player player = (Player)e.getWhoClicked();
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
            event.setCancelled(true);
    }

    public void GiveHead(Player player)
    {
        ItemStack PlayerHead = new ItemStack(Material.SKULL_ITEM, 1);
        PlayerHead.setDurability((short)3);
        SkullMeta PlayerHeadMeta = (SkullMeta)PlayerHead.getItemMeta();
        String Name = getConfig().getString("PlayerHead.Name").replaceAll("&", "§").replaceAll("%player%", player.getName());
        Integer Slot = getConfig().getInt("PlayerHead.Slot") - 1;

        PlayerHeadMeta.setDisplayName(Name);
        PlayerHeadMeta.setOwner(player.getName());
        PlayerHead.setItemMeta(PlayerHeadMeta);
        player.getInventory().setItem(Slot, PlayerHead);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) && (event.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) && (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(getConfig().getString("PlayerHead.Name").replaceAll("&", "§").replaceAll("%player%", event.getPlayer().getName())))) {
            Player player = event.getPlayer();
            Bukkit.dispatchCommand(player, getConfig().getString("PlayerHead.Command").replaceAll("%player%", event.getPlayer().getName()));
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("lobbyreload")) {
            if (sender.hasPermission("lobby.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "LobbyCore Reloaded!"));
                reloadConfig();
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "No Permission!"));
            }
        }
        return false;
    }
}
