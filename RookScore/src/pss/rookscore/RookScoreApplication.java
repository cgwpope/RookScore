package pss.rookscore;

import android.app.Application;

import com.google.common.eventbus.EventBus;

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
