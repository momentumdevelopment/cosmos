package cope.cosmos.client.events;

import cope.cosmos.event.annotation.Cancelable;
import cope.cosmos.event.listener.Event;

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
