# Thaumic Inventory Scanning (Thaumcraft 4: Reforged, NeoForge 26.3)

Unofficial port of [ThaumicInventoryScanning](https://github.com/TwelveIterationMods/ThaumicInventoryScanning)
to Minecraft 26.3 / NeoForge 26.3, as an addon for
[Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged).

Put the Thaumometer on the cursor in a container GUI and hold it over an item for one second to scan it.
Hover the player model in the inventory to scan yourself. Scanned items and players show their aspects
next to the cursor.

Same rules as scanning in the world: unknown parent aspects block the scan, scanned things are skipped.
The mod is needed on both client and server.

## Versions

| Minecraft | Location | Java |
|-----------|----------|------|
| 26.3      | repository root | 25 |
| 1.21.1    | [`mc-1.21.1/`](mc-1.21.1) | 21 |

## Build

Java 25 required.

1. Build Thaumcraft 4: Reforged and copy `build/libs/thaumcraft-4-reforged-1.0-26.3.jar` into `libs/`.
2. `./gradlew build` produces `build/libs/tcinventoryscan-1.0.1-26.3.jar`.
3. Put it into `mods/` together with the Reforged jar and TerraBlender.

## License

MIT, see [LICENSE](LICENSE). The original mod is MIT as well (checked on GitHub, both
TwelveIterationMods and ModdingForBlockheads repositories): Copyright (c) 2015 Christopher "BlayTheNinth" Baker.
The original copyright notice is kept as the license requires.

## Credits

- evawevawevaw: unofficial port to Thaumcraft 4: Reforged / Minecraft 26.3
- BlayTheNinth: original Thaumic Inventory Scanning
- Azanor: Thaumcraft
- notlikethiss: Thaumcraft 4: Reforged
