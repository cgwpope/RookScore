
package pss.rookscore;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pss.rookscore.events.GameOverEvent;
import pss.rookscore.events.GameStateChangedEvent;
import pss.rookscore.model.GameStateModel;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class BluetoothReceiverService extends Service {

    public static final String DEVICE_ADDRESS = BluetoothReceiverService.class.getName() + ".DeviceAddress";
    public static final String SERVICE_UUID = BluetoothReceiverService.class.getName() + ".ServiceUUID";
    private EventBus mEventBus;
    private ExecutorService mExecutorService;
    public BluetoothConnectionRunnable mCurrentBluetoothConnectionRunnable;
    
    private final Object mRunnableStateLock = new Object();


    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
        mExecutorService.shutdown();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = ((RookScoreApplication)getApplication()).getEventBus();
        mEventBus.register(this);
        mExecutorService = Executors.newFixedThreadPool(1);
    }

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String address = intent.getStringExtra(DEVICE_ADDRESS);
        UUID uuid = (UUID)intent.getSerializableExtra(SERVICE_UUID);
        if(address != null && uuid != null){
            //this appears to be a valid launch
            
            synchronized (mRunnableStateLock) {
                if(mCurrentBluetoothConnectionRunnable != null){
                    //we are already connected - see if this is the same device. If so, ignore.
                    if(!mCurrentBluetoothConnectionRunnable.getBluetoothSocket().getRemoteDevice().getAddress().equals(address)){
                        //stop the current thread and start a new connection to the new address
                        mCurrentBluetoothConnectionRunnable.halt();
                        mExecutorService.submit(new BluetoothConnectionRunnable(address, uuid));
                    }
                }  else {
                    //we're not running. start a new connection to the provided address
                    mExecutorService.submit(new BluetoothConnectionRunnable(address, uuid));
                }
            }
            
        }
        
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Subscribe
    public void handleGameOverEvent(GameOverEvent e) {
        
        //we can only be observing one game at a time (for now...)
        //so if that game is over, halt the connection and stop the service
        
        synchronized (mRunnableStateLock) {
            if(mCurrentBluetoothConnectionRunnable != null){
                mCurrentBluetoothConnectionRunnable.halt();
            }
        }
        stopSelf();
    }
    
    
    private class BluetoothConnectionRunnable implements Runnable {
        private BluetoothSocket mBluetoothSocket;
        private String mAddress;
        private UUID mServiceUUID;

        public BluetoothConnectionRunnable(String address, UUID serviceUUID) {
            mAddress = address;
            mServiceUUID = serviceUUID;
        }
        
        @Override
        public void run() {
            
            //precondition to run() - mCurrentBluetoothConnectionRunnable must be null
            //if it's not, someone else must be running. Let them finish.
            
            synchronized (mRunnableStateLock) {
                if(mCurrentBluetoothConnectionRunnable != null){
                    return;
                } else {
                    mCurrentBluetoothConnectionRunnable = BluetoothConnectionRunnable.this;                    
                }
            }
            
            try {
                BluetoothDevice remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mAddress);
                mBluetoothSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(mServiceUUID);
                mBluetoothSocket.connect();
                // success! - use this socket from now on -

                InputStream inputStream = mBluetoothSocket.getInputStream();
                while (mBluetoothSocket != null) {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(inputStream);

                        Object o = ois.readObject();
                        processReceivedObject(o);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        //keep trying
                    }
                }
            } catch (IOException e) {
                // read failed. need a reset protocol. For now, just quit.
                e.printStackTrace();
                
                //this connection is done.
                //let the app know the current observed game is basically done
                mEventBus.post(new GameOverEvent(null));
            } finally {
                mCurrentBluetoothConnectionRunnable = null;
            }
        }
        
        private void processReceivedObject(Object o) {
            if(o instanceof GameStateModel){
                mEventBus.post(new GameStateChangedEvent((GameStateModel)o));
            } else if(BluetoothBroadcastService.GAVE_OVER_MSG.equals(o)){
                mEventBus.post(new GameOverEvent(null));
            }
        }
        
        private void halt() {
            if(mBluetoothSocket != null){
                try {
                    mBluetoothSocket.close();
                } catch (IOException e) {
                }
                mBluetoothSocket = null;
            }
        }
        
        public BluetoothSocket getBluetoothSocket() {
            return mBluetoothSocket;
        }
        
        
    }





}
