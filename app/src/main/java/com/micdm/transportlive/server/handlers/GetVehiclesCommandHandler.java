package com.micdm.transportlive.server.handlers;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.data.VehicleInfo;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GetVehiclesCommandHandler extends CommandHandler {

    private static class ContentHandler extends DefaultHandler {

        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private List<SelectedRouteInfo> selected;
        public List<VehicleInfo> vehicles = new ArrayList<VehicleInfo>();

        public ContentHandler(List<SelectedRouteInfo> selected) {
            this.selected = selected;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("veh")) {
                Date lastUpdate = getLastVehicleUpdate(attrs);
                if (lastUpdate == null) {
                    return;
                }
                SelectedRouteInfo info = getSelectedRouteInfo(getTransportCode(attrs), getRouteNumber(attrs));
                Direction direction = info.route.getDirectionById(getDirectionId(attrs));
                Vehicle vehicle = new Vehicle(getVehicleId(attrs), getVehicleNumber(attrs), getVehicleLatitude(attrs), getVehicleLongitude(attrs), getVehicleDirection(attrs), lastUpdate);
                vehicles.add(new VehicleInfo(info.transport, info.route, direction, vehicle));
            }
        }

        private SelectedRouteInfo getSelectedRouteInfo(String transportCode, int routeNumber) {
            for (SelectedRouteInfo info: selected) {
                if (info.transport.code.equals(transportCode) && info.route.number == routeNumber) {
                    return info;
                }
            }
            throw new RuntimeException("cannot find route info");
        }

        private String getTransportCode(Attributes attrs) {
            return attrs.getValue("", "rtype");
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "rnum"));
        }

        private int getDirectionId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "rid"));
        }

        private String getVehicleId(Attributes attrs) {
            return attrs.getValue("", "id");
        }

        private String getVehicleNumber(Attributes attrs) {
            return attrs.getValue("", "gos_num");
        }

        private int getVehicleLatitude(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "lat"));
        }

        private int getVehicleLongitude(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "lon"));
        }

        private int getVehicleDirection(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "dir"));
        }

        private Date getLastVehicleUpdate(Attributes attrs) {
            try {
                return DATE_FORMAT.parse(attrs.getValue("", "lasttime"));
            } catch (ParseException e) {
                return null;
            }
        }
    }

    public GetVehiclesCommandHandler(Context context) {
        super(context);
    }

    @Override
    public GetVehiclesCommand.Result handle() {
        try {
            List<SelectedRouteInfo> selected = ((GetVehiclesCommand) command).selected;
            HashMap<String, String> params = getRequestParams(selected);
            String response = sendRequest("getRoutesVehicles", params);
            ContentHandler handler = new ContentHandler(selected);
            Xml.parse(response, handler);
            return new GetVehiclesCommand.Result(handler.vehicles);
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }

    private HashMap<String, String> getRequestParams(List<SelectedRouteInfo> selected) {
        List<String> keys = getSelectedDirectionKeys(selected);
        if (keys.size() == 0) {
            throw new RuntimeException("cannot build request: no route selected");
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ids", StringUtils.join(keys, '|'));
        return params;
    }

    private List<String> getSelectedDirectionKeys(List<SelectedRouteInfo> selected) {
        List<String> keys = new ArrayList<String>();
        for (SelectedRouteInfo info: selected) {
            for (Direction direction: info.route.directions) {
                keys.add(String.format("%s;%s", direction.id, getTransportId(info.transport)));
            }
        }
        return keys;
    }

    private int getTransportId(Transport transport) {
        switch (transport.type) {
            case BUS:
            case TROLLEYBUS:
            case TAXI:
                return 0;
            case TRAM:
                return 1;
            default:
                throw new RuntimeException("unknown transport type");
        }
    }
}
