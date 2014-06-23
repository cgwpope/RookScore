package pss.rookscore.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pss.rookscore.GameStateModel;
import pss.rookscore.GameStateModel.RoundResult;
import pss.rookscore.R;
import pss.rookscore.fragments.views.RoundSummary;
import pss.rookscore.fragments.views.ScoresheetBodyView;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScoresheetFragment extends Fragment {

    private static final int MAX_SCORE = 180;
    private GameStateModel mGameStateModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scoresheet_fragment, container, false);
    }
    
    
    public void setGameStateModel(GameStateModel model){
        mGameStateModel = model;
        
        //compute summary
        
        //update the view
        ((ScoresheetHeaderView)getView().findViewById(R.id.scoresheetHeaderView)).setGameStateModel(model);
        ((ScoresheetBodyView)getView().findViewById(R.id.scoresheetBodyView)).setGameStateModel(model);

        scoreUpdated();
    }


	private List<RoundSummary> computeRoundScores(GameStateModel model) {
		List<RoundSummary> rounds = new ArrayList<RoundSummary>();
        
        for(int i = 0; i < model.getRounds().size(); i++){
            
            Map<String, Integer> previousRoundScores;
            if(i == 0){
                previousRoundScores = new HashMap<String, Integer>();
                for (String player : model.getPlayers()) {
                    previousRoundScores.put(player, 0);
                }
            } else {
                previousRoundScores = rounds.get(i - 1).getRoundScores();
            }
            
            
            Map<String, Integer> newRoundScore = computeRoundScore(model.getPlayers(), previousRoundScores, model.getRounds().get(i));
            
            rounds.add(new RoundSummary(model.getRounds().get(i), newRoundScore));
        }
        
        return rounds;
	}


    private Map<String, Integer> computeRoundScore(ArrayList<String> players, Map<String, Integer> previousRoundScores, RoundResult roundResult) {
        //main rook logic
        
        /*
         * 1. Determine calling side
         * 2. Determine defending side
         * 3. Determine delta for calling side
         * 4. Apply to calling side players
         * 5. Determine delate for defending side
         * 6. Apply to defending side players
         */
        
        Map<String, Integer> newScores = new HashMap<String, Integer>();
        Set<String> callers = new HashSet<String>();
        Set<String> defenders = new HashSet<String>();
        callers.add(roundResult.getCaller());
        
        for (String partner : roundResult.getParters()) {
            callers.add(partner);
        }
        
        
        for (String player : players) {
            if(!callers.contains(player)){
                defenders.add(player);
            }
        }
        
        int callingDelta;
        int defendingDelta;
        
        if(roundResult.getMade() >= roundResult.getBid()){
            callingDelta = roundResult.getMade();
            defendingDelta = MAX_SCORE - roundResult.getMade(); 
        } else {
            callingDelta = -1 * roundResult.getBid();
            defendingDelta = MAX_SCORE - roundResult.getMade(); 
        }
        
        
        //alone bonus!
        if(callers.size() == 1){
            if(callingDelta == MAX_SCORE){
                callingDelta += (20 + (players.size() - 4) * 10);
            }
        }
        
        for (String defender : defenders) {
            newScores.put(defender, previousRoundScores.get(defender) + defendingDelta);
        }
        
        for (String caller : callers) {
            newScores.put(caller, previousRoundScores.get(caller) + callingDelta);
        }
        
        
        return newScores;
    }


    public void scoreUpdated() {
        List<RoundSummary> computeRoundScores = computeRoundScores(mGameStateModel);
        ((ScoresheetBodyView)getView().findViewById(R.id.scoresheetBodyView)).setRoundScores(computeRoundScores);
        ((ScoresheetHeaderView)getView().findViewById(R.id.scoresheetHeaderView)).setRoundScores(computeRoundScores);

    }
    
    

    
    
    
    
    
    
}
