package com.limelight.grid;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.limelight.PcView;
import com.limelight.R;
import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.nvstream.http.PairingManager;
import com.limelight.preferences.PreferenceConfiguration;

import java.util.Collections;
import java.util.Comparator;

public class PcGridAdapter extends GenericGridAdapter<PcView.ComputerObject> {

    public PcGridAdapter(Context context, PreferenceConfiguration prefs) {
        super(context, getLayoutIdForPreferences(prefs));
    }

    private static int getLayoutIdForPreferences(PreferenceConfiguration prefs) {
        return R.layout.pc_grid_item;
    }

    public void updateLayoutWithPreferences(Context context, PreferenceConfiguration prefs) {
        // This will trigger the view to reload with the new layout
        setLayoutId(getLayoutIdForPreferences(prefs));
    }

    public void addComputer(PcView.ComputerObject computer) {
        itemList.add(computer);
        sortList();
    }

    private void sortList() {
        Collections.sort(itemList, new Comparator<PcView.ComputerObject>() {
            @Override
            public int compare(PcView.ComputerObject lhs, PcView.ComputerObject rhs) {
                return lhs.details.name.toLowerCase().compareTo(rhs.details.name.toLowerCase());
            }
        });
    }

    public boolean removeComputer(PcView.ComputerObject computer) {
        return itemList.remove(computer);
    }

    private ComputerDetails.State getDisplayState(PcView.ComputerObject obj) {
        if (obj.details.state == ComputerDetails.State.UNKNOWN) {
            // Polling can transiently mark a host unknown even when we still have a known-good
            // active address from recent polls. Keep cards visually stable in that case.
            if (obj.details.activeAddress != null) {
                return ComputerDetails.State.ONLINE;
            }

            // Don't present long-lived UNKNOWN as "Refreshing" in the host grid.
            return ComputerDetails.State.OFFLINE;
        }

        return obj.details.state;
    }

    @Override
    public void populateView(View parentView, ImageView imgView, ProgressBar prgView, TextView txtView, ImageView overlayView, PcView.ComputerObject obj) {
        TextView statusView = parentView.findViewById(R.id.grid_status);
        ComputerDetails.State displayState = getDisplayState(obj);

        imgView.setImageResource(R.drawable.ic_computer);
        if (displayState == ComputerDetails.State.ONLINE) {
            imgView.setAlpha(1.0f);
        }
        else if (displayState == ComputerDetails.State.UNKNOWN) {
            imgView.setAlpha(0.8f);
        }
        else {
            imgView.setAlpha(0.6f);
        }

        if (displayState == ComputerDetails.State.UNKNOWN) {
            prgView.setVisibility(View.VISIBLE);
        }
        else {
            prgView.setVisibility(View.INVISIBLE);
        }

        txtView.setText(obj.details.name);
        txtView.setAlpha(1.0f);

        if (statusView != null) {
            if (obj.details.state == ComputerDetails.State.ONLINE &&
                    obj.details.pairState == PairingManager.PairState.NOT_PAIRED &&
                    obj.details.serverCert == null) {
                statusView.setText(R.string.pcview_status_pair_required);
                statusView.setBackgroundResource(R.drawable.bg_status_pair_required);
            }
            else if (displayState == ComputerDetails.State.ONLINE) {
                statusView.setText(R.string.pcview_menu_header_online);
                statusView.setBackgroundResource(R.drawable.bg_status_online);
            }
            else if (displayState == ComputerDetails.State.OFFLINE) {
                statusView.setText(R.string.pcview_menu_header_offline);
                statusView.setBackgroundResource(R.drawable.bg_status_offline);
            }
            else {
                statusView.setText(R.string.pcview_menu_header_unknown);
                statusView.setBackgroundResource(R.drawable.bg_status_refreshing);
            }
        }

        if (displayState == ComputerDetails.State.OFFLINE) {
            overlayView.setImageResource(R.drawable.ic_pc_offline);
            overlayView.setAlpha(0.4f);
            overlayView.setVisibility(View.VISIBLE);
        }
        // We must check if the status is exactly online and unpaired
        // to avoid colliding with the loading spinner when status is unknown
        else if (obj.details.state == ComputerDetails.State.ONLINE &&
                obj.details.pairState == PairingManager.PairState.NOT_PAIRED &&
                obj.details.serverCert == null) {
            overlayView.setImageResource(R.drawable.ic_lock);
            overlayView.setAlpha(1.0f);
            overlayView.setVisibility(View.VISIBLE);
        }
        else {
            overlayView.setVisibility(View.GONE);
        }
    }
}
