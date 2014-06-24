
package pss.rookscore.fragments;

import pss.rookscore.R;
import pss.rookscore.fragments.views.ScoresheetBodyView;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.model.GameStateModel;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScoresheetFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scoresheet_fragment, container, false);
    }

    public void setGameStateModel(GameStateModel model) {
        // update the view
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).setGameStateModel(model);
        ((ScoresheetBodyView) getView().findViewById(R.id.scoresheetBodyView)).setGameStateModel(model);

        scoreUpdated();
    }

    public void scoreUpdated() {
        ((ScoresheetBodyView) getView().findViewById(R.id.scoresheetBodyView)).scoreUpdated();
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).scoreUpdated();

    }

}
