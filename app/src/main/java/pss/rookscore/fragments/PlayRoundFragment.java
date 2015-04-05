package pss.rookscore.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.Stack;

import pss.rookscore.R;
import pss.rookscore.RookScoreApplication;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.ruleset.RookRuleSet;
import pss.rookscore.core.ruleset.RoundController;
import pss.rookscore.core.ruleset.RoundStateModel;
import pss.rookscore.fragments.views.ScoresheetHeaderView;

/**
 * Created by t890428 on 2015-04-04.
 */
public class PlayRoundFragment extends Fragment implements BidFragment.BidSelectionListener, PlayerListFragment.PlayerSelectionListener{

    public static final String ROUND_STATE_MODEL = PlayRoundFragment.class.getName() + " .RoundStateModel";
    public static final String ROUND_BACK_STACK = PlayRoundFragment.class.getName() + " .RoundBackStack";



    private RoundController mRoundController;

    private GameStateModel mModel;

    private final Stack<RoundStateModel> mRoundStateStack = new Stack<RoundStateModel>();
    private View mView;

    public static interface PlayRoundFragmentParent {

        GameStateModel getGameStateModel();
        public void setTitle(String title);
        public void doneRound();
        public void updateBidSummary();
        void broadcastGameState();
    }

    private static enum AnimateRequest {
        DO_NOT_ANIMATE(0),
        ANIMATE_FORWARD (1),
        ANIMATE_REVERSE (-1);

        private final int mValue;

        private AnimateRequest(int value){
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initRound();

        if(savedInstanceState != null){
            mRoundController.setRoundState((RoundStateModel)savedInstanceState.getSerializable(ROUND_STATE_MODEL));
            mRoundStateStack.addAll((Stack<RoundStateModel>)savedInstanceState.getSerializable(ROUND_BACK_STACK));
        }


    }

    private void initRound() {
        mRoundStateStack.clear();

        mModel = ((PlayRoundFragmentParent)getActivity()).getGameStateModel();

        if(mModel == null){
            throw new IllegalArgumentException("Game state must be provided to " + getClass());
        }

        RookRuleSet rrs = ((RookScoreApplication)getActivity().getApplication()).buildRookRuleSet(mModel.getPlayers().size());
        if(rrs == null){
            throw new IllegalArgumentException("Unable to determine rule set for this game");
        }
        mRoundController = new RoundController(rrs);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView  = inflater.inflate(R.layout.play_round_fragment, container);
        return mView;
    }

    public RoundController getRoundController() {
        return mRoundController;
    }


    /**
     *
     * @return true if the back event was handled (consumed) by this fragment
     */
    public boolean backPressed() {
        if(mRoundStateStack.size() > 0){
            mRoundController.setRoundState(mRoundStateStack.pop());
            updateBidView(AnimateRequest.ANIMATE_REVERSE);
            return true;
        } else {
            return false;
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        serializeState(outState);
    }

    private void serializeState(Bundle outState) {
        outState.putSerializable(ROUND_STATE_MODEL, mRoundController.getRoundState());
        outState.putSerializable(ROUND_BACK_STACK, mRoundStateStack);
    }


    @Override
    public void onResume() {
        super.onResume();

        // based on state, and fragment mode, choose fragment to display
        updateBidView(AnimateRequest.DO_NOT_ANIMATE);
    }

    public void startNewRound() {
        initRound();
        updateBidView(AnimateRequest.DO_NOT_ANIMATE);
    }



    private void updateBidView(final AnimateRequest animateRequest) {
        // assume single fragment for now
        Fragment newFragment;

        RoundController.RoundState state = mRoundController.getRoundState().getState();
        if(state == null){
            newFragment = null;
        } else {
            switch (state) {
                case COLLECT_CALLER:
                    newFragment = prepareSelectCallerFragment();
                    break;
                case COLLECT_BID:
                    newFragment = prepareSelectBidFragment();
                    break;
                case COLLECT_PARTNER:
                    newFragment = prepareSelectPartnerFragment();
                    break;
                case COLLECT_MADE_BID:
                    newFragment = prepareSelectMadeBidFragment();
                    break;
                default:
                    newFragment = null;
            }

        }

        ((PlayRoundFragmentParent)getActivity()).broadcastGameState();


        if (newFragment != null) {
            final Fragment finalNewFragment = newFragment;

            final Runnable fragmentSwitchRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getChildFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.playRoundActivityFragmentParent, finalNewFragment);
                    //don't add to back-stack, we are supplying custom back-stack handling
                    fragmentTransaction.commit();

                }
            };


            if(!animateRequest.equals(AnimateRequest.DO_NOT_ANIMATE)){
                final View v = mView.findViewById(R.id.playRoundActivityFragmentParent);
                v.animate()
                        .translationX(-1 * animateRequest.getValue() * v.getWidth())
                        .setDuration(250)
                        .setInterpolator(new AccelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //the animation listener is added to the view, so without removing the listener, it will fire when the second animation finishes as well - we don't want this!
                                v.animate().setListener(null);


                                fragmentSwitchRunnable.run();

                                v.setTranslationX(animateRequest.getValue() * v.getWidth());

                                v.animate()
                                        .translationX(0)
                                        .setDuration(250)
                                        .setInterpolator(new DecelerateInterpolator())
                                        .start();
                            }

                        })
                        .start();
            } else {
                fragmentSwitchRunnable.run();
            }




        } else {


            ((PlayRoundFragmentParent)getActivity()).doneRound();
        }


        ((PlayRoundFragmentParent)getActivity()).updateBidSummary();

    }



    private Fragment prepareSelectMadeBidFragment() {
        BidFragment bidFragment = new MadeBidFragment();
        bidFragment.setRuleSet(mRoundController.getRules());
        Bundle bundle = new Bundle();
        bundle.putInt(BidFragment.kStartingBidArg, mRoundController.getRoundState().getRoundResult().getBid());
        bidFragment.setArguments(bundle);
        ((PlayRoundFragmentParent)getActivity()).setTitle("Points Made");
        return bidFragment;
    }


    private Fragment prepareSelectPartnerFragment() {
        PlayerListFragment playerListFragment = new InRoundPlayerListFragment();
        playerListFragment.setPlayerListSelectionListener(this);
        playerListFragment.setPlayerList(mModel.getPlayers());
        ((PlayRoundFragmentParent)getActivity()).setTitle("Select Partner");
        return playerListFragment;
    }

    private Fragment prepareSelectBidFragment() {
        BidFragment bidFragment = new BidFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BidFragment.kStartingBidArg, mRoundController.getRoundState().getRoundResult().getBid() == 0 ? 150 : mRoundController.getRoundState().getRoundResult().getBid());
        bidFragment.setArguments(bundle);
        bidFragment.setRuleSet(mRoundController.getRules());
        ((PlayRoundFragmentParent)getActivity()).setTitle("Enter Bid");
        return bidFragment;

    }

    private Fragment prepareSelectCallerFragment() {
        // start with showing the PlayerSelectionFragment
        PlayerListFragment playerListFragment = new InRoundPlayerListFragment();
        playerListFragment.setPlayerList(mModel.getPlayers());
        playerListFragment.setPlayerListSelectionListener(this);
        ((PlayRoundFragmentParent)getActivity()).setTitle("Select Caller");
        return playerListFragment;
    }


    /*
    PlayerSelectionListener, BidSelectionListener, RookScoreNFCBroadcaster
     */

    public void playerSelected(List<Player> playerNames) {
        //TODO: Ensure size == 1

        if(playerNames.size() == 1){
            mRoundController.playerSelected(playerNames.get(0));

            //store state
            mRoundStateStack.push(new RoundStateModel(mRoundController.getRoundState()));

            //advance to next
            mRoundController.getRoundState().setState(mRoundController.nextState());

            updateBidView(AnimateRequest.ANIMATE_FORWARD);
        }

    }

    @Override
    public void playerRemoved(List<Player> playerNames) {
        //no-op
    }

    public void bidSelected(int bid) {
        // assume done in order - needs to be improved...
        mRoundController.applyBid(bid);

        //store state
        mRoundStateStack.push(new RoundStateModel(mRoundController.getRoundState()));


        //advance to next state
        mRoundController.getRoundState().setState(mRoundController.nextState());


        updateBidView(AnimateRequest.ANIMATE_FORWARD);
    }








}
