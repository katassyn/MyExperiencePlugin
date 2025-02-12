package com.maks.myexperienceplugin.alchemy;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AlchemyItemMatcher {

    /**
     * Próbuje rozpoznać alchemiczny przedmiot na podstawie jego wyświetlanej nazwy.
     * Jeśli przedmiot pasuje do któregoś z efektów, zwraca klucz (np. "potion5hp").
     *
     * @param item Przedmiot do sprawdzenia
     * @return Klucz identyfikujący efekt lub null, jeśli przedmiot nie pasuje
     */
    public static String matchItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        String display = meta.getDisplayName();

        // Potions (Basic Potions)
        if (display.equals("§aSmall Healing Potion")) {
            return "potion5hp";
        }
        if (display.equals("§aMedium Healing Potion")) {
            return "potion10hp";
        }
        if (display.equals("§aLarge Healing Potion")) {
            return "potion15hp";
        }

        // Potiony (non-stacking, z czasem wygasania)
        if (display.equals("§9[I]§a Small Healing Potion")) {
            return "potionheal_inf";
        }
        if (display.equals("§9[I]§a Small Lasting Healing Potion")) {
            return "potionlasting_inf";
        }
        if (display.equals("§5[II]§a Medium Healing Potion")) {
            return "potionheal_hell";
        }
        if (display.equals("§5[II]§a Medium Lasting Healing Potion")) {
            return "potionlasting_hell";
        }
        if (display.equals("§6[III]§a Large Healing Potion")) {
            return "potionheal_blood";
        }
        if (display.equals("§6[III]§a Large Lasting Healing Potion")) {
            return "potionlasting_blood";
        }
        if (display.equals("§9[I]§a Small Immunity Potion")) {
            return "potionimmun_inf";
        }
        if (display.equals("§5[II]§a Medium Immunity Potion")) {
            return "potionimmun_hell";
        }
        if (display.equals("§6[III]§a Large Immunity Potion")) {
            return "potionimmun_blood";
        }
        if (display.equals("§9[I]§a Small Movement Potion")) {
            return "potionms_inf";
        }
        if (display.equals("§5[II]§a Medium Movement Potion")) {
            return "potionms_hell";
        }
        if (display.equals("§6[III]§a Large Movement Potion")) {
            return "potionms_blood";
        }
        if (display.equals("§9[I]§a Small One Mob Luck Potion")) {
            return "potiononeluck_inf";
        }
        if (display.equals("§5[II]§a Medium One Mob Luck Potion")) {
            return "potionnoneluck_hell";
        }
        if (display.equals("§6[III]§a Large One Mob Luck Potion")) {
            return "potionnoneluck_blood";
        }
        if (display.equals("§9[I]§a Small Absorption Potion")) {
            return "potionabsorption_inf";
        }
        if (display.equals("§5[II]§a Medium Absorption Potion")) {
            return "potionabsorption_hell";
        }
        if (display.equals("§6[III]§a Large Absorption Potion")) {
            return "potionabsorption_blood";
        }
        if (display.equals("§9[I]§a Small Berserk Potion")) {
            return "potionbersek_inf";
        }
        if (display.equals("§5[II]§a Medium Berserk Potion")) {
            return "potionabersek_hell";
        }
        if (display.equals("§6[III]§a Large Berserk Potion")) {
            return "potionbersek_blood";
        }

        // Tonics
        if (display.equals("§9[I]§a Basic Damage Tonic")) {
            return "tonicdmg_inf";
        }
        if (display.equals("§5[II]§a Great Damage Tonic")) {
            return "tonicdmg_hell";
        }
        if (display.equals("§6[III]§a Excellent Damage Tonic")) {
            return "tonicdmg_blood";
        }
        if (display.equals("§9[I]§a Basic Health Tonic")) {
            return "tonichp_inf";
        }
        if (display.equals("§5[II]§a Great Health Tonic")) {
            return "tonichp_hell";
        }
        if (display.equals("§6[III]§a Excellent Health Tonic")) {
            return "tonichp_blood";
        }
        if (display.equals("§9[I]§a Basic Experience Tonic")) {
            return "tonicexp_inf";
        }
        if (display.equals("§5[II]§a Great Experience Tonic")) {
            return "tonicexp_hell";
        }
        if (display.equals("§6[III]§a Excellent Experience Tonic")) {
            return "tonicexp_blood";
        }
        if (display.equals("§9[I]§a Basic Movement Tonic")) {
            return "tonicms_inf";
        }
        if (display.equals("§5[II]§a Great Movement Tonic")) {
            return "tonicms_hell";
        }
        if (display.equals("§6[III]§a Excellent Movement Tonic")) {
            return "tonicms_blood";
        }
        if (display.equals("§9[I]§a Basic Totem Tonic")) {
            return "tonictotem_inf";
        }
        if (display.equals("§5[II]§a Great Totem Tonic")) {
            return "tonictotem_hell";
        }
        if (display.equals("§6[III]§a Excellent Totem Tonic")) {
            return "tonictotem_blood";
        }
        if (display.equals("§9[I]§a Basic Night Vision Tonic")) {
            return "tonicnight_inf";
        }
        if (display.equals("§5[II]§a Great Night Vision Tonic")) {
            return "tonicnight_hell";
        }
        if (display.equals("§6[III]§a Excellent Night Vision Tonic")) {
            return "tonicnight_blood";
        }
        if (display.equals("§9[I]§a Basic Luck Tonic")) {
            return "tonicluck_inf";
        }
        if (display.equals("§5[II]§a Great Luck Tonic")) {
            return "tonicluck_hell";
        }
        if (display.equals("§6[III]§a Excellent Luck Tonic")) {
            return "tonicluck_blood";
        }

        // Phycis effects
        if (display.equals("§9[I]§a Basic Damage Phycis")) {
            return "phycisdmg_inf";
        }
        if (display.equals("§5[II]§a Great Damage Phycis")) {
            return "phycisdmg_hell";
        }
        if (display.equals("§6[III]§a Excellent Damage Phycis")) {
            return "phycisdmg_blood";
        }
        if (display.equals("§9[I]§a Basic Health Phycis")) {
            return "phycishp_inf";
        }
        if (display.equals("§5[II]§a Great Health Phycis")) {
            return "phycishp_hell";
        }
        if (display.equals("§6[III]§a Excellent Health Phycis")) {
            return "phycishp_blood";
        }
        if (display.equals("§9[I]§a Basic Experience Phycis")) {
            return "phycisexp_inf";
        }
        if (display.equals("§5[II]§a Great Experience Phycis")) {
            return "phycisexp_hell";
        }
        if (display.equals("§6[III]§a Excellent Experience Phycis")) {
            return "phycisexp_blood";
        }
        if (display.equals("§9[I]§a Basic Movement Phycis")) {
            return "phycisms_inf";
        }
        if (display.equals("§5[II]§a Great Movement Phycis")) {
            return "phycisms_hell";
        }
        if (display.equals("§6[III]§a Excellent Movement Phycis")) {
            return "phycisms_blood";
        }
        if (display.equals("§9[I]§a Basic Magnet Phycis")) {
            return "phycismagnes_inf";
        }
        if (display.equals("§5[II]§a Great Magnet Phycis")) {
            return "phycismagnes_hell";
        }
        if (display.equals("§6[III]§a Excellent Magnet Phycis")) {
            return "phycismagnes_blood";
        }
        if (display.equals("§9[I]§a Basic Lifesteal Phycis")) {
            return "phycissteal_inf";
        }
        if (display.equals("§5[II]§a Great Lifesteal Phycis")) {
            return "phycissteal_hell";
        }
        if (display.equals("§6[III]§a Excellent Lifesteal Phycis")) {
            return "phycissteal_blood";
        }
        if (display.equals("§9[I]§a Basic Luck Phycis")) {
            return "phycisluck_inf";
        }
        if (display.equals("§5[II]§a Great Luck Phycis")) {
            return "phycisluck_hell";
        }
        if (display.equals("§6[III]§a Excellent Luck Phycis")) {
            return "phycisluck_blood";
        }

        return null;
    }
}
