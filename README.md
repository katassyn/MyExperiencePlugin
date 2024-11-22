
# MyExperiencePlugin

A robust Minecraft plugin designed to enhance gameplay with a custom experience and level system. The plugin integrates with external systems such as Vault, MythicMobs, and BeautyQuests to create a rewarding and interactive environment for players. With features like party XP sharing, configurable XP tables, monetary rewards, and a database-backed leveling system, this plugin is ideal for any server.

---

## Features

### **1. Dynamic XP and Level System**
- Players earn XP through:
  - Killing mobs (with **MythicMobs** integration).
  - Completing quests (with **BeautyQuests** integration).
- Supports a maximum level of **100**.
- Level progression and XP requirements are fully configurable via `exp_table.yml`.

### **2. Custom Chat and Tab Formatting**

- **Chat:**
  - Player chat messages display their current level:
    - Max-level players (level 100) show a special tag: `[MAX LEVEL]`.
    - Other players display their level in a `[ X ]` format.

- **Tab:**
  - Player levels are displayed next to their names in the server's player list (Tab):
    - Format: `[ X ] PlayerName`
    - Levels update dynamically as players level up.


### **3. Database Integration**
- Player data (levels and XP) is stored in a MySQL database.
- Automatically loads and saves data for each player.
- Ensures persistence across server restarts.

### **4. Party System**
- Players can form parties with up to **3 members**.
- XP sharing:
  - 100% XP for the player who kills a mob.
  - 30% XP shared with party members within a **25-block radius**.

### **5. Monetary Rewards**
- Players receive in-game currency rewards upon leveling up (requires Vault).
- Rewards are defined in `exp_money.yml`:

\`\`\`yaml
rewards:
  1: 50
  2: 100
  3: 150
  4: 200
  5: 250
  # Add more levels as needed
\`\`\`

### **6. Configurable XP Values for Mobs**
Define XP values for specific mob types in `exp_table.yml`:

\`\`\`yaml
xp_per_mob:
  serpent_fighter: 10
  undead_villager: 10
  # Add more mobs as needed
\`\`\`

Mobs are identified by their **MythicMobs** ID.

---

## Commands

### **Player Commands**
- `/exp`: Displays your XP, level, and progress toward the next level.
- `/top`: Shows the top 10 players by level.
- `/party`: Manage party-related actions:
  - `/party inv <player>`: Invite a player to your party.
  - `/party accept`: Accept a party invite.
  - `/party decline`: Decline a party invite.
  - `/party leave`: Leave your current party.
  - `/party info`: View party details.

### **Admin Commands**

- `/exp_table reload`  
  Reloads the XP table configuration (`exp_table.yml`).

- `/exp_money reload`  
  Reloads monetary rewards settings (`exp_money.yml`).

- `/get_lvl <level>`  
  Sets the level and XP of the command executor to the specified `<level>`.  
  **Example:** `/get_lvl 10` sets your level to 10 and resets XP for that level.

- `/exp_give <amount> <player>`  
  Grants the specified `<amount>` of XP to the targeted `<player>`.  
  **Example:** `/exp_give 500 Steve` gives 500 XP to the player named Steve.

- `/exp_give_p <percentage> <player>`  
  Grants the specified `<percentage>` of the XP required for the current level to the targeted `<player>`.  
  **Example:** `/exp_give_p 10 Steve` gives Steve 10% of the XP required to reach their next level.

---

## Configuration Files

### **1. config.yml**
Define database and plugin settings:

\`\`\`yaml
database:
  host: localhost
  port: 3306
  name: minecraft
  user: root
  password: password
\`\`\`

### **2. exp_table.yml**
Define XP requirements and mob XP values:

\`\`\`yaml
xp_per_mob:
  serpent_fighter: 10
  undead_villager: 10
  # Add more mobs as needed
\`\`\`

### **3. exp_money.yml**
Define monetary rewards for leveling up:

\`\`\`yaml
rewards:
  1: 50
  2: 100
  3: 150
  4: 200
  5: 250
  # Add more levels as needed
\`\`\`

---

## Integration with Other Plugins

### **Required Plugins**
- **Vault**: Handles monetary rewards.
- **MythicMobs**: Provides XP rewards for killing custom mobs.
- **BeautyQuests**: Assigns quests for XP rewards.

---

## Installation

1. Place the plugin's JAR file in your server's plugins folder.
2. Start or reload the server.
3. Configure the database in `config.yml`.
4. Add custom XP values in `exp_table.yml`.
5. Define monetary rewards in `exp_money.yml`.
6. Restart the server to apply changes.

---

## Future Features

- **Dungeon Integration**: XP rewards for dungeon completion.
- **Custom Events**: Trigger XP rewards based on special events.
- **Enhanced Party System**: Support for more members and party-specific bonuses.
- **Leaderboard System**: Track top players across various metrics.

---

## Support

If you encounter issues or have feature requests, feel free to open an issue in the repository or contact the developer.
