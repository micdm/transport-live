package com.micdm.transportlive.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.micdm.transportlive.R;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.misc.ServiceHandler;
import com.micdm.transportlive.misc.Utils;

public class RouteListFragment extends Fragment {

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
                view = View.inflate(getActivity(), R.layout.view_route_list_title, null);
            }
            ((TextView) view).setText(Utils.getTransportName(getActivity(), transport));
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
            final Transport transport = getGroup(groupPosition);
            final Route route = getChild(groupPosition, childPosition);
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.view_route_list_item, null);
            }
            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.is_selected);
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(handler.isRouteSelected(transport, route));
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                    ((ServiceHandler) getActivity()).selectRoute(transport, route, isChecked);
                }
            });
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ServiceHandler) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler.setOnLoadServiceListener(new ServiceHandler.OnLoadServiceListener() {
            @Override
            public void onLoadService(Service service) {
                ExpandableListView listView = (ExpandableListView) getView().findViewById(R.id.route_list);
                listView.setGroupIndicator(null);
                ExpandableListAdapter adapter = new RouteListAdapter(service);
                listView.setAdapter(adapter);
                for (int i = 0; i < adapter.getGroupCount(); i += 1) {
                    listView.expandGroup(i);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.setOnLoadServiceListener(null);
    }
}
