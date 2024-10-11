# Commands

## Admin
All `boostadmin` commands require the base permission `boosters.admin` along with the per-command permission
- `/boostadmin help` Displays the help menu
- `/boostadmin list [player]` Opens the boost list menu
- `/boostadmin reload` Reload plugin data
- `/boostadmin start <type> <duration> <target> ...` Start a new boost. Follow tab-completion prompting
  - For an explanation of the duration and target arguments, see below
  - Once all required arguments have been set, any remaining will be used to build the boss bar contents
    - Spaces are allowed
    - Coloring for boss bar contents only supports the & color codes (0-9a-f)
    - The bossbar title becomes the name of the boost in all menus

## Player
All `boost` commands require the base permission `boosters.player` along with the per-command permission
- `/boost help` Displays the help menu
- `/boost list [player]` View your active boosts or of another player
  - The optional player argument requires an additional permission: `boosters.player.list.other`

## Start Command Arguments

### Targets
Syntax for defining the target of a boost. Multiple targets are comma separated. DO NOT USE SPACES
- `*` The boost will target all players
- A player (`GavvyDizzle`) or list of players (`GavvyDizzle,Notch,player3`) will target just those players
- A group (`g:vip`) or list of groups (`g:vip,g:king,g:elite`) will target players who belong to **any** of the groups
- You are unable to merge targets. For example, `GavvyDizzle,g:vip` is not allowed

### Duration
Duration is parsed as Unix time or a duration from the current time

#### Epoch time
- The **seconds** since Jan 1, 1970
- Don't attempt to do the math, use a [tool](https://www.epochconverter.com/) to calculate the timestamp for your desired end time

#### Duration
- If a time character (s/m/h/d/w/y) is included in the argument, it will be parsed as a duration
- The end time of the boost will represent `now + duration`, a point in the future
- See the ServerUtils API implementation [here](https://github.com/MittenMC/ServerUtils/blob/master/src/main/java/com/github/mittenmc/serverutils/Numbers.java#L267)