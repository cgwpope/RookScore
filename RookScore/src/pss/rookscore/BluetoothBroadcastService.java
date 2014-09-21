
package pss.rookscore;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pss.rookscore.events.BluetoothBroadcastStartedEvent;
import pss.rookscore.events.GameOverEvent;
import pss.rookscore.events.GameStateChangedEvent;
import pss.rookscore.events.SpectatorsChangedEvent;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class BluetoothBroadcastService extends Service  {

    public static class BluetoothPairingNFCActivityLifecycleListener implements ActivityLifecycleCallbacks {

        private NfcAdapter mNFCAdapter;
        private BluetoothAdapter mBluetoothAdapter;

        public BluetoothPairingNFCActivityLifecycleListener(NfcAdapter nfcAdapter, BluetoothAdapter bluetoothAdapter) {
            mNFCAdapter = nfcAdapter;
            mBluetoothAdapter = bluetoothAdapter;
        }
        


        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onActivityStarted(Activity activity) {
            setNFCBroadcastMessageForActivity(activity);
                
        }

        void setNFCBroadcastMessageForActivity(Activity activity) {
            if(activity instanceof RookScoreNFCBroadcaster){
                mNFCAdapter.setNdefPushMessage(new NdefMessage(RookScoreNFCBroadcaster.RookScoreNFCUtils.newTextRecord(mBluetoothAdapter.getAddress())), activity);
            } else {
                mNFCAdapter.setNdefPushMessage(null, activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onActivityPaused(Activity activity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onActivityStopped(Activity activity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            // TODO Auto-generated method stub

        }

    }

    public static final Serializable GAVE_OVER_MSG = RookScoreApplication.class.getName() + ".GameOverMessage";
    private BluetoothServerSocket mBluetoothServerSocket;
    private boolean mRun;
    protected List<BluetoothSocket> mConnectedSockets = new ArrayList<BluetoothSocket>();
    private EventBus mEventBus;
    private ExecutorService mNetworkCommunicationThreadPool;
    
    
    
    
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        
        mNetworkCommunicationThreadPool = Executors.newFixedThreadPool(1);
        
        mEventBus = ((RookScoreApplication) getApplication()).getEventBus();
        mEventBus.register(this);

        // When the service is created, start the Bluetooth listener accept
        // loop.
        try {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            //handle case where we're missing hardware
            if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
                mBluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Rookscore SDP", MainActivity.ROOK_SCORE_BLUETOOTH_SERVICE_UUID);
                
                
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
                
                final BluetoothPairingNFCActivityLifecycleListener activityLifecycleCallback = (nfcAdapter == null ? null : new BluetoothPairingNFCActivityLifecycleListener(nfcAdapter, bluetoothAdapter));
                if (activityLifecycleCallback != null) {
                    getApplication().registerActivityLifecycleCallbacks(activityLifecycleCallback);
                }
                
                
                mRun = true;
                new Thread("Bluetooth listen thread") {
                    public void run() {
                        try {
                            while (mRun) {
                                try {
                                    
                                    //now the accept loop has started, notify the app that we're good to go for Bluetooth connections
                                    mEventBus.post(new BluetoothBroadcastStartedEvent(bluetoothAdapter.getAddress()));
                                    
                                    
                                    BluetoothSocket newConnectectSocket = mBluetoothServerSocket.accept();
                                    mConnectedSockets.add(newConnectectSocket);
                                    mEventBus.post(new SpectatorsChangedEvent());
                                } catch (IOException e) {
                                    Log.w(getClass().getName(), "Exception accepting on bluetooth server socket: " + e.getMessage());
                                    continue;
                                }
                            }
                        } finally {
                            if(activityLifecycleCallback != null){
                                getApplication().unregisterActivityLifecycleCallbacks(activityLifecycleCallback);
                            }
                        }
                    }
                }.start();
            }
        } catch (IOException e) {
            // we are not listening anymore... oh well
            Log.w(getClass().getName(), "Stopped listening on bluetooth server socket: " + e.getMessage());
        }
    }

    @Subscribe
    public void handleGameStateChanged(GameStateChangedEvent e) {
        sendSerializable(e.mModel);
    }
    
    @Subscribe
    public void handleGameOver(GameOverEvent e) {
        sendSerializable(GAVE_OVER_MSG);

        //ensure shutdown happens after all gave over msgs have been sent
        mNetworkCommunicationThreadPool.submit(new Runnable() {

            @Override
            public void run() {
                stopSelf();
            }
            
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
        mRun = false;
        if(mBluetoothServerSocket != null){
            try {
                mBluetoothServerSocket.close();
                for (BluetoothSocket bluetoothSocket : mConnectedSockets) {
                    bluetoothSocket.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private void sendSerializable(final Serializable s) {
        mNetworkCommunicationThreadPool.submit(new Runnable(){

            @Override
            public void run() {
                List<BluetoothSocket> toRemove = new ArrayList<BluetoothSocket>();
                for (BluetoothSocket bluetoothSocket : mConnectedSockets) {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(bluetoothSocket.getOutputStream());
                        oos.writeObject(s);
                        oos.flush();
                    } catch (IOException e) {
                        // communication with this client is probably hopeless. Remove
                        // it from the list of connected clients once we're done
                        // iterating over the list

                        toRemove.add(bluetoothSocket);
                        e.printStackTrace();
                    }
                }

                for (BluetoothSocket bluetoothSocket : toRemove) {
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        // this will likely also fail, ignore
                    }

                    mConnectedSockets.remove(bluetoothSocket);
                }
                
            }
            
        });
        
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    

    
  


}
