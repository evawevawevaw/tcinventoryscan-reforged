package tcinventoryscan;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.common.Mod;

@Mod(TcInventoryScanning.MODID)
public class TcInventoryScanning {
    public static final String MODID = "tcinventoryscan";

    public TcInventoryScanning() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
