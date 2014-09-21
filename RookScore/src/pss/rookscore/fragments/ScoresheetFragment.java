
package pss.rookscore.fragments;

import java.util.ArrayList;

import pss.rookscore.GameActivity;
import pss.rookscore.R;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.fragments.views.ScoresheetRoundScoreView;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.RoundSummary;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
        
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                
                promptToRemoveRound(position);
                
                return true;
            }
        });
        
        return v;
        
        
    }
    
    protected void promptToRemoveRound(final int position) {
        // prompt to delete round
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.scoresheet_remove_round_dialog_title)
                .setMessage(R.string.scoresheet_remove_round_dialog_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeRound(position);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();

        
    }

    protected void removeRound(final int position) {
        
        ListView lv = (ListView)getView().findViewById(R.id.roundScoreListview);
        View v = lv.getChildAt(position);

        Animation listItemSlideAnimation = AnimationUtils.loadAnimation(getActivity(), R.animator.list_item_slide_anim);
        v.startAnimation(listItemSlideAnimation);
        listItemSlideAnimation.setInterpolator(new AccelerateInterpolator());
        listItemSlideAnimation.setDuration(500);
        listItemSlideAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            
            @Override
            public void onAnimationEnd(Animation arg0) {
                ((GameActivity)getActivity()).removeRound(position);
                getView().invalidate();
            }
        });
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new ArrayAdapter<RoundSummary>(getActivity(),R.layout.round_score_row, new ArrayList<RoundSummary>()){
            public View getView(int position, View convertView, ViewGroup parent) {
                ScoresheetRoundScoreView shv = null;
                View v;
                if(convertView != null && (shv = (ScoresheetRoundScoreView)convertView.findViewById(R.id.scoresheetHeaderView)) != null){
                    v = convertView;
                } else {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.round_score_row, parent, false);
                    shv = (ScoresheetRoundScoreView)v.findViewById(R.id.scoresheetHeaderView);
                }

                shv.setGameStateModel(mGameStateModel);
                shv.setRound(position);

                return v;
            };
        };

    }

    public void setGameStateModel(GameStateModel model) {
        // update the view
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).setGameStateModel(model);

        mGameStateModel = model;

        scoreUpdated();
        
        //ensure the last row is always visible when updating game state
        ListView lv = (ListView)getView().findViewById(R.id.roundScoreListview);
        lv.smoothScrollToPosition(mListAdapter.getCount() - 1);
        
        
    }

    public void scoreUpdated() {
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).scoreUpdated();
        mListAdapter.clear();
        mListAdapter.addAll(mGameStateModel.computeRoundScores());
    }

}
