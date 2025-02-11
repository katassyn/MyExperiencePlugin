# MyExperiencePlugin

Advanced leveling and class system plugin for Minecraft servers with party system, custom experience tables, and class-based progression.

## Features

### Experience System
- Custom leveling system with configurable experience requirements
- Experience gain from MythicMobs
- Party system with shared experience
- Experience boost events
- Money rewards for leveling up
- Level display in chat and tab list
- Top players leaderboard

### Class System
- Three base classes:
  - **Ranger**: Guardian of the wilds, excels at taming beasts and stealth
  - **Dragonknight**: Mighty frontline fighter with strong defensive capabilities
  - **Spellweaver**: Master of arcane arts, focuses on ranged spell damage

### Ascendancy System
Each base class has three specialized ascendancy options:

#### Ranger Ascendancies
- **Beastmaster**: Empowers pets and wild nature-based abilities
- **Shadowstalker**: Masters stealth, surprise, and high mobility
- **Earthwarden**: Harnesses nature's power for support & defense

#### Dragonknight Ascendancies
- **Flame Warden**: Focuses on fire power, burning, and melee strength
- **Scale Guardian**: A living shield with high defense & taunts
- **Berserker**: Draconic rage, growing stronger in battle

#### Spellweaver Ascendancies
- **Elementalist**: Masters elemental power, unleashing deadly spells
- **Chronomancer**: Manipulates time for unique offense & support
- **Arcane Protector**: Shields allies with arcane magic & protective spells

## Commands

### Experience Commands
- `/exp` - Display your current level and experience
- `/exp_give <amount> <player>` - Give experience to a player
- `/exp_give_p <percentage> <player>` - Give percentage of required XP to level up
- `/get_lvl <level>` - Set player level (Admin only)
- `/top` - Display top 10 players by level
- `/bonus_exp <enable|disable|set> [value]` - Manage bonus XP events
- `/exp_table reload` - Reload experience configuration

### Party Commands
- `/party inv <player>` - Invite player to party
- `/party accept` - Accept party invitation
- `/party decline` - Decline party invitation
- `/party leave` - Leave current party
- `/party info` - Display party information

### Class Commands
- `/chose_class` - Open base class selection GUI
- `/chose_ascendancy` - Open ascendancy selection GUI (requires level 20)
- `/skilltree` - View available skill points
- `/class set <className>` - Force-change class (Admin only)

## Configuration

### config.yml
```yaml
database:
  host: "localhost"
  port: "3307"
  name: "minecraft_experience"
  user: "root"
  password: ""

Bonus_exp:
  Enabled: false  # Whether bonus XP event is active
  Value: 100      # Bonus XP percentage (100 = double XP)
```

### exp_table.yml
Configure experience rewards for different mob types:
```yaml
xp_per_mob:
  serpent_fighter: 10
  undead_villager: 10
  # Add more mobs here
```

### exp_money.yml
Configure money rewards for reaching certain levels:
```yaml
rewards:
  1: 50    # 50 money at level 1
  2: 100   # 100 money at level 2
  # Add more levels
```

## Dependencies
- Vault - For economy integration
- MythicMobs - For custom mob experience

## Permissions
- `myplugin.exp_table.reload` - Permission to reload exp tables
- `myplugin.exp_money.reload` - Permission to reload money rewards
- `myplugin.bonusexp` - Permission to manage bonus XP events
- `myplugin.reloadbonus` - Permission to reload bonus XP config
- `myplugin.skilltree` - Permission to use skill tree
- `myplugin.class` - Permission to use class commands
- `myplugin.choseclass.others` - Permission to open class GUI for others

## Installation
1. Place the plugin JAR in your server's `plugins` folder
2. Configure database settings in `config.yml`
3. Configure experience tables in `exp_table.yml`
4. Configure level rewards in `exp_money.yml`
5. Restart your server
6. Give appropriate permissions to players/groups

## Database Schema
The plugin uses MySQL/MariaDB with the following tables:

### players
```sql
CREATE TABLE players (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(16),
    level INT,
    xp DOUBLE
);
```

### player_classes
```sql
CREATE TABLE player_classes (
    uuid VARCHAR(36) PRIMARY KEY,
    class VARCHAR(32),
    ascendancy VARCHAR(32),
    skill_points INT
);
```

## Features Coming Soon
- Skill tree implementation
- Class-specific abilities
- More ascendancy options
- Custom class quests
- Advanced party features

## Support
If you encounter any issues or need help, please create an issue on the GitHub repository.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
