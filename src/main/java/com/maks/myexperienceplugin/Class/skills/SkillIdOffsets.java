package com.maks.myexperienceplugin.Class.skills;

/**
 * Centralized class for managing skill ID offsets to prevent conflicts
 * between different class skill trees.
 */
public class SkillIdOffsets {
    // Base class offsets (1-99 reserved for base class skills)
    public static final int BASE_SKILLS_MAX = 99;
    
    // Ranger ascendancy offsets
    public static final int BEASTMASTER_OFFSET = 100000;
    public static final int SHADOWSTALKER_OFFSET = 500000;
    public static final int EARTHWARDEN_OFFSET = 600000;
    
    // DragonKnight ascendancy offsets
    public static final int BERSERKER_OFFSET = 200000;
    public static final int FLAMEWARDEN_OFFSET = 300000;
    public static final int SCALEGUARDIAN_OFFSET = 400000;
    
    // SpellWeaver ascendancy offsets
    public static final int ELEMENTALIST_OFFSET = 700000;
    public static final int CHRONOMANCER_OFFSET = 800000;
    public static final int ARCANEPROTECTOR_OFFSET = 900000;
    
    // Reserved for future classes (starting at 1000000)
    public static final int FUTURE_CLASS_START = 1000000;
    
    /**
     * Get the original skill ID by removing the class-specific offset
     * @param skillId The skill ID with offset
     * @return The original skill ID without offset
     */
    public static int getOriginalId(int skillId) {
        if (skillId < BASE_SKILLS_MAX) {
            return skillId; // Already a base skill ID
        }
        
        // Check ranges for each class
        if (skillId >= BEASTMASTER_OFFSET && skillId < SHADOWSTALKER_OFFSET) {
            return skillId - BEASTMASTER_OFFSET;
        } else if (skillId >= SHADOWSTALKER_OFFSET && skillId < EARTHWARDEN_OFFSET) {
            return skillId - SHADOWSTALKER_OFFSET;
        } else if (skillId >= EARTHWARDEN_OFFSET && skillId < ELEMENTALIST_OFFSET) {
            return skillId - EARTHWARDEN_OFFSET;
        } else if (skillId >= BERSERKER_OFFSET && skillId < FLAMEWARDEN_OFFSET) {
            return skillId - BERSERKER_OFFSET;
        } else if (skillId >= FLAMEWARDEN_OFFSET && skillId < SCALEGUARDIAN_OFFSET) {
            return skillId - FLAMEWARDEN_OFFSET;
        } else if (skillId >= SCALEGUARDIAN_OFFSET && skillId < SHADOWSTALKER_OFFSET) {
            return skillId - SCALEGUARDIAN_OFFSET;
        } else if (skillId >= ELEMENTALIST_OFFSET && skillId < CHRONOMANCER_OFFSET) {
            return skillId - ELEMENTALIST_OFFSET;
        } else if (skillId >= CHRONOMANCER_OFFSET && skillId < ARCANEPROTECTOR_OFFSET) {
            return skillId - CHRONOMANCER_OFFSET;
        } else if (skillId >= ARCANEPROTECTOR_OFFSET && skillId < FUTURE_CLASS_START) {
            return skillId - ARCANEPROTECTOR_OFFSET;
        }
        
        // For future classes
        return skillId % FUTURE_CLASS_START;
    }
}
