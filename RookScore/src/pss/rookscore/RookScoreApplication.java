package pss.rookscore;

import com.google.common.eventbus.EventBus;

import android.app.Application;
import android.content.Intent;

public class RookScoreApplication extends Application {
    
    private EventBus mEventBus;

    //provide eventbus for sharing between running components
    
    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new EventBus();
        
    }
    
    
    public EventBus getEventBus() {
        return mEventBus;
    }
    
    

}
