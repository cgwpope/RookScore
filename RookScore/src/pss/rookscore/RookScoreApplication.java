package pss.rookscore;

import android.app.Application;
import android.nfc.NfcAdapter;

import com.google.common.eventbus.EventBus;

public class RookScoreApplication extends Application {
    
    private EventBus mEventBus;

    //provide eventbus for sharing between running components
    
    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new EventBus();
        
        
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        if(nfcAdapter != null){
            registerActivityLifecycleCallbacks(new NFCLifecycleCallbacks(mEventBus, nfcAdapter));    
        }
        
        
    }
    
    
    public EventBus getEventBus() {
        return mEventBus;
    }
    
    

}
