
package pss.rookscore.fragments;

import pss.rookscore.R;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.fragments.views.ScoresheetRoundScoreView;
import pss.rookscore.model.GameStateModel;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class ScoresheetFragment extends Fragment {

    private GameStateModel mGameStateModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scoresheet_fragment, container, false);
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

        LinearLayout ll = ((LinearLayout) getView().findViewById(R.id.scoresheetBodyParent));
        ll.removeAllViews();

        
        for (int i = 0; i < mGameStateModel.getRounds().size(); i++) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.round_score_row, ll, false);
        
            MarginLayoutParams marginParams = new MarginLayoutParams(ll.getLayoutParams());
            marginParams.setMargins(0,10,0,0);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);
            v.setLayoutParams(layoutParams);
            ll.addView(v, layoutParams);
//            ll.addView(v);
            ScoresheetRoundScoreView srv = (ScoresheetRoundScoreView) v
                    .findViewById(R.id.scoresheetHeaderView);
            srv.setRound(i);
            srv.setGameStateModel(mGameStateModel);

            
            
        }

    }

}
