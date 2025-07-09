# iClan Plugin

A simple and efficient clan management plugin for PaperMC 1.21.7 servers.

## 📋 Features

- **Create Clans** - Players can create their own clans with unique names
- **Join System** - Request-based clan joining with owner approval
- **Member Management** - Kick members, transfer ownership, and leave clans
- **Clan Information** - View detailed clan stats and member lists
- **Data Persistence** - All clan data is automatically saved and loaded
- **Permission System** - Full permission support for fine-tuned control
- **Tab Completion** - Smart command autocomplete for better UX

## 📦 Installation

1. **Download** the latest `iClan-1.0.0.jar` from the [releases page](https://github.com/PavTheProgrammer89/Clan-Plugin/releases)
2. **Place** the JAR file in your server's `plugins/` folder
3. **Restart** your PaperMC server
4. **Done!** The plugin will generate its configuration files automatically

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/clan create <name>` | Create a new clan | `iclan.create` |
| `/clan join <name>` | Request to join a clan | `iclan.join` |
| `/clan accept <username>` | Accept a player into your clan | `iclan.accept` |
| `/clan info [name]` | Show clan information | `iclan.info` |
| `/clan leave` | Leave your current clan | `iclan.leave` |
| `/clan kick <username>` | Kick a member from your clan | `iclan.kick` |
| `/clan disband` | Disband your clan | `iclan.disband` |
| `/clan list` | List all clans on the server | `iclan.list` |

### Command Aliases
- `/clan` can also be used as `/c` or `/clans`

## 🔐 Permissions

### Basic Permissions (Default: true)
- `iclan.create` - Create clans
- `iclan.join` - Join clans
- `iclan.leave` - Leave clans
- `iclan.info` - View clan information
- `iclan.list` - List all clans

### Owner Permissions (Default: false)
- `iclan.accept` - Accept clan members
- `iclan.kick` - Kick clan members
- `iclan.disband` - Disband clans

### Admin Permissions (Default: op)
- `iclan.admin` - All clan permissions
- `iclan.reload` - Reload plugin configuration
- `iclan.backup` - Backup clan data

### Permission Groups
- `iclan.user` - All basic user permissions
- `iclan.owner` - All clan owner permissions

## 🎯 How It Works

1. **Creating a Clan**: Use `/clan create MyClaN` to create your clan
2. **Joining**: Players use `/clan join MyClan` to request membership
3. **Accepting**: Clan owners use `/clan accept PlayerName` to accept requests
4. **Managing**: Owners can kick members, view info, or disband the clan

## 📊 Clan Rules

- **Clan Names**: 3-16 characters, letters and numbers only
- **One Clan Per Player**: Players can only be in one clan at a time
- **Owner Powers**: Only clan owners can accept/kick members and disband
- **Join Requests**: Expire after 5 minutes automatically

## 🛠️ Configuration

The plugin automatically creates these files:
- `plugins/iClan/clans.yml` - Stores all clan data
- `plugins/iClan/backups/` - Automatic data backups

**No manual configuration required!** The plugin works out of the box.

## 📋 Requirements

- **Minecraft**: 1.21.7
- **Server Software**: PaperMC (recommended) or Spigot
- **Java**: 21 or higher

## 🐛 Support

If you encounter any issues:

1. Check the server console for error messages
2. Ensure you're using PaperMC 1.21.7
3. Verify Java 21 is installed
4. Create an issue on our [GitHub repository](https://github.com/PavTheProgrammer89/Clan-Plugin/issues)

## 🔄 Updates

- Check the [releases page](https://github.com/PavTheProgrammer89/Clan-Plugin/releases) for updates
- Replace the old JAR file with the new one
- Restart your server

## 👥 Credits

**Developed by**: [NaufalVerse](https://github.com/PavTheProgrammer89)  
**For**: PaperMC Minecraft Servers

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**⭐ Star this repository if you find it helpful!**