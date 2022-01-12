package cope.cosmos.client.clickgui.ethius.feature;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.clickgui.ethius.EthiusGuiScreen;
import cope.cosmos.client.events.GuiScreenClosedEvent;
import cope.cosmos.client.events.SettingUpdateEvent;
import cope.cosmos.client.features.modules.Category;
import cope.cosmos.client.features.setting.Setting;
import cope.cosmos.client.manager.managers.ModuleManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CategoryWindowFeature extends TabbedWindowFeature {
    public CategoryWindowFeature(int width, int height, Category category) {
        super(StringUtils.capitalise(category.toString().toLowerCase(Locale.ROOT)), width, height, ModuleManager.getModules(i -> i.getCategory() == category).stream().map(i -> {
            final List<WindowFeature> comps = i.getSettings().stream().filter(j -> !j.hasParent()).map(s -> new SettingWindowFeature(0, 0, s)).collect(Collectors.toList());
            {
                final Setting<Boolean> s = new Setting<>("Enabled", i.isEnabled());
                s.setModule(i);
                s.setDescription("Enable/Disable " + i.getName());
                comps.add(new SettingWindowFeature(0, 0, s));
                Cosmos.EVENT_BUS.register(new Object() {
                    @SubscribeEvent
                    public void onSettingUpdate(SettingUpdateEvent e) {
                        if (e.getSetting() == s) {
                            if (s.getValue()) {
                                i.enable();
                            } else {
                                i.disable();
                            }
                        }
                    }

                    @SubscribeEvent
                    public void onScreenClose(GuiScreenClosedEvent event) {
                        if (event.getCurrentScreen() instanceof EthiusGuiScreen) {
                            Cosmos.EVENT_BUS.unregister(this);
                        }
                    }
                });
            }
            {
                final Setting<Character> s = new Setting<>("Bind", '0');
                s.setModule(i);
                s.setDescription("Bind key for " + i.getName());
                comps.add(new SettingWindowFeature(0, 0, s));
            }
            {
                final Setting<Boolean> s = new Setting<>("Drawn", i.isDrawn());
                s.setModule(i);
                s.setDescription("Allow " + i.getName() + " to be drawn");
                Cosmos.EVENT_BUS.register(new Object() {
                    @SubscribeEvent
                    public void onSettingChange(SettingUpdateEvent event) {
                        if (event.getSetting() == s) {
                            i.setDrawn(s.getValue());
                        }
                    }

                    @SubscribeEvent
                    public void onScreenClose(GuiScreenClosedEvent event) {
                        if (event.getCurrentScreen() instanceof EthiusGuiScreen) {
                            Cosmos.EVENT_BUS.unregister(this);
                        }
                    }
                });
                comps.add(new SettingWindowFeature(0, 0, s));
            }
            final TabbedWindowFeature t = new TabbedWindowFeature(i.getName(), 0, 0, comps.toArray(new WindowFeature[0]));
            t.type = TabbedWindowFeatureType.LEFT;
            t.tooltip = i.getDescription();
            t.backgroundColor = 0xff232329;
            t.selectColor = 0xff17171D;
            return t;
        }).toArray(WindowFeature[]::new));
        this.type = TabbedWindowFeatureType.LEFT;
    }
}
