
package pss.rookscore;

import java.io.UnsupportedEncodingException;

import pss.rookscore.events.GameOverEvent;
import pss.rookscore.events.GameStateChangedEvent;
import pss.rookscore.fragments.ScoresheetFragment;
import pss.rookscore.model.GameStateModel;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;

import com.google.common.eventbus.Subscribe;

public class ObserveGameActivity extends FragmentActivity {

    protected BluetoothSocket mRemoteDeviceSocket;
    private ScoresheetFragment mScoresheetFragment;
    private GameStateModel mLatestGameState;
    private String mRemoteDeviceAddress;

    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.observe_game_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((RookScoreApplication) getApplication()).getEventBus().register(this);

        mScoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        if (mLatestGameState != null) {
            mScoresheetFragment.setGameStateModel(mLatestGameState);
        } else {
            mScoresheetFragment.setGameStateModel(new GameStateModel());
        }

        checkForNDEFMessageStartup();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        //confirm stop observing, and if so, perform game over
        ((RookScoreApplication) getApplication()).getEventBus().post(new GameOverEvent());

    }

    private void checkForNDEFMessageStartup() {
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                for (int i = 0; i < rawMsgs.length; i++) {
                    NdefMessage msg = (NdefMessage) rawMsgs[i];
                    try {
                        if(msg.getRecords().length == 1){
                            String bluetoothAddress = new String(msg.getRecords()[0].getPayload(), "UTF-8");

                            if (bluetoothAddress.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$")) {
                                mRemoteDeviceAddress = bluetoothAddress;
                                
                                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                } else {
                                    startBluetoothReceiverService();
                                }
                            }
                            
                        }
                    } catch (UnsupportedEncodingException e) {

                    }
                }
            }
        } else {
            // cheat
            mRemoteDeviceAddress = "CC:FA:00:33:B1:14";
            startBluetoothReceiverService();
        }
    }

    private void startBluetoothReceiverService() {
        Intent startReceiverIntent = new Intent(this, BluetoothReceiverService.class);
        startReceiverIntent.putExtra(BluetoothReceiverService.DEVICE_ADDRESS, mRemoteDeviceAddress);
        startReceiverIntent.putExtra(BluetoothReceiverService.SERVICE_UUID, MainActivity.ROOK_SCORE_BLUETOOTH_SERVICE_UUID);
        startService(startReceiverIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            startBluetoothReceiverService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((RookScoreApplication) getApplication()).getEventBus().unregister(this);
    }

    @Subscribe
    public void handleGameStateChanged(final GameStateChangedEvent e) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScoresheetFragment.setGameStateModel(e.mModel);
                mLatestGameState = e.mModel;
            }
        });
    }

    @Subscribe
    public void handleGameOverEvent(GameOverEvent e) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLatestGameState = (GameStateModel) savedInstanceState.getSerializable("GAME_STATE");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLatestGameState != null) {
            outState.putSerializable("GAME_STATE", mLatestGameState);
        }
    }

}
