# Boosts
Documentation for the various boost types

## Usage
- To start a boost, use the `/boostadmin start` command. See the commands documentation for more details
- You can view and edit all active boosts with `/boostadmin list`
  - Only the admin menu (`/boostadmin list` not `/boost list`) supports modifications
  - Left click to open a menu where you can modify the duration
  - Right click to cancel the boost **for all targets**

## API
Requirements for creating a new boost type

### Categorization
Boosts currently fit into a few groups:
- `TimedPermissionBoost` Uses LuckPerms to manage a temporary permission
    - Applies to the default group, individual players or groups
- `EventBoost` Hooks into another plugin via a custom event
    - All invocations of the desired event type will be passed to the boost
- `Miscellaneous` Some boost types don't fit into the above categories. They require a custom solution

### New Pieces
- Create a new `BoostType` entry
  - Add all soft dependencies required for the boost to function. If any are missing, the boost type will become disabled
- Create a new class
  - Follow the categorization guidelines for inheritance

### Commands
Command logic is split up along the prior categorizations
- If your boost fits into a pre-existing group, add it to the list of boost types for that command
- Otherwise, create a new command with the new data fields. Copy the existing logic

## Save Data
All active boosts are written to `boosts.yml` on server shutdown
- Data is only touched on startup and shutdown. The `reload` command will not touch this file
- The ordering of all boosts is preserved across restarts (ordering goes by an internal ID system)
- You should never need to edit this file. Syntax errors will corrupt all data
- If a disabled boost type tries to load, it will immediately be deleted and not written back to the file in future attempts