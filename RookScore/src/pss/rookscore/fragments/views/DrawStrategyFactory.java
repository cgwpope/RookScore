package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.model.RoundSummary;
import android.content.Context;
import android.graphics.Paint;

public class DrawStrategyFactory {
    public static DrawStrategy buildDrawStrategy(Context context, Paint p, List<String> playerNames, List<RoundSummary> roundSummaries, int totalWidth){
        
        if(playerNames.size() == 0 ){
            //use a null draw stratrg
            return new NullDrawStrategy();
        } else {
            //figure out if basic scores will fit into the space per player. Try a single-line strategy
            SingleLineDrawStrategy singleLineStrategy = new SingleLineDrawStrategy(context, p, playerNames, roundSummaries, totalWidth);
            //compute test values
            
            String maxScore = "99999";
            float maxWidth = p.measureText(maxScore);
            float widthPerPlayer = singleLineStrategy.getWidthPerPlayer();
            if(maxWidth > widthPerPlayer){
                //let's see if the double-line will allow this to work
                
                
                //for now, just return it
                return new DoubleLineDrawStrategy(context, p, playerNames, roundSummaries, totalWidth);
            } else {
                //we are OK to use the single-line strategy
                return singleLineStrategy;
            }
        }
    }

}
