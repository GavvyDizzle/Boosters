# Menus
The only menu lists active boosts for one or all players

## Admin Editing
When opening the `list` menu with the admin command, the viewer will have options to edit the boosts
- Right-clicking a boost will cancel the boost **for all targets**
- Left-clicking a boost  will open up an anvil menu where you can modify the boost's duration
![anvil_menu.png](/docs/images/anvil_menu.png)

## Configuration
Each boost type gets its own configuration. The data will appear for enabled boost types only
```yaml
# Formatting for any date variables
time:
  date_format: d MMM yyyy h:mm:ssa z
  # Unfortunately, there is no easy way to show players time in their time zone
  time_zone: America/New_York
  
# Lore to add to boost items in menus.
# Placeholders:
# {time} The seconds remaining
# {time_formatted} The seconds remaining, formatted nicely
# {end_date} The end date, formatted by time.date_format
# (admin only) {targets} Lists the boost's targets
lore:
  player:
    - '&7Time Remaining: &b{time_formatted}'
    - '&7End Date: &b{end_date}'
  admin:
    - '&7Time Remaining: &b{time_formatted}'
    - '&7End Date: &b{end_date}'
    - '&7Targets: &e{targets}'

# The prefix_lore sections are placed before the above lore
# The existing placeholders are for *most* of the values
# If you want all of them, please check the code (defined in each concrete class)
type:
  COMMAND_DROPS:
    material: BUNDLE
    # The name given to the menu item if no bossbar name is specified
    default_name: '&eRewards Boost'
    prefix_lore:
      - '&7Multiplier: &a{multiplier}x'
      - ''
  LEVELS:
    material: EXPERIENCE_BOTTLE
    default_name: '&eLevels Boost'
    prefix_lore: []
  POTION_EFFECT:
    material: BEACON
    default_name: '&ePotion Effect Boost'
    prefix_lore:
      - '&7Effect: &a{effect} {amplifier}'
      - ''
  PRISON_ENCHANTS:
    material: ENCHANTING_TABLE
    default_name: '&eEnchants Boost'
    prefix_lore:
      - '&7Multiplier: &a{multiplier}x'
      - ''
  SHOPGUIPLUS_SELL_BOOST:
    material: SUNFLOWER
    default_name: '&eSell Boost'
    prefix_lore: []
  VANILLA_FISHING:
    material: FISHING_ROD
    default_name: '&eFishing Boost'
    prefix_lore:
      - '&7Multiplier: &a{multiplier}x'
      - ''
```