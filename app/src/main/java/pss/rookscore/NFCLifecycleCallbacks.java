package pss.rookscore;

import java.nio.charset.Charset;

import pss.rookscore.events.BluetoothBroadcastStartedEvent;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NFCLifecycleCallbacks implements ActivityLifecycleCallbacks {

    public interface RookScoreNFCBroadcaster {
        //empty interface - just a tag
    }
    
    
    private NfcAdapter mNFCAdapter;
    private String mBluetoothAdapterAddress;
    private EventBus mEventBus;
    private Activity mLastActivityCreated;

    public NFCLifecycleCallbacks(EventBus eventBus, NfcAdapter nfcAdapter) {
        
        mNFCAdapter = nfcAdapter;
        mEventBus = eventBus;
        mEventBus.register(this);
        
    }
    
    @Subscribe
    public void handleBluetoothBroadcastStarted(BluetoothBroadcastStartedEvent e) {
        mBluetoothAdapterAddress = e.mAddress;
        if(mLastActivityCreated != null && mLastActivityCreated instanceof RookScoreNFCBroadcaster){
            mNFCAdapter.setNdefPushMessage(new NdefMessage(newTextRecord(e.mAddress)), mLastActivityCreated);
        }
    }
    

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
            
    }

    void setNFCBroadcastMessageForActivity(Activity activity) {
        if(activity instanceof RookScoreNFCBroadcaster && mBluetoothAdapterAddress != null){
            mNFCAdapter.setNdefPushMessage(new NdefMessage(newTextRecord(mBluetoothAdapterAddress)), activity);
        } else {
            mNFCAdapter.setNdefPushMessage(null, activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mLastActivityCreated = activity;
        setNFCBroadcastMessageForActivity(activity);

    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    
    public static NdefRecord newTextRecord(String msg) {
        byte[] textBytes = msg.getBytes(Charset.forName("UTF-8"));
        return NdefRecord.createMime("application/vnd.pss.rookscore", textBytes);
    }

    
}
