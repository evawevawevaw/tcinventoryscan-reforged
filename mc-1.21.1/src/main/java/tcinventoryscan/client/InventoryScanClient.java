package tcinventoryscan.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import tcinventoryscan.TcInventoryScanning;
import tcinventoryscan.network.ScanSelfPayload;
import tcinventoryscan.network.ScanSlotPayload;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.client.aspect.AspectVisibility;
import thaumcraft.client.platform.ClientNetwork;
import thaumcraft.item.ThaumometerItem;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ScanManager;

@EventBusSubscriber(modid = TcInventoryScanning.MODID, value = Dist.CLIENT)
public final class InventoryScanClient {
    private static final int SCAN_TICKS = 20;
    private static final int PLAYER_X1 = 26;
    private static final int PLAYER_Y1 = 8;
    private static final int PLAYER_X2 = 75;
    private static final int PLAYER_Y2 = 78;
    private static final String SELF_KEY = "self";

    private static int mouseX;
    private static int mouseY;
    private static String scanKey = "";
    private static int scanTicks;
    private static boolean attempted;

    private InventoryScanClient() {
    }

    private record Subject(String key, ScanManager.Target target, CustomPacketPayload payload) {
    }

    private static void reset() {
        scanKey = "";
        scanTicks = 0;
        attempted = false;
    }

    private static boolean holdingThaumometer(AbstractContainerScreen<?> screen) {
        return screen.getMenu().getCarried().getItem() instanceof ThaumometerItem;
    }

    private static @Nullable AbstractContainerScreen<?> activeScreen(Minecraft minecraft) {
        if (minecraft.screen instanceof AbstractContainerScreen<?> screen
            && !(screen instanceof CreativeModeInventoryScreen)
            && holdingThaumometer(screen)) {
            return screen;
        }
        return null;
    }

    private static boolean hoveringSelf(AbstractContainerScreen<?> screen) {
        if (!(screen instanceof InventoryScreen) || screen.getSlotUnderMouse() != null) {
            return false;
        }
        int x = mouseX - screen.leftPos;
        int y = mouseY - screen.topPos;
        return x >= PLAYER_X1 && x < PLAYER_X2 && y >= PLAYER_Y1 && y < PLAYER_Y2;
    }

    private static @Nullable Subject subject(AbstractContainerScreen<?> screen, Player player) {
        if (hoveringSelf(screen)) {
            ScanManager.Target target = ScanManager.resolveEntity(player);
            return target == null ? null : new Subject(SELF_KEY, target, new ScanSelfPayload());
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem() || slot instanceof ResultSlot || !slot.mayPickup(player)) {
            return null;
        }
        ScanManager.Target target = ScanManager.resolveStack(slot.getItem());
        if (target == null) {
            return null;
        }
        String key = slot.index + ":" + ScanManager.itemKey(slot.getItem());
        return new Subject(key, target, new ScanSlotPayload(screen.getMenu().containerId, slot.index));
    }

    private static boolean scannable(Player player, ScanManager.Target target) {
        return target.aspects() != null && target.aspects().size() > 0 && !ScanManager.hasBeenScanned(player, target);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        AbstractContainerScreen<?> screen = player == null ? null : activeScreen(minecraft);
        Subject subject = screen == null ? null : subject(screen, player);
        if (subject == null) {
            reset();
            return;
        }
        if (!subject.key().equals(scanKey)) {
            scanKey = subject.key();
            scanTicks = 0;
            attempted = false;
        }
        if (attempted) {
            return;
        }
        if (!scannable(player, subject.target())) {
            scanTicks = 0;
            attempted = true;
            return;
        }
        scanTicks++;
        if (scanTicks % 2 == 0) {
            player.level().playLocalSound(
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.CAMERATICKS.get(),
                SoundSource.PLAYERS,
                0.2F,
                0.45F + player.level().getRandom().nextFloat() * 0.1F,
                false
            );
        }
        if (scanTicks >= SCAN_TICKS) {
            attempted = true;
            scanTicks = 0;
            if (ScanManager.validScan(subject.target().aspects(), player)) {
                ClientNetwork.sendToServer(subject.payload());
            }
        }
    }

    @SubscribeEvent
    static void onScreenRender(ScreenEvent.Render.Post event) {
        mouseX = event.getMouseX();
        mouseY = event.getMouseY();
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        if (scanTicks > 0 && !attempted) {
            drawProgress(graphics, minecraft);
        } else if (minecraft.player != null && activeScreen(minecraft) == screen && hoveringSelf(screen)) {
            drawSelfAspects(graphics, minecraft.player);
        }
    }

    private static void drawProgress(GuiGraphics graphics, Minecraft minecraft) {
        float progress = scanTicks / (float) SCAN_TICKS;
        StringBuilder text = new StringBuilder(Component.translatable("tcinventoryscan.scanning").getString());
        if (progress >= 0.75F) {
            text.append("...");
        } else if (progress >= 0.5F) {
            text.append("..");
        } else if (progress >= 0.25F) {
            text.append(".");
        }
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        graphics.drawString(minecraft.font, text.toString(), mouseX, mouseY - 30, 0xFFFFAA00, true);
        graphics.flush();
        graphics.pose().popPose();
    }

    private static void drawSelfAspects(GuiGraphics graphics, Player player) {
        ScanManager.Target target = ScanManager.resolveEntity(player);
        if (target == null || target.aspects() == null || !ScanManager.hasBeenScanned(player, target)) {
            return;
        }
        AspectList aspects = target.aspects();
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        int index = 0;
        for (Aspect aspect : aspects.getAspectsSortedAmount()) {
            int x = mouseX + 17 + index * 18 - 8;
            int y = mouseY + 7 - 33 - 8;
            AspectRenderer.drawBack(graphics, x, y);
            if (AspectVisibility.isKnown(aspect)) {
                AspectRenderer.drawTag(graphics, x, y, aspect, aspects.getAmount(aspect));
            } else {
                AspectRenderer.drawUnknown(graphics, x, y);
            }
            index++;
        }
        graphics.flush();
        graphics.pose().popPose();
    }
}
