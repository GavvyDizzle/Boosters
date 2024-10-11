# Bossbar

## Details
- A bossbar is an option component to a boost
- If added to the end of the `/boostadmin start` command...
  - A bossbar will appear for the duration of the boost with the given contents
  - The name of the boost item in all menus will match the boss bar contents

## Notes
- There is no way for players to toggle boss bars per-boost. They are all or nothing
- The Minecraft client only supports legacy color codes for bossbar contents. Attempting to use hex coloring will result in undefined coloring

## Configuration
Each boost type gets boss bar configuration. The data will appear for enabled boost types only
```yaml
type:
  POTION_EFFECT:
    # Ignore the value. It is ignored
    content: ''
    # The color of the bossbar bar. Supported colors are PINK,BLUE,RED,GREEN,YELLOW,PURPLE,WHITE
    color: GREEN
    # The overlay of the bossbar bar. Supported types are PROGRESS,NOTCHED_6,NOTCHED_10,NOTCHED_12,NOTCHED_20
    overlay: PROGRESS
    # Ignore this value too
    initialProgress: 1.0
  VANILLA_FISHING:
    content: ''
    color: GREEN
    overlay: PROGRESS
    initialProgress: 1.0
```