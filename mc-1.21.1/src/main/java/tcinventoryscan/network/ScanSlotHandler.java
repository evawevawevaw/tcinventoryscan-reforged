package tcinventoryscan.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tcinventoryscan.TcInventoryScanning;
import thaumcraft.item.ThaumometerItem;
import thaumcraft.research.ScanManager;

@EventBusSubscriber(modid = TcInventoryScanning.MODID)
public final class ScanSlotHandler {
    private ScanSlotHandler() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ScanSlotPayload.TYPE, ScanSlotPayload.STREAM_CODEC, (payload, context) -> handleSlot(payload, context.player()));
        registrar.playToServer(ScanSelfPayload.TYPE, ScanSelfPayload.STREAM_CODEC, (payload, context) -> handleSelf(context.player()));
    }

    private static boolean holdingThaumometer(ServerPlayer player) {
        return player.containerMenu != null && player.containerMenu.getCarried().getItem() instanceof ThaumometerItem;
    }

    private static void complete(ServerPlayer player, ScanManager.Target target) {
        if (target != null && !ScanManager.hasBeenScanned(player, target)) {
            ScanManager.completeScan(player, target);
        }
    }

    private static void handleSlot(ScanSlotPayload payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !holdingThaumometer(serverPlayer)) {
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        int index = payload.slotIndex();
        if (menu.containerId != payload.containerId() || index < 0 || index >= menu.slots.size()) {
            return;
        }
        Slot slot = menu.slots.get(index);
        if (!slot.hasItem() || slot instanceof ResultSlot || !slot.mayPickup(serverPlayer)) {
            return;
        }
        complete(serverPlayer, ScanManager.resolveStack(slot.getItem()));
    }

    private static void handleSelf(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !holdingThaumometer(serverPlayer)) {
            return;
        }
        if (serverPlayer.containerMenu != serverPlayer.inventoryMenu) {
            return;
        }
        complete(serverPlayer, ScanManager.resolveEntity(serverPlayer));
    }
}
