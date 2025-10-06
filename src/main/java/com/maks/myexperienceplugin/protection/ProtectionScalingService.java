package com.maks.myexperienceplugin.protection;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Centralized calculator for Protection enchantment scaling. This allows
 * sharing the same calculations between runtime damage adjustments and
 * presentation layers such as player stat GUIs.
 */
public class ProtectionScalingService {

    private final MyExperiencePlugin plugin;

    private boolean enabled;
    private int baseLevel;
    private double baseReduction;
    private double maxCap;
    private int maxTotalLevel;
    private double kFactor;

    public ProtectionScalingService(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("protection-scaling.enabled", true);
        baseLevel = Math.max(0, config.getInt("protection-scaling.base-level", 16));
        baseReduction = clamp(config.getDouble("protection-scaling.base-reduction", 0.64), 0.0, 1.0);
        maxCap = clamp(config.getDouble("protection-scaling.max-cap", 0.80), 0.0, 1.0);
        maxTotalLevel = Math.max(0, config.getInt("protection-scaling.max-total-level", 800));
        kFactor = Math.max(0.0, config.getDouble("protection-scaling.k-factor", 0.102));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ProtectionScalingResult calculateFor(Player player) {
        if (player == null) {
            return ProtectionScalingResult.empty();
        }

        int totalProt = 0;
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        if (armorContents != null) {
            for (ItemStack armor : armorContents) {
                if (armor == null) {
                    continue;
                }
                totalProt += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            }
        }

        return calculate(totalProt);
    }

    public ProtectionScalingResult calculate(int totalProtectionLevel) {
        int rawTotal = Math.max(0, totalProtectionLevel);
        int clampedTotal = maxTotalLevel > 0 ? Math.min(rawTotal, maxTotalLevel) : rawTotal;

        int vanillaEPF = Math.min(rawTotal, 20);
        double vanillaMultiplier = 1.0 - (vanillaEPF * 0.04);
        if (vanillaMultiplier < 0.0) {
            vanillaMultiplier = 0.0;
        }
        double vanillaReduction = clamp(1.0 - vanillaMultiplier, 0.0, 1.0);

        if (!enabled || rawTotal <= 0) {
            return new ProtectionScalingResult(
                    rawTotal,
                    clampedTotal,
                    0,
                    vanillaMultiplier,
                    vanillaMultiplier,
                    vanillaReduction,
                    vanillaReduction
            );
        }

        double baseMultiplier = 1.0;
        if (baseLevel > 0 && baseReduction > 0.0) {
            int baseProt = Math.min(clampedTotal, baseLevel);
            if (baseLevel > 0) {
                double baseRatio = (double) baseProt / (double) baseLevel;
                baseMultiplier -= baseReduction * baseRatio;
            }
        }
        baseMultiplier = clamp(baseMultiplier, 0.0, 1.0);

        int extraLevels = Math.max(0, clampedTotal - baseLevel);
        double extraMultiplier = 1.0;
        if (extraLevels > 0 && kFactor > 0.0) {
            extraMultiplier = 100.0 / (100.0 + kFactor * extraLevels);
        }

        double finalMultiplier = clamp(baseMultiplier * extraMultiplier, 0.0, 1.0);
        double minMultiplier = maxCap >= 1.0 ? 0.0 : 1.0 - maxCap;
        if (minMultiplier < 0.0) {
            minMultiplier = 0.0;
        }
        if (finalMultiplier < minMultiplier) {
            finalMultiplier = minMultiplier;
        }

        double finalReduction = clamp(1.0 - finalMultiplier, 0.0, 1.0);

        return new ProtectionScalingResult(
                rawTotal,
                clampedTotal,
                extraLevels,
                vanillaMultiplier,
                finalMultiplier,
                vanillaReduction,
                finalReduction
        );
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static class ProtectionScalingResult {
        private static final ProtectionScalingResult EMPTY = new ProtectionScalingResult(0, 0, 0, 1.0, 1.0, 0.0, 0.0);

        private final int totalProtectionLevels;
        private final int clampedProtectionLevels;
        private final int extraProtectionLevels;
        private final double vanillaMultiplier;
        private final double finalMultiplier;
        private final double vanillaReduction;
        private final double finalReduction;

        public ProtectionScalingResult(int totalProtectionLevels,
                                       int clampedProtectionLevels,
                                       int extraProtectionLevels,
                                       double vanillaMultiplier,
                                       double finalMultiplier,
                                       double vanillaReduction,
                                       double finalReduction) {
            this.totalProtectionLevels = totalProtectionLevels;
            this.clampedProtectionLevels = clampedProtectionLevels;
            this.extraProtectionLevels = extraProtectionLevels;
            this.vanillaMultiplier = vanillaMultiplier;
            this.finalMultiplier = finalMultiplier;
            this.vanillaReduction = vanillaReduction;
            this.finalReduction = finalReduction;
        }

        public static ProtectionScalingResult empty() {
            return EMPTY;
        }

        public boolean hasProtection() {
            return totalProtectionLevels > 0;
        }

        public int totalProtectionLevels() {
            return totalProtectionLevels;
        }

        public int clampedProtectionLevels() {
            return clampedProtectionLevels;
        }

        public int extraProtectionLevels() {
            return extraProtectionLevels;
        }

        public double vanillaMultiplier() {
            return vanillaMultiplier;
        }

        public double finalMultiplier() {
            return finalMultiplier;
        }

        public double vanillaReduction() {
            return vanillaReduction;
        }

        public double finalReduction() {
            return finalReduction;
        }

        public double safeVanillaMultiplier() {
            return vanillaMultiplier <= 0.0 ? 0.0001 : vanillaMultiplier;
        }

        public double adjustmentRatio() {
            return finalMultiplier / safeVanillaMultiplier();
        }
    }
}
