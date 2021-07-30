package cope.cosmos.client.features.modules.misc;

import java.util.concurrent.ThreadLocalRandom;

import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.util.world.WorldUtil;

public class FakePlayer extends Module {
	public static FakePlayer INSTANCE;
	
	public FakePlayer() {
        super("FakePlayer", Category.MISC, "Spawns in a indestructible client-side player");
        INSTANCE = this;
        setExempt(true);
    }

    public static Setting<Boolean> inventory = new Setting<>("Inventory", "Sync the fake player inventory", true);
    public static Setting<Boolean> health = new Setting<>("Health", "Sync the fakeplayer health", true);
	
	public int id = -1;

    @Override
    public void onEnable() {
        super.onEnable();
        WorldUtil.createFakePlayer(mc.player.getGameProfile(), id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE), inventory.getValue(), health.getValue());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.world.removeEntityFromWorld(id);
        id = -1;
    }

    public int getID() {
        return id;
    }
}
