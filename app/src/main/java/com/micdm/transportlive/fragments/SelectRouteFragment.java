package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.interfaces.ServiceHandler;
import com.micdm.transportlive.misc.Utils;

public class SelectRouteFragment extends DialogFragment {

    private class RouteListAdapter extends BaseExpandableListAdapter {

        private Service service;

        public RouteListAdapter(Service service) {
            this.service = service;
        }

        @Override
        public int getGroupCount() {
            return service.transports.size();
        }

        @Override
        public int getChildrenCount(int position) {
            return getGroup(position).routes.size();
        }

        @Override
        public Transport getGroup(int position) {
            return service.transports.get(position);
        }

        @Override
        public Route getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).routes.get(childPosition);
        }

        @Override
        public long getGroupId(int position) {
            return getGroup(position).id;
        }

        @Override
        public long getChildId(int groupPosition, int childPodition) {
            return getChild(groupPosition, childPodition).number;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View view, ViewGroup viewGroup) {
            Transport transport = getGroup(position);
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.view_select_route_title, null);
            }
            ((TextView) view).setText(Utils.getTransportName(getActivity(), transport));
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
            final Transport transport = getGroup(groupPosition);
            final Route route = getChild(groupPosition, childPosition);
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.view_select_route_list_item, null);
            }
            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.is_selected);
            checkbox.setChecked(handler.isRouteSelected(transport, route));
            TextView numberView = (TextView) view.findViewById(R.id.number);
            numberView.setText(String.valueOf(route.number));
            TextView startView = (TextView) view.findViewById(R.id.start);
            startView.setText(route.getStart());
            TextView finishView = (TextView) view.findViewById(R.id.finish);
            finishView.setText(route.getFinish());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkbox.toggle();
                }
            });
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private ServiceHandler handler;
    private ServiceHandler.OnLoadServiceListener onLoadServiceListener = new ServiceHandler.OnLoadServiceListener() {
        @Override
        public void onLoadService(Service service) {
            ExpandableListView listView = (ExpandableListView) getDialog().findViewById(R.id.route_list);
            ExpandableListAdapter adapter = new RouteListAdapter(service);
            listView.setAdapter(adapter);
            for (int i = 0; i < adapter.getGroupCount(); i += 1) {
                listView.expandGroup(i);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ServiceHandler) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.fragment_select_route_title);
        builder.setView(View.inflate(getActivity(), R.layout.fragment_select_route, null));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO: обновить выделенные маршруты
            }
        });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.addOnLoadServiceListener(onLoadServiceListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeOnLoadServiceListener(onLoadServiceListener);
    }
}
