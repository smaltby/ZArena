package com.github.zarena.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public enum Permissions
{
	/**
     * The node for admin commands. You get all below permissions if you have this
     */
    ADMIN,
    /**
     * Lets players create and edit levels
     */
    LEVEL_EDITOR,
    /**
     * Lets player control games
     */
    GAME_CONTROL,
    /**
     * Allows player to set the kills of other players
     */
    KILLCOUNTER_CONTROL,
    /**
     * Lets player enter and leave game at will
     */
    ENTER_LEAVE,
    /**
     * Lets player vote
     */
    VOTER;

    public static Map<String, Integer> startMoneyPermissions = new HashMap<String, Integer>();
    public static Map<String, Integer> extraVotesPermissions = new HashMap<String, Integer>();
    
    @Override
    public String toString()
    {
        switch(this)
        {
            case ADMIN:
                return "zarena.admin";
            case LEVEL_EDITOR:
                return "zarena.leveleditor";
            case GAME_CONTROL:
                return "zarena.gamecontrol";
            case KILLCOUNTER_CONTROL:
            	return "zarena.killcountercontrol";
            case ENTER_LEAVE:
                return "zarena.enterleave";
            case VOTER:
                return "zarena.voter";
            default:
            	return null;
        }
    }

    /**
     * Registers all permissions in the plugin manager
     * @param pm the plugin manager in which to register permissions
     */
    public static void registerPermNodes(PluginManager pm) {
        try
        {
            pm.addPermission(new Permission(ADMIN.toString()));
            pm.addPermission(new Permission(LEVEL_EDITOR.toString()));
            pm.addPermission(new Permission(GAME_CONTROL.toString()));
            pm.addPermission(new Permission(ENTER_LEAVE.toString()));
            pm.addPermission(new Permission(VOTER.toString()));
        } catch (Exception e)
        {
        }

    }
    
    /**
     * Registers all config specified donator permissions in the plugin manager
     * @param pm the plugin manager in which to register permissions
     */
    public static void registerDonatorPermNodes(PluginManager pm) {
    	try
        {
            for(String permission : startMoneyPermissions.keySet())
            {
            	pm.addPermission(new Permission(permission));
            }
            for(String permission : extraVotesPermissions.keySet())
            {
            	pm.addPermission(new Permission(permission));
            }
        } catch (Exception e)
        {
        }
    }
}
