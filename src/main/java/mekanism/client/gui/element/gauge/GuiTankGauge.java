package mekanism.client.gui.element.gauge;

import javax.annotation.Nullable;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketDropperUse.DropperAction;
import mekanism.common.network.PacketDropperUse.TankType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

public abstract class GuiTankGauge<T, TANK> extends GuiGauge<T> implements IJEIIngredientHelper {

    private final ITankInfoHandler<TANK> infoHandler;
    private final TankType tankType;

    public GuiTankGauge(GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, ITankInfoHandler<TANK> infoHandler, TankType tankType) {
        super(type, gui, x, y, sizeX, sizeY);
        this.infoHandler = infoHandler;
        this.tankType = tankType;
    }

    public TANK getTank() {
        return infoHandler.getTank();
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        if (func_231047_b_(mouseX, mouseY) && tankType != null) {
            ItemStack stack = GuiTexturedElement.minecraft.player.inventory.getItemStack();
            if (guiObj instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                int index = infoHandler.getTankIndex();
                if (index != -1) {
                    DropperAction action;
                    if (button == 0) {
                        action = Screen.func_231173_s_() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                    } else {
                        action = DropperAction.DRAIN_DROPPER;
                    }
                    Mekanism.packetHandler.sendToServer(new PacketDropperUse(((GuiMekanismTile<?, ?>) guiObj).getTileEntity().getPos(), action, tankType, index));
                }
                return true;
            }
        }
        return super.func_231044_a_(mouseX, mouseY, button);
    }

    public interface ITankInfoHandler<TANK> {

        @Nullable
        TANK getTank();

        int getTankIndex();
    }
}