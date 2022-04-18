package cope.cosmos.asm.mixins.render.gui.overlay;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.events.render.gui.TabListSizeEvent;
import cope.cosmos.client.events.render.gui.TabOverlayEvent;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public List<NetworkPlayerInfo> renderPlayerList(List<NetworkPlayerInfo> list, int fromIndex, int toIndex) {
        TabListSizeEvent tabListSizeEvent = new TabListSizeEvent();
        Cosmos.EVENT_BUS.post(tabListSizeEvent);

        if (tabListSizeEvent.isCanceled()) {
            return list.subList(0, list.size());
        }

        else {
            return list.subList(0, Math.min(list.size(), 80));
        }
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> info) {
        TabOverlayEvent tabOverlayEvent = new TabOverlayEvent(networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName()));
        Cosmos.EVENT_BUS.post(tabOverlayEvent);

        if (tabOverlayEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(tabOverlayEvent.getInformation());
        }
    }
}
