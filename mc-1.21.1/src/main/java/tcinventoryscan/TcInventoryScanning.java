package tcinventoryscan;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;

@Mod(TcInventoryScanning.MODID)
public class TcInventoryScanning {
    public static final String MODID = "tcinventoryscan";

    public TcInventoryScanning() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
