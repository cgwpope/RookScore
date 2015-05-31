
package pss.rookscore.fragments;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.GameActivity;
import pss.rookscore.R;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import pss.rookscore.fragments.views.DrawStrategy;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.fragments.views.ScoresheetRoundScoreView;

@SuppressLint("NewApi")
public class ScoresheetFragment extends Fragment {

    private GameStateModel mGameStateModel;
    private List<Player> mSortedPlayerList;

    private ArrayAdapter<RoundSummary> mListAdapter;
    private List<RoundSummary> mComputeRoundScores;

    private ScoresheetRoundScoreView.DrawStrategyHolder mSharedDrawStrategyHolder = new ScoresheetRoundScoreView.DrawStrategyHolder();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scoresheet_fragment, container, false);
        ListView lv = (ListView) v.findViewById(R.id.roundScoreListview);
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

        ListView lv = (ListView) getView().findViewById(R.id.roundScoreListview);
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
                ((GameActivity) getActivity()).removeRound(position);
                getView().invalidate();
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new ArrayAdapter<RoundSummary>(getActivity(), R.layout.round_score_row, new ArrayList<RoundSummary>()) {
            public View getView(int position, View convertView, ViewGroup parent) {
                ScoresheetRoundScoreView scoresheetRoundScoreView = null;
                View v;
                if (convertView != null && (scoresheetRoundScoreView = (ScoresheetRoundScoreView) convertView.findViewById(R.id.scoresheetHeaderView)) != null) {
                    v = convertView;
                } else {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.round_score_row, parent, false);
                    scoresheetRoundScoreView = (ScoresheetRoundScoreView) v.findViewById(R.id.scoresheetHeaderView);
                    //players won't ever change.
                }

                scoresheetRoundScoreView.setDrawStrategy(mSharedDrawStrategyHolder);
                scoresheetRoundScoreView.setPlayers(mSortedPlayerList);
                scoresheetRoundScoreView.setRoundScores(mComputeRoundScores);
                scoresheetRoundScoreView.setRound(position);
                scoresheetRoundScoreView.scoreUpdated();

                return v;
            };
        };

    }

    public void setGameStateModel(GameStateModel model) {
        if(getView() != null){
            // update the view
            ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).setGameStateModel(model);

            mGameStateModel = model;

            scoreUpdated();

            // ensure the last row is always visible when updating game state
            ListView lv = (ListView) getView().findViewById(R.id.roundScoreListview);
            lv.smoothScrollToPosition(mListAdapter.getCount() - 1);
        } else {
            //transitioning away from this fragment
        }

    }

    public void scoreUpdated() {
        ((ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView)).scoreUpdated();
        mListAdapter.clear();

        mComputeRoundScores = mGameStateModel.computeRoundScores();
        mSortedPlayerList = new ArrayList<>(mGameStateModel.getPlayers());
        if(mGameStateModel.getRounds().size() > 0) {
            ModelUtilities.sortPlayerNames(mSortedPlayerList, mGameStateModel.getRounds(), mGameStateModel.computeRoundScores());
        }
        mSharedDrawStrategyHolder.setDS(null);

        mListAdapter.addAll(mComputeRoundScores);
        
    }

    public void runExitAnimation(Runnable r) {

        // ok, move the scoresheet out of the way first, then move the header to
        // the bottom
        View v = getView().findViewById(R.id.roundScoreListview);
        v.animate().translationX(-getView().getWidth())
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        final ScoresheetHeaderView shv = (ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView);
        v = getView().findViewById(R.id.scoresheet_header_card_view);
        v.animate().translationY(getView().getHeight() - getResources().getDimension(R.dimen.card_view_padding) * 2 - v.getHeight())
                .setDuration(500)
                .withEndAction(r)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        shv.setFractionReservedForSummaryColumn(1 - animation.getAnimatedFraction());
                        shv.postInvalidate();
                    }
                })

                .setStartDelay(125)
                .start();

    }

    public void runEnterAnimation() {
        final ScoresheetHeaderView shv = (ScoresheetHeaderView) getView().findViewById(R.id.scoresheetHeaderView);
        View v = getView().findViewById(R.id.scoresheet_header_card_view);
        v.animate().translationY(0)
                .setDuration(350)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        shv.setFractionReservedForSummaryColumn( animation.getAnimatedFraction());
                        shv.postInvalidate();
                    }
                })

                .start();
        
        v = getView().findViewById(R.id.roundScoreListview);
        v.animate().translationX(0)
                .setDuration(250)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

    }

}
