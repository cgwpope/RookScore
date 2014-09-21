package pss.rookscore;

import java.nio.charset.Charset;

import android.nfc.NdefRecord;

public interface RookScoreNFCBroadcaster {
    //empty interface - just a tag

    
    public static class RookScoreNFCUtils {
        public static NdefRecord newTextRecord(String msg) {
            byte[] textBytes = msg.getBytes(Charset.forName("UTF-8"));
            return NdefRecord.createMime("application/vnd.pss.rookscore", textBytes);
        }

    }
}
