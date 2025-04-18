package com.maks.myexperienceplugin.Class;

/**
 * Helper class to ensure consistent class name capitalization
 */
public class ClassNameNormalizer {

    /**
     * Normalize class name capitalization for internal use
     *
     * @param className The input class name in any capitalization
     * @return A properly capitalized class name for internal use
     */
    public static String normalize(String className) {
        if (className == null || className.isEmpty()) {
            return "NoClass";
        }

        String lowerCaseName = className.toLowerCase();

        switch (lowerCaseName) {
            case "ranger":
                return "Ranger";
            case "dragonknight":
            case "dragon knight":
            case "dragon_knight":
                return "DragonKnight";
            case "spellweaver":
            case "spell weaver":
            case "spell_weaver":
                return "SpellWeaver";
            default:
                return className; // Return as-is if not recognized
        }
    }

    /**
     * Check if a class name is valid
     *
     * @param className The input class name
     * @return true if the class name is valid
     */
    public static boolean isValidClass(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }

        String normalizedName = normalize(className);
        return normalizedName.equals("Ranger") ||
                normalizedName.equals("DragonKnight") ||
                normalizedName.equals("SpellWeaver");
    }
}