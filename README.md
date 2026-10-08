![A player in a minecart going very fast](https://cdn.modrinth.com/data/cached\_images/aa5139a381dfdf9e1ec2797f88542852935573f1.webp)

<center>
  Minecarts are arguably the best transport in Minecraft, but they can be better
</center>

<br>

# Flash Carts Enhanced

## About
[![Modrinth](https://img.shields.io/badge/Published%20on-Modrinth-1bd96a?logo=modrinth&logoColor=bluegreen)](https://modrinth.com/mod/flash-carts-enhanced)
[![CurseForge](https://img.shields.io/badge/Published%20on-CurseForge-f16436?logo=curseforge&logoColor=orange)](https://www.curseforge.com/minecraft/mc-mods/flash-carts-enhanced/)
[![GitHub](https://img.shields.io/badge/%E2%80%8B-GitHub-gray?logo=github&logoColor=black&labelColor=white)](https://github.com/DarkgreenWorld/FlashCarts)

#### Requires <img alt="Fabric API icon" src="https://cdn.modrinth.com/data/P7dR8mSH/icon.png" width="20" height="20"> [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

#### The Link of Original Flash Carts

[![Modrinth](https://img.shields.io/badge/Flash%20Carts-Modrinth-1bd96a?logo=modrinth\&logoColor=bluegreen)](https://modrinth.com/mod/flash-carts)
[![GitHub](https://img.shields.io/badge/%E2%80%8B-GitHub-gray?logo=github\&logoColor=black\&labelColor=white)](https://github.com/gjorgdy/FlashCarts)

The primary goal of this mod is to make minecarts better without breaking your Redstone.

### New Minecart Physics

To make Minecarts faster, Flash Carts uses Minecrafts experimental physics. <br>
These experimental physics allow for a lot higher speeds, but do often break redstone contraptions.
Because of this, the mod only applies the new physics where you would benefit from the higher speeds.

By default, the new physics are applied to normal and TNT minecarts, but not to chest, furnace, or hopper minecarts.

Alternatively, you can enable the Express Minecart crafting recipe in the config file.
They are crafted using one redstone torch and any regular minecart, and always have experimental physics enabled.
You can then disable experimental physics for regular minecarts.

#### Smart Halting

Slowing down minecarts can become inconsistent pretty quickly with the speeds made possible by this mod.
To account for this, it also adds 'smart halting' which will intelligently stop a cart at the end of a line of
unpowered rails or at the top of a unpowered slope.

!\[A normal minecraft using a different physics system than a chest minecart](https://cdn.modrinth.com/data/cached\_images/a6e3345ce1fde6c5247d474b99ef5c1601e25499.webp)

### Building tools

To make building of minecart tracks easier, Flash Carts adds selection placing inspired by the Create mod.

To start, right-click a rail block with a rail in your hand, and a preview of the rail will appear in front of you. You can then place the rail by right-clicking again.

> For people who want a more vanilla experience, Flash Carts also adds an 'extend' build mode which can be enabled in the config.

!\[A rail being built across a bridge using selections](https://cdn.modrinth.com/data/cached\_images/a895d7390749274bf1d5e64760d2802531848b98.webp)

### Speed HUD

To make sure you use the new maximum speeds to their full potential, a speedometer and speedbar show up when you are in a minecart.

!\[Speedometer showing the current speed of a minecart in the action bar](https://cdn.modrinth.com/data/cached\_images/cbda492ba396d3d4478fd3770f91b36dfe4857df\_0.webp)

### Station Titles

To make Minecarts more convenient to create transport networks, a station can show its name when you are standing still on top of it.

To set a station's name, place a sign under the block where a Minecart stands still, and write the station name on the first line of the sign.
Any extra lines on the sign will be combined into the subtitle under the station name.

!\[The title of a station and the sign making it possible](https://cdn.modrinth.com/data/cached\_images/5d2e65d81feb46b33a9353666fa8cbd58d773066\_0.webp)

### Rail recipes

To make building railways a bit more do-able early game, Flash Carts adds some alternative recipes for rails like being able to use Copper instead of Gold for Powered Rails.

!\[A custom recipe for Powered Rail with Copper instead of Gold.](https://cdn.modrinth.com/data/cached\_images/b1afca4f1db77be4f53b8fc22f26bfe78730bc3b.webp)

<br>

## Configuration

Use `/flashcarts config` to open the configuration screen in-game.

In addition, you can also open the configuration screen via the Mod Menu,
but only if you are in a singleplayer world with cheats enabled or on a server where you have operator permissions.

If you have directly edited the configuration file,
to load changes to the config file, you can use the vanilla `/reload` command.

```toml
cheaperRecipes = true
# Whether to show the speedometer when in minecart (current speed in blocks per second), vanilla: false
showSpeedometer = true
# Whether to show the speed bar when in minecart (bars up to max speed), vanilla: false
showSpeedBar = true
# Whether to show the station title (title when standing still at a block with a sign under it), vanilla: false
showStationTitle = true
# Percentage of boost a powered rail should give, vanilla: 0.06 (6%)
poweredRailBoostPercentage = 0.06
# How slow a minecart has to be, to be considered halted, vanilla: 0.03
haltSpeedThreshold = 0.03
# Multiplier applied to speed when minecart is considered halted, vanilla: 0.5
haltSpeedMultiplier = 0.5
...
```

