# Boosters
An extremely powerful and user-friendly boosts plugin

## Documentation
Additional documentation is available in the `/docs` and `/example_config` directories

## Features
- Boosts can target player(s), LuckPerms group(s), or everyone
- Create boosts with tab-complete commands
- Visual support through boss bars (optional per boost) and player menus
- Support for editing a boost's time remaining with an anvil menu
- Supports reloading with Plugman

## Boost Types
- `POTION_EFFECT` Apply a potion effect. Similar behavior to a beacon effect
- `VANILLA_FISHING` Speed up vanilla fishing by modifying hook time and/or animation time


- `COMMAND_DROPS` Boost the chance for rewards to roll
- `LEVELS` Uses permissions to boost custom experience rate
- `PRISON_ENCHANTS` Boost custom enchantment activation chance
- `SHOPGUIPLUS_SELL_BOOST` Uses permissions to boost sell multiplier

## Dependencies
- [ServerUtils](https://github.com/MittenMC/ServerUtils) 1.1.10+

## Soft Dependencies for Boosts
- `POTION_EFFECT`, `VANILLA_FISHING` None
- `COMMAND_DROPS` [CommandDrops](https://github.com/GavvyDizzle/CommandDrops)
- `LEVELS` Levels (private), [LuckPerms](https://luckperms.net/)
- `PRISON_ENCHANTS` PrisonEnchants (private)
- `SHOPGUIPLUS_SELL_BOOST` [ShopGUIPlus](https://www.spigotmc.org/resources/shopgui-1-8-1-21.6515/), [LuckPerms](https://luckperms.net/)

## Save Data
- The plugin uses a `.yml` file to save active boosts across plugin restarts