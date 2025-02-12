package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

public class AlchemyEffectFactory {

    public static AlchemyEffect createEffect(String key, Player player) {
        switch (key.toLowerCase()) {
            // Potions (Elixiry)
            case "potion5hp":
                return new InstantHealingEffect(player, 0.5, 10 * 1000, "Small Healing Potion");
            case "potion10hp":
                return new InstantHealingEffect(player, 1.0, 10 * 1000, "Medium Healing Potion");
            case "potion15hp":
                return new InstantHealingEffect(player, 1.5, 10 * 1000, "Large Healing Potion");

            case "potionheal_inf":
                return new InstantHealingEffect(player, 0.5, 30 * 1000, "Small Healing Potion [I]");
            case "potionlasting_inf":
                return new OverTimeHealingEffect(player, 0.10, 5 * 1000, 30 * 1000, "Small Lasting Healing Potion [I]");
            case "potionheal_hell":
                return new InstantHealingEffect(player, 0.75, 30 * 1000, "Medium Healing Potion [II]");
            case "potionlasting_hell":
                return new OverTimeHealingEffect(player, 0.15, 5 * 1000, 30 * 1000, "Medium Lasting Healing Potion [II]");
            case "potionheal_blood":
                return new InstantHealingEffect(player, 1.0, 30 * 1000, "Large Healing Potion [III]");
            case "potionlasting_blood":
                return new OverTimeHealingEffect(player, 0.20, 5 * 1000, 30 * 1000, "Large Lasting Healing Potion [III]");

            case "potionimmun_inf":
                return new ImmunityEffect(player, 1 * 1000, 30 * 1000, "Small Immunity Potion [I]");
            case "potionimmun_hell":
                return new ImmunityEffect(player, 2 * 1000, 30 * 1000, "Medium Immunity Potion [II]");
            case "potionimmun_blood":
                return new ImmunityEffect(player, 3 * 1000, 30 * 1000, "Large Immunity Potion [III]");

            case "potionms_inf":
                return new TonicMovementSpeedEffect(player, 0.25, 3 * 1000, 30 * 1000, "Small Movement Potion [I]");
            case "potionms_hell":
                return new TonicMovementSpeedEffect(player, 0.50, 3 * 1000, 30 * 1000, "Medium Movement Potion [II]");
            case "potionms_blood":
                return new TonicMovementSpeedEffect(player, 1.00, 3 * 1000, 30 * 1000, "Large Movement Potion [III]");

            case "potiononeluck_inf":
                return new SingleTargetLuckEffect(player, 10.0, 5 * 1000, 30 * 1000, "Small One Mob Luck Potion [I]");
            case "potionnoneluck_hell":
                return new SingleTargetLuckEffect(player, 20.0, 5 * 1000, 30 * 1000, "Medium One Mob Luck Potion [II]");
            case "potionnoneluck_blood":
                return new SingleTargetLuckEffect(player, 30.0, 5 * 1000, 30 * 1000, "Large One Mob Luck Potion [III]");

            case "potionabsorption_inf":
                return new AbsorptionEffect(player, 10.0, 10 * 1000, 30 * 1000, "Small Absorption Potion [I]");
            case "potionabsorption_hell":
                return new AbsorptionEffect(player, 20.0, 10 * 1000, 30 * 1000, "Medium Absorption Potion [II]");
            case "potionabsorption_blood":
                return new AbsorptionEffect(player, 30.0, 10 * 1000, 30 * 1000, "Large Absorption Potion [III]");

            case "potionbersek_inf":
                return new BerserkerEffect(player, 0.60, 0.60, 5 * 1000, 30 * 1000, "Small Berserk Potion [I]");
            case "potionabersek_hell":
                return new BerserkerEffect(player, 0.75, 0.75, 5 * 1000, 30 * 1000, "Medium Berserk Potion [II]");
            case "potionbersek_blood":
                return new BerserkerEffect(player, 0.90, 0.90, 5 * 1000, 30 * 1000, "Large Berserk Potion [III]");

            // Tonics
            case "tonicdmg_inf":
                return new TonicDamageEffect(player, 50.0, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Damage Tonic [I]");
            case "tonicdmg_hell":
                return new TonicDamageEffect(player, 100.0, 10 * 60 * 1000, 15 * 60 * 1000, "Great Damage Tonic [II]");
            case "tonicdmg_blood":
                return new TonicDamageEffect(player, 150.0, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Damage Tonic [III]");

            case "tonichp_inf":
                return new TonicHealthEffect(player, 5.0, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Health Tonic [I]");
            case "tonichp_hell":
                return new TonicHealthEffect(player, 10.0, 10 * 60 * 1000, 15 * 60 * 1000, "Great Health Tonic [II]");
            case "tonichp_blood":
                return new TonicHealthEffect(player, 15.0, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Health Tonic [III]");

            case "tonicexp_inf":
                return new TonicExpEffect(player, 0.001, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Experience Tonic [I]");
            case "tonicexp_hell":
                return new TonicExpEffect(player, 0.002, 10 * 60 * 1000, 15 * 60 * 1000, "Great Experience Tonic [II]");
            case "tonicexp_blood":
                return new TonicExpEffect(player, 0.003, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Experience Tonic [III]");

            case "tonicms_inf":
                return new TonicMovementSpeedEffect(player, 0.05, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Movement Tonic [I]");
            case "tonicms_hell":
                return new TonicMovementSpeedEffect(player, 0.10, 10 * 60 * 1000, 15 * 60 * 1000, "Great Movement Tonic [II]");
            case "tonicms_blood":
                return new TonicMovementSpeedEffect(player, 0.15, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Movement Tonic [III]");

            case "tonictotem_inf":
                return new TonicTotemEffect(player, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Totem Tonic [I]");
            case "tonictotem_hell":
                return new TonicTotemEffect(player, 10 * 60 * 1000, 15 * 60 * 1000, "Great Totem Tonic [II]");
            case "tonictotem_blood":
                return new TonicTotemEffect(player, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Totem Tonic [III]");

            case "tonicnight_inf":
                return new NightVisionEffect(player, 0, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Night Vision Tonic [I]");
            case "tonicnight_hell":
                return new NightVisionEffect(player, 0, 10 * 60 * 1000, 15 * 60 * 1000, "Great Night Vision Tonic [II]");
            case "tonicnight_blood":
                return new NightVisionEffect(player, 0, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Night Vision Tonic [III]");

            case "tonicluck_inf":
                return new TonicLuckEffect(player, 1.0, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Luck Tonic [I]");
            case "tonicluck_hell":
                return new TonicLuckEffect(player, 2.0, 10 * 60 * 1000, 15 * 60 * 1000, "Great Luck Tonic [II]");
            case "tonicluck_blood":
                return new TonicLuckEffect(player, 3.0, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Luck Tonic [III]");

            // Phycis effects
            case "phycisdmg_inf":
                return new PhysisPercentageDamageEffect(player, 0.10, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Damage Phycis [I]");
            case "phycisdmg_hell":
                return new PhysisPercentageDamageEffect(player, 0.15, 10 * 60 * 1000, 15 * 60 * 1000, "Great Damage Phycis [II]");
            case "phycisdmg_blood":
                return new PhysisPercentageDamageEffect(player, 0.20, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Damage Phycis [III]");

            case "phycishp_inf":
                return new PhysisPercentageHealthEffect(player, 0.05, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Health Phycis [I]");
            case "phycishp_hell":
                return new PhysisPercentageHealthEffect(player, 0.10, 10 * 60 * 1000, 15 * 60 * 1000, "Great Health Phycis [II]");
            case "phycishp_blood":
                return new PhysisPercentageHealthEffect(player, 0.15, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Health Phycis [III]");

            case "phycisexp_inf":
                return new PhysisPercentageExpEffect(player, 0.05, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Experience Phycis [I]");
            case "phycisexp_hell":
                return new PhysisPercentageExpEffect(player, 0.10, 10 * 60 * 1000, 15 * 60 * 1000, "Great Experience Phycis [II]");
            case "phycisexp_blood":
                return new PhysisPercentageExpEffect(player, 0.15, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Experience Phycis [III]");

            case "phycisms_inf":
                return new PhysisPercentageMovementEffect(player, 0.05, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Movement Phycis [I]");
            case "phycisms_hell":
                return new PhysisPercentageMovementEffect(player, 0.10, 10 * 60 * 1000, 15 * 60 * 1000, "Great Movement Phycis [II]");
            case "phycisms_blood":
                return new PhysisPercentageMovementEffect(player, 0.15, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Movement Phycis [III]");

            case "phycismagnes_inf":
                return new PhycisMagnetEffect(player, 15 * 60 * 1000, "Basic Magnet Phycis [I]");
            case "phycismagnes_hell":
                return new PhycisMagnetEffect(player, 15 * 60 * 1000, "Great Magnet Phycis [II]");
            case "phycismagnes_blood":
                return new PhycisMagnetEffect(player, 15 * 60 * 1000, "Excellent Magnet Phycis [III]");

            case "phycissteal_inf":
                return new LifestealEffect(player, 0.01, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Lifesteal Phycis [I]");
            case "phycissteal_hell":
                return new LifestealEffect(player, 0.03, 10 * 60 * 1000, 15 * 60 * 1000, "Great Lifesteal Phycis [II]");
            case "phycissteal_blood":
                return new LifestealEffect(player, 0.05, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Lifesteal Phycis [III]");

            case "phycisluck_inf":
                return new PhysisPercentageLuckEffect(player, 0.05, 5 * 60 * 1000, 15 * 60 * 1000, "Basic Luck Phycis [I]");
            case "phycisluck_hell":
                return new PhysisPercentageLuckEffect(player, 0.10, 10 * 60 * 1000, 15 * 60 * 1000, "Great Luck Phycis [II]");
            case "phycisluck_blood":
                return new PhysisPercentageLuckEffect(player, 0.15, 15 * 60 * 1000, 15 * 60 * 1000, "Excellent Luck Phycis [III]");

            default:
                return null;
        }
    }
}
