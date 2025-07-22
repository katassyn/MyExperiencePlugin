package com.maks.myexperienceplugin.alchemy;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.UUID;

public class AlchemyEffectFactory {
    // ID for Potion Master skill in ScaleGuardian (ID 20)
    private static final int POTION_MASTER_SKILL_ID = 400000 + 20;
    
    /**
     * Checks if a player has the Potion Master skill and returns the duration multiplier
     * @param player The player to check
     * @return 1.2 if the player has the Potion Master skill, 1.0 otherwise
     */
    private static double getPotionDurationMultiplier(Player player) {
        MyExperiencePlugin plugin = (MyExperiencePlugin) Bukkit.getPluginManager().getPlugin("MyExperiencePlugin");
        if (plugin == null) return 1.0;
        
        UUID playerId = player.getUniqueId();
        
        // Check if player has the Potion Master skill (ID 20 in ScaleGuardian)
        Set<Integer> purchasedSkills = plugin.getSkillTreeManager().getPurchasedSkills(playerId);
        if (purchasedSkills.contains(POTION_MASTER_SKILL_ID)) {
            // Get purchase count for stacking effect
            int purchaseCount = plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, POTION_MASTER_SKILL_ID);
            // Return 1.2 (20% increase) per purchase
            return 1.0 + (0.2 * purchaseCount);
        }
        
        return 1.0; // No duration increase
    }

    public static AlchemyEffect createEffect(String key, Player player) {
        // Get duration multiplier based on player's skills
        double durationMultiplier = getPotionDurationMultiplier(player);
        switch (key.toLowerCase()) {
            // Potions (Elixiry)
            case "potion5hp":
                return new InstantHealingEffect(player, 5.0, 10 * 1000L, "Small Healing Potion");
            case "potion10hp":
                return new InstantHealingEffect(player, 10.0, 10 * 1000L, "Medium Healing Potion");
            case "potion15hp":
                return new InstantHealingEffect(player, 15.0, 10 * 1000L, "Large Healing Potion");
            case "potionheal_inf":
                return new InstantHealingEffect(player, 50.0, (long)(30 * 1000L * durationMultiplier), "Small Healing Potion [I]", true);
            case "potionlasting_inf":
                return new OverTimeHealingEffect(player, 0.10, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Small Lasting Healing Potion [I]");
            case "potionheal_hell":
                return new InstantHealingEffect(player, 75.0, (long)(30 * 1000L * durationMultiplier), "Medium Healing Potion [II]", true);
            case "potionlasting_hell":
                return new OverTimeHealingEffect(player, 0.15, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Medium Lasting Healing Potion [II]");
            case "potionheal_blood":
                return new InstantHealingEffect(player, 100.0, (long)(30 * 1000L * durationMultiplier), "Large Healing Potion [III]", true);
            case "potionlasting_blood":
                return new OverTimeHealingEffect(player, 0.20, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Large Lasting Healing Potion [III]");

            case "potionimmun_inf":
                return new ImmunityEffect(player, (long)(1 * 1000 * durationMultiplier), 30 * 1000, "Small Immunity Potion [I]");
            case "potionimmun_hell":
                return new ImmunityEffect(player, (long)(2 * 1000 * durationMultiplier), 30 * 1000, "Medium Immunity Potion [II]");
            case "potionimmun_blood":
                return new ImmunityEffect(player, (long)(3 * 1000 * durationMultiplier), 30 * 1000, "Large Immunity Potion [III]");

            case "potionms_inf":
                return new TonicMovementSpeedEffect(player, 0.25, (long)(3 * 1000 * durationMultiplier), 30 * 1000, "Small Movement Potion [I]");
            case "potionms_hell":
                return new TonicMovementSpeedEffect(player, 0.50, (long)(3 * 1000 * durationMultiplier), 30 * 1000, "Medium Movement Potion [II]");
            case "potionms_blood":
                return new TonicMovementSpeedEffect(player, 1.00, (long)(3 * 1000 * durationMultiplier), 30 * 1000, "Large Movement Potion [III]");

            case "potiononeluck_inf":
                return new SingleTargetLuckEffect(player, 10.0, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Small One Mob Luck Potion [I]");
            case "potionnoneluck_hell":
                return new SingleTargetLuckEffect(player, 20.0, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Medium One Mob Luck Potion [II]");
            case "potionnoneluck_blood":
                return new SingleTargetLuckEffect(player, 30.0, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Large One Mob Luck Potion [III]");

            case "potionabsorption_inf":
                return new AbsorptionEffect(player, 10.0, (long)(10 * 1000 * durationMultiplier), 30 * 1000, "Small Absorption Potion [I]");
            case "potionabsorption_hell":
                return new AbsorptionEffect(player, 20.0, (long)(10 * 1000 * durationMultiplier), 30 * 1000, "Medium Absorption Potion [II]");
            case "potionabsorption_blood":
                return new AbsorptionEffect(player, 30.0, (long)(10 * 1000 * durationMultiplier), 30 * 1000, "Large Absorption Potion [III]");

            case "potionbersek_inf":
                return new BerserkerEffect(player, 0.60, 0.60, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Small Berserk Potion [I]");
            case "potionabersek_hell":
                return new BerserkerEffect(player, 0.75, 0.75, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Medium Berserk Potion [II]");
            case "potionbersek_blood":
                return new BerserkerEffect(player, 0.90, 0.90, (long)(5 * 1000 * durationMultiplier), 30 * 1000, "Large Berserk Potion [III]");

            // Tonics
            case "tonicdmg_inf":
                return new TonicDamageEffect(player, 50.0, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Damage Tonic [I]");
            case "tonicdmg_hell":
                return new TonicDamageEffect(player, 100.0, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Damage Tonic [II]");
            case "tonicdmg_blood":
                return new TonicDamageEffect(player, 150.0, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Damage Tonic [III]");

            case "tonichp_inf":
                return new TonicHealthEffect(player, 5.0, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Health Tonic [I]");
            case "tonichp_hell":
                return new TonicHealthEffect(player, 10.0, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Health Tonic [II]");
            case "tonichp_blood":
                return new TonicHealthEffect(player, 15.0, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Health Tonic [III]");

            // Fixed: These should be percentages, not decimals (0.003 = 0.3%, not 3%)
            case "tonicexp_inf":
                return new TonicExpEffect(player, 0.00003, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Experience Tonic [I]");
            case "tonicexp_hell":
                return new TonicExpEffect(player, 0.00003, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Experience Tonic [II]");
            case "tonicexp_blood":
                return new TonicExpEffect(player, 0.00003, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Experience Tonic [III]");

            case "tonicms_inf":
                return new TonicMovementSpeedEffect(player, 0.05, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Movement Tonic [I]");
            case "tonicms_hell":
                return new TonicMovementSpeedEffect(player, 0.10, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Movement Tonic [II]");
            case "tonicms_blood":
                return new TonicMovementSpeedEffect(player, 0.15, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Movement Tonic [III]");

            case "tonictotem_inf":
                return new TonicTotemEffect(player, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Totem Tonic [I]");
            case "tonictotem_hell":
                return new TonicTotemEffect(player, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Totem Tonic [II]");
            case "tonictotem_blood":
                return new TonicTotemEffect(player, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Totem Tonic [III]");

            case "tonicnight_inf":
                return new NightVisionEffect(player, 0, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Night Vision Tonic [I]");
            case "tonicnight_hell":
                return new NightVisionEffect(player, 0, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Night Vision Tonic [II]");
            case "tonicnight_blood":
                return new NightVisionEffect(player, 0, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Night Vision Tonic [III]");

            case "tonicluck_inf":
                return new TonicLuckEffect(player, 1.0, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Luck Tonic [I]");
            case "tonicluck_hell":
                return new TonicLuckEffect(player, 2.0, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Luck Tonic [II]");
            case "tonicluck_blood":
                return new TonicLuckEffect(player, 3.0, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Luck Tonic [III]");

            // Phycis effects
            case "phycisdmg_inf":
                return new PhysisPercentageDamageEffect(player, 0.10, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Damage Phycis [I]");
            case "phycisdmg_hell":
                return new PhysisPercentageDamageEffect(player, 0.15, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Damage Phycis [II]");
            case "phycisdmg_blood":
                return new PhysisPercentageDamageEffect(player, 0.20, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Damage Phycis [III]");

            case "phycishp_inf":
                return new PhysisPercentageHealthEffect(player, 0.05, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Health Phycis [I]");
            case "phycishp_hell":
                return new PhysisPercentageHealthEffect(player, 0.10, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Health Phycis [II]");
            case "phycishp_blood":
                return new PhysisPercentageHealthEffect(player, 0.15, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Health Phycis [III]");

            case "phycisexp_inf":
                return new PhysisPercentageExpEffect(player, 0.00003, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Experience Phycis [I]");
            case "phycisexp_hell":
                return new PhysisPercentageExpEffect(player, 0.00006, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Experience Phycis [II]");
            case "phycisexp_blood":
                return new PhysisPercentageExpEffect(player, 0.00009, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Experience Phycis [III]");

            case "phycisms_inf":
                return new PhysisPercentageMovementEffect(player, 0.05, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Movement Phycis [I]");
            case "phycisms_hell":
                return new PhysisPercentageMovementEffect(player, 0.10, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Movement Phycis [II]");
            case "phycisms_blood":
                return new PhysisPercentageMovementEffect(player, 0.15, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Movement Phycis [III]");

            case "phycismagnes_inf":
                return new PhycisMagnetEffect(player, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Magnet Phycis [I]");
            case "phycismagnes_hell":
                return new PhycisMagnetEffect(player, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Magnet Phycis [II]");
            case "phycismagnes_blood":
                return new PhycisMagnetEffect(player, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Magnet Phycis [III]");

            case "phycissteal_inf":
                return new LifestealEffect(player, 0.01, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Lifesteal Phycis [I]");
            case "phycissteal_hell":
                return new LifestealEffect(player, 0.03, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Lifesteal Phycis [II]");
            case "phycissteal_blood":
                return new LifestealEffect(player, 0.05, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Lifesteal Phycis [III]");

            case "phycisluck_inf":
                return new PhysisPercentageLuckEffect(player, 0.05, (long)(5 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Basic Luck Phycis [I]");
            case "phycisluck_hell":
                return new PhysisPercentageLuckEffect(player, 0.10, (long)(10 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Great Luck Phycis [II]");
            case "phycisluck_blood":
                return new PhysisPercentageLuckEffect(player, 0.15, (long)(15 * 60 * 1000 * durationMultiplier), 15 * 60 * 1000, "Excellent Luck Phycis [III]");

            default:
                return null;
        }
    }
}
