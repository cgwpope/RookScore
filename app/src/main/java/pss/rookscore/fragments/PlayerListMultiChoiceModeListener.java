
package pss.rookscore.fragments;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.R;
import pss.rookscore.core.model.Player;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;

class PlayerListMultiChoiceModeListener implements MultiChoiceModeListener {

    private ListView mListView;
    private ListAdapter mAdapter;
    private PlayerSelectionListener mPlayerSelectionListener;
    private boolean mIgnoreFling;

    public PlayerListMultiChoiceModeListener(PlayerSelectionListener parentActivity, ListView lv) {
        mPlayerSelectionListener = parentActivity;
        mListView = lv;
        mAdapter = mListView.getAdapter();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(pss.rookscore.R.menu.player_list_multi_select_menu, menu);
        mIgnoreFling = true;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<Player> selectedPlayers = new ArrayList<Player>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mListView.isItemChecked(i)) {
                selectedPlayers.add((Player) mAdapter.getItem(i));
            }
        }

        if (item.getItemId() == R.id.addMultiplePlayers) {
            mPlayerSelectionListener.playerSelected(selectedPlayers);
            mode.finish();
            return true;
        } else if (item.getItemId() == R.id.deleteMultiplePlayers) {
            mPlayerSelectionListener.playerRemoved(selectedPlayers);
            mode.finish();
            return true;
        }

        return false;

    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mIgnoreFling = false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mListView.invalidateViews();
    }

    public boolean isIgnoreFling() {
        return mIgnoreFling;
    }

}
