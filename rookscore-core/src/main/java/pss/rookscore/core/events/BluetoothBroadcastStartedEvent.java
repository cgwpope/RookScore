package pss.rookscore.core.events;

public class BluetoothBroadcastStartedEvent {

    public final String mAddress;
    
    public BluetoothBroadcastStartedEvent(String address) {
        mAddress = address;
    }

}
