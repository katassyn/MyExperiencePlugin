package com.maks.myexperienceplugin.Class.skills;

import org.bukkit.ChatColor;

/**
 * Centralized class for skill-related messages to separate business logic from presentation.
 * This helps maintain consistency in messages and makes it easier to change message formatting.
 */
public class SkillMessages {
    // Common skill effect messages
    public static final String EVADE_MESSAGE = ChatColor.GREEN + "Evaded!";
    public static final String CRITICAL_HIT = ChatColor.RED + "Critical Hit!";
    public static final String CRITICAL_HIT_WITH_MULTIPLIER = ChatColor.RED + "CRITICAL HIT! " + 
                                                             ChatColor.GOLD + "x%.1f " + 
                                                             ChatColor.YELLOW + "damage";
    
    // Defensive messages
    public static final String BLOCK_MESSAGE = ChatColor.GREEN + "Blocked!";
    public static final String DAMAGE_REDUCED = ChatColor.GREEN + "Damage Reduced!";
    public static final String SHIELD_ACTIVATED = ChatColor.AQUA + "Shield Activated!";
    
    // Offensive messages
    public static final String BONUS_DAMAGE = ChatColor.YELLOW + "+%.1f damage!";
    public static final String POISON_APPLIED = ChatColor.DARK_GREEN + "Poison Applied!";
    public static final String BLEEDING_APPLIED = ChatColor.DARK_RED + "Bleeding Applied!";
    public static final String FIRE_DAMAGE = ChatColor.GOLD + "Fire Damage!";
    
    // Status effect messages
    public static final String SPEED_BOOST = ChatColor.AQUA + "Speed Boost!";
    public static final String STRENGTH_BOOST = ChatColor.RED + "Strength Boost!";
    public static final String RESISTANCE_BOOST = ChatColor.BLUE + "Resistance Boost!";
    
    // Debug messages
    public static final String DEBUG_PREFIX = ChatColor.DARK_GRAY + "[DEBUG] ";
    public static final String DEBUG_ATTACK_EVADED = DEBUG_PREFIX + "Attack evaded...";
    public static final String DEBUG_CRITICAL_HIT = DEBUG_PREFIX + "Critical hit! x%.2f damage (%.1f → %.1f)";
    public static final String DEBUG_BONUS_DAMAGE = DEBUG_PREFIX + "Skill bonus damage: +%.1f";
    public static final String DEBUG_DAMAGE_MULTIPLIER = DEBUG_PREFIX + "Damage multiplier: x%.2f";
    
    /**
     * Format a message with the provided arguments
     * @param message The message template
     * @param args The arguments to format into the message
     * @return The formatted message
     */
    public static String format(String message, Object... args) {
        return String.format(message, args);
    }
}