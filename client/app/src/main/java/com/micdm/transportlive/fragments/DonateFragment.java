package com.micdm.transportlive.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.donate.DonateProduct;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.DonateEvent;
import com.micdm.transportlive.events.events.LoadDonateProductsEvent;
import com.micdm.transportlive.events.events.RequestDonateEvent;
import com.micdm.transportlive.events.events.RequestLoadDonateProductsEvent;

import java.util.List;

public class DonateFragment extends DialogFragment {

    private class DonateListAdapter extends BaseAdapter {

        private final List<DonateProduct> products;

        public DonateListAdapter(List<DonateProduct> products) {
            this.products = products;
        }

        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public DonateProduct getItem(int position) {
            return products.get(position);
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
            DonateProduct product = getItem(position);
            TextView titleView = (TextView) view.findViewById(R.id.v__donate_list_item__title);
            titleView.setText(product.getTitle());
            TextView priceView = (TextView) view.findViewById(R.id.v__donate_list_item__price);
            priceView.setText(product.getPrice());
            return view;
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.f__donate__title));
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
                DonateProduct product = adapter.getItem(position);
                App.get().getEventManager().publish(new RequestDonateEvent(product));
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeForEvents();
        requestForData();
    }

    private void subscribeForEvents() {
        EventManager manager = App.get().getEventManager();
        manager.subscribe(this, EventType.LOAD_DONATE_PRODUCTS, new EventManager.OnEventListener<LoadDonateProductsEvent>() {
            @Override
            public void onEvent(LoadDonateProductsEvent event) {
                List<DonateProduct> products = event.getProducts();
                if (products != null) {
                    ListView view = (ListView) getDialog().findViewById(R.id.f__donate__donate_list);
                    view.setAdapter(new DonateListAdapter(products));
                }
            }
        });
        manager.subscribe(this, EventType.DONATE, new EventManager.OnEventListener<DonateEvent>() {
            @Override
            public void onEvent(DonateEvent event) {
                Toast.makeText(getActivity(), R.string.f__donate__thanks, Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadDonateProductsEvent());
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
