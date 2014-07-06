
package pss.rookscore.fragments;

import java.util.ArrayList;

import pss.rookscore.R;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.fragments.views.ScoresheetRoundScoreView;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.RoundSummary;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressLint("NewApi")
public class ScoresheetFragment extends Fragment {

    private GameStateModel mGameStateModel;
    private ArrayAdapter<RoundSummary> mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scoresheet_fragment, container, false);
        ListView lv = (ListView)v.findViewById(R.id.roundScoreListview);
        lv.setAdapter(mListAdapter);
        return v;
        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new ArrayAdapter<RoundSummary>(getActivity(),R.layout.round_score_row, new ArrayList<RoundSummary>()){
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.round_score_row, parent, false);
                ScoresheetRoundScoreView shv = (ScoresheetRoundScoreView)v.findViewById(R.id.scoresheetHeaderView);
                shv.setGameStateModel(mGameStateModel);
                shv.setRound(position);
                
                if(position == mListAdapter.getCount() - 1){
//                    v.setElevation(10);
                }
                return v;
            };
        };

    }

    public void setGameStateModel(GameStateModel model) {
        // update the view
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView))
                .setGameStateModel(model);

        mGameStateModel = model;

        scoreUpdated();
    }

    public void scoreUpdated() {
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).scoreUpdated();
        mListAdapter.clear();
        mListAdapter.addAll(mGameStateModel.computeRoundScores());
    }

}
