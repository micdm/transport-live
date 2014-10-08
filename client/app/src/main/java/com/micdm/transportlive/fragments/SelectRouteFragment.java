package com.micdm.transportlive.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.micdm.transportlive.App;
import com.micdm.transportlive.R;
import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.data.service.Transport;
import com.micdm.transportlive.events.Event;
import com.micdm.transportlive.events.EventManager;
import com.micdm.transportlive.events.EventType;
import com.micdm.transportlive.events.events.LoadRoutesEvent;
import com.micdm.transportlive.events.events.LoadServiceEvent;
import com.micdm.transportlive.events.events.RequestLoadRoutesEvent;
import com.micdm.transportlive.events.events.RequestLoadServiceEvent;
import com.micdm.transportlive.events.events.RequestSelectRouteEvent;
import com.micdm.transportlive.events.events.RequestUnselectRouteEvent;
import com.micdm.transportlive.misc.RouteColors;
import com.micdm.transportlive.misc.Utils;

import java.util.List;

public class SelectRouteFragment extends DialogFragment {

    private class RouteListAdapter extends BaseExpandableListAdapter {

        private class ChildViewHolder {

            public final View colorView;
            public final CheckBox checkboxView;
            public final TextView numberView;
            public final TextView directionView;

            private ChildViewHolder(View colorView, CheckBox checkboxView, TextView numberView, TextView directionView) {
                this.colorView = colorView;
                this.checkboxView = checkboxView;
                this.numberView = numberView;
                this.directionView = directionView;
            }
        }

        private RouteColors colors;
        private List<Transport> transports;
        private List<SelectedRoute> selectedRoutes;

        public void setService(Service service) {
            colors = new RouteColors(service);
            transports = service.getTransports();
        }

        public void setSelectedRoutes(List<SelectedRoute> selectedRoutes) {
            this.selectedRoutes = selectedRoutes;
        }

        @Override
        public int getGroupCount() {
            return (transports == null) ? 0 : transports.size();
        }

        @Override
        public int getChildrenCount(int position) {
            return getGroup(position).getRoutes().size();
        }

        @Override
        public Transport getGroup(int position) {
            return transports.get(position);
        }

        @Override
        public Route getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getRoutes().get(childPosition);
        }

        @Override
        public long getGroupId(int position) {
            return getGroup(position).getId();
        }

        @Override
        public long getChildId(int groupPosition, int childPodition) {
            return getChild(groupPosition, childPodition).getNumber();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__select_route_title, null);
            }
            Transport transport = getGroup(position);
            ((TextView) view).setText(Utils.getTransportName(getActivity(), transport));
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.v__select_route_list_item, null);
            }
            final ChildViewHolder holder = getChildViewHolder(view);
            final Transport transport = getGroup(groupPosition);
            final Route route = getChild(groupPosition, childPosition);
            holder.colorView.setBackgroundColor(colors.get(route));
            holder.checkboxView.setOnCheckedChangeListener(null);
            holder.checkboxView.setChecked(Utils.isRouteSelected(selectedRoutes, transport.getId(), route.getNumber()));
            holder.checkboxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {
                    SelectedRoute selectedRoute = new SelectedRoute(transport.getId(), route.getNumber());
                    Event event = isChecked ? new RequestSelectRouteEvent(selectedRoute) : new RequestUnselectRouteEvent(selectedRoute);
                    App.get().getEventManager().publish(event);
                }
            });
            holder.numberView.setText(String.valueOf(route.getNumber()));
            holder.directionView.setText(getString(R.string.f__select_route__direction, route.getStart(), route.getFinish()));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.checkboxView.toggle();
                }
            });
            return view;
        }

        private ChildViewHolder getChildViewHolder(View view) {
            ChildViewHolder holder = (ChildViewHolder) view.getTag();
            if (holder != null) {
                return holder;
            }
            View colorView = view.findViewById(R.id.v__select_route_list_item__color);
            CheckBox checkboxView = (CheckBox) view.findViewById(R.id.v__select_route_list_item__is_selected);
            TextView numberView = (TextView) view.findViewById(R.id.v__select_route_list_item__number);
            TextView directionView = (TextView) view.findViewById(R.id.v__select_route_list_item__direction);
            holder = new ChildViewHolder(colorView, checkboxView, numberView, directionView);
            view.setTag(holder);
            return holder;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private ExpandableListView routesView;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.f__select_route__title);
        builder.setView(setupView());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        return builder.create();
    }

    private View setupView() {
        View view = View.inflate(getActivity(), R.layout.f__select_route, null);
        routesView = (ExpandableListView) view.findViewById(R.id.f__select_route__route_list);
        routesView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
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
        manager.subscribe(this, EventType.LOAD_SERVICE, new EventManager.OnEventListener<LoadServiceEvent>() {
            @Override
            public void onEvent(LoadServiceEvent event) {
                RouteListAdapter adapter = getAdapter();
                adapter.setService(event.getService());
                adapter.notifyDataSetChanged();
                expandAllGroups();
            }
        });
        manager.subscribe(this, EventType.LOAD_ROUTES, new EventManager.OnEventListener<LoadRoutesEvent>() {
            @Override
            public void onEvent(LoadRoutesEvent event) {
                RouteListAdapter adapter = getAdapter();
                adapter.setSelectedRoutes(event.getRoutes());
                adapter.notifyDataSetChanged();
                expandAllGroups();
            }
        });
    }

    private void requestForData() {
        EventManager manager = App.get().getEventManager();
        manager.publish(new RequestLoadServiceEvent());
        manager.publish(new RequestLoadRoutesEvent());
    }

    private RouteListAdapter getAdapter() {
        RouteListAdapter adapter = (RouteListAdapter) routesView.getExpandableListAdapter();
        if (adapter == null) {
            adapter = new RouteListAdapter();
            routesView.setAdapter(adapter);
        }
        return adapter;
    }

    private void expandAllGroups() {
        RouteListAdapter adapter = getAdapter();
        for (int i = 0; i < adapter.getGroupCount(); i += 1) {
            routesView.expandGroup(i);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        App.get().getEventManager().unsubscribeAll(this);
    }
}
