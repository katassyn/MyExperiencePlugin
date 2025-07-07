# MyExperiencePlugin

A comprehensive Minecraft plugin that adds RPG elements including experience system, classes, parties, and alchemy.

## Features

### Experience System
- Custom leveling system with configurable XP requirements
- Maximum level cap of 100
- Experience sharing in parties
- Bonus XP events system
- Money rewards for leveling up (requires Vault)
- Level display in chat and tab list
- `/exp` command to check current XP progress
- Top players leaderboard

### Class System
#### Base Classes
- **Ranger**: A guardian of the wilds, excels at taming beasts or stealth
- **Dragonknight**: A mighty frontline fighter with strong defensive capabilities
- **Spellweaver**: A master of arcane arts, focuses on ranged spell damage

#### Ascendancy Classes
Each base class can evolve into one of three specialized ascendancy classes at level 20:

**Ranger Ascendancies:**
- Beastmaster: Empowers pets and wild nature-based abilities
- Shadowstalker: Masters stealth, surprise, and high mobility
- Earthwarden: Harnesses nature's power for support & defense

**Dragonknight Ascendancies:**
- Flame Warden: Focuses on fire power, burning, and melee strength
- Scale Guardian: A living shield with high defense & taunts
- Berserker: Draconic rage, growing stronger in battle

**Spellweaver Ascendancies:**
- Elementalist: Masters elemental power, unleashing deadly spells
- Chronomancer: Manipulates time for unique offense & support
- Arcane Protector: Shields allies with arcane magic & protective spells

### Party System
- Create parties of up to 3 players
- Experience sharing within 25 blocks
- Party members receive 30% of the kill XP
- Party invites with clickable accept buttons
- Party info command to see members and their levels

### Database Integration
- MySQL/MariaDB support using HikariCP
- Efficient async data handling
- Stores player levels, XP, classes, and skill points

## Commands

### Experience Commands
- `/exp` - Check your current XP and level
- `/exp_give <amount> <player>` - Give XP to a player
- `/exp_give_p <percentage> <player>` - Give percentage of level XP to a player
- `/get_lvl <level>` - Set your level
- `/top` - Show top 10 players by level
- `/bonus_exp <enable|disable|set> [value]` - Manage bonus XP events

### Class Commands
- `/chose_class` - Open base class selection GUI
- `/chose_ascendancy` - Open ascendancy selection GUI (requires level 20)
- `/skilltree` - View available skill points (GUI coming soon)

### Party Commands
- `/party inv <player>` - Invite a player to your party
- `/party accept` - Accept a party invitation
- `/party decline` - Decline a party invitation
- `/party leave` - Leave your current party
- `/party info` - Show information about your party

## Configuration

### config.yml
- Database settings
- Bonus XP event settings

### exp_table.yml
- Configure XP rewards for different mobs
- Customize XP requirements per level

### exp_money.yml
- Configure money rewards for reaching specific levels

## Permissions
- `myplugin.bonusexp` - Access to bonus XP commands
- `myplugin.choseclass.others` - Ability to open class GUI for other players
- `myplugin.reloadbonus` - Permission to reload bonus XP configuration

## Dependencies
- Vault - For economy integration
- MythicMobs - For custom mob XP rewards
- LuckPerms - For permissions management

## Requirements
- Java 8 or higher
- Spigot/Paper server 1.20.1 or higher

## Database Schema

### players table
```sql
CREATE TABLE players (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(16),
    level INT,
    xp DOUBLE
);
```

### player_classes table
```sql
CREATE TABLE player_classes (
    uuid VARCHAR(36) PRIMARY KEY,
    class VARCHAR(32),
    ascendancy VARCHAR(32),
    skill_points INT
);
```

## Installation
1. Place the plugin JAR in your server's `plugins` folder
2. Configure database settings in `config.yml`
3. Start/restart your server
4. Configure XP rewards in `exp_table.yml`
5. Configure level-up rewards in `exp_money.yml`

## Features Coming Soon
- Skill tree system with unlockable abilities
- More ascendancy classes
- Class-specific quests and challenges
- Enhanced party features
- Alchemy system implementation

## Debugging and Error Handling
The plugin includes a comprehensive logging system to help diagnose issues:

- Error messages are logged to the server console
- Debug mode can be enabled in the config.yml file:
  ```yaml
  debug:
    enabled: true
    level: 1  # 0=off, 1=basic, 2=verbose
  ```
- Common error messages and their solutions:
  - Database connection issues: Check your database credentials in config.yml
  - Missing dependencies: Ensure all required plugins are installed
  - Permission errors: Verify permission nodes are correctly set in your permissions plugin

## Building from Source
To build the plugin from source:

1. Clone the repository
2. Make sure you have Maven installed
3. Run `mvn clean package` in the project directory
4. The compiled JAR will be in the `target` folder

## Contributing
Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows the existing style and includes appropriate documentation.

## Support
If you encounter any issues or have questions:

1. Check the [common issues](#debugging-and-error-handling) section
2. Submit an issue on the project's issue tracker
3. Contact the plugin author for direct support
