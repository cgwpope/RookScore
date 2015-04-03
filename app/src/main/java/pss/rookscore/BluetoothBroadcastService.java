
package pss.rookscore;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pss.rookscore.core.events.BluetoothBroadcastStartedEvent;
import pss.rookscore.core.events.GameOverEvent;
import pss.rookscore.core.events.GameStateChangedEvent;
import pss.rookscore.core.events.SpectatorsChangedEvent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class BluetoothBroadcastService extends Service  {

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
                
                
                mRun = true;
                new Thread("Bluetooth listen thread") {
                    public void run() {
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
