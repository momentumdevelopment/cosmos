package cope.cosmos.client.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class TabOverlayEvent extends Event {

    private String information;

    public TabOverlayEvent(String information) {
        this.information = information;
    }

    public void setInformation(String in) {
        information = in;
    }

    public String getInformation() {
        return information;
    }
}
