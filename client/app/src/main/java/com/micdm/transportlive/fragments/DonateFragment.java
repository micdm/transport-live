package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateItem;
import com.micdm.transportlive.interfaces.DonateHandler;

import java.util.List;

// TODO: предусмотреть, что список не загрузился
public class DonateFragment extends DialogFragment {

    private class DonateListAdapter extends BaseAdapter {

        private final List<DonateItem> items;

        public DonateListAdapter(List<DonateItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public DonateItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__donate_list_item, null);
            }
            DonateItem item = getItem(position);
            TextView titleView = (TextView) view.findViewById(R.id.v__donate_list_item__title);
            titleView.setText(item.title);
            TextView priceView = (TextView) view.findViewById(R.id.v__donate_list_item__price);
            priceView.setText(item.price);
            return view;
        }
    }

    private DonateHandler handler;
    private final DonateHandler.OnLoadDonateItemsListener onLoadDonateItemsListener = new DonateHandler.OnLoadDonateItemsListener() {
        @Override
        public void onLoadDonateItems(List<DonateItem> items) {
            if (items != null) {
                ListView view = (ListView) getDialog().findViewById(R.id.f__donate__donate_list);
                view.setAdapter(new DonateListAdapter(items));
            }
        }
    };
    private final DonateHandler.OnDonateListener onDonateListener = new DonateHandler.OnDonateListener() {
        @Override
        public void onDonate() {
            Toast.makeText(getActivity(), R.string.fragment_donate_thanks, Toast.LENGTH_LONG).show();
            dismiss();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (DonateHandler) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.fragment_donate_title));
        builder.setView(getContent());
        return builder.create();
    }

    private View getContent() {
        View view = View.inflate(getActivity(), R.layout.f__donate, null);
        ListView listView = (ListView) view.findViewById(R.id.f__donate__donate_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                DonateListAdapter adapter = (DonateListAdapter) adapterView.getAdapter();
                DonateItem item = adapter.getItem(position);
                handler.makeDonation(item);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.addOnLoadDonateItemsListener(onLoadDonateItemsListener);
        handler.addOnDonateListener(onDonateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeOnLoadDonateItemsListener(onLoadDonateItemsListener);
        handler.removeOnDonateListener(onDonateListener);
    }
}
