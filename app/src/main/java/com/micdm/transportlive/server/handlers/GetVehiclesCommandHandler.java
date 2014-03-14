package com.micdm.transportlive.server.handlers;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GetVehiclesCommandHandler extends CommandHandler {

    public GetVehiclesCommandHandler(Context context) {
        super(context);
    }

    @Override
    public GetVehiclesCommand.Result handle() {
        try {
            HashMap<String, String> params = getRequestParams(((GetVehiclesCommand) command).selected);
            String response = sendRequest("getRoutesVehicles", params);
            Service service = ((GetVehiclesCommand) command).service;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(IOUtils.toInputStream(response), null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, "", "vehicles");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, "", "veh");
                Transport transport = service.getTransportByCode(parseTransportCode(parser));
                Route route = transport.getRouteByNumber(parseRouteNumber(parser));
                Direction direction = route.getDirectionById(parseDirectionId(parser));
                Vehicle update = parseVehicle(parser);
                if (update == null) {
                    continue;
                }
                Vehicle vehicle = direction.getVehicleById(update.id);
                if (vehicle == null) {
                    direction.vehicles.add(update);
                } else {
                    vehicle.location = update.location;
                    vehicle.direction = update.direction;
                    vehicle.lastUpdate = update.lastUpdate;
                }
                parser.nextTag();
            }
            return new GetVehiclesCommand.Result(service);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("can't parse XML");
        } catch (IOException e) {
            throw new RuntimeException("can't parse XML");
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

    private String parseTransportCode(XmlPullParser parser) {
        return parser.getAttributeValue("", "rtype");
    }

    private int parseRouteNumber(XmlPullParser parser) {
        return Integer.valueOf(parser.getAttributeValue("", "rnum"));
    }

    private int parseDirectionId(XmlPullParser parser) {
        return Integer.valueOf(parser.getAttributeValue("", "rid"));
    }

    private Vehicle parseVehicle(XmlPullParser parser) {
        String id = parser.getAttributeValue("", "id");
        String number = parser.getAttributeValue("", "gos_num");
        int latitude = Integer.valueOf(parser.getAttributeValue("", "lat"));
        int longitude = Integer.valueOf(parser.getAttributeValue("", "lon"));
        int direction = Integer.valueOf(parser.getAttributeValue("", "dir"));
        Date lastUpdate = parseVehicleLastUpdate(parser);
        if (lastUpdate == null) {
            return null;
        }
        return new Vehicle(id, number, new Point(latitude, longitude), direction, lastUpdate);
    }

    private Date parseVehicleLastUpdate(XmlPullParser parser) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(parser.getAttributeValue("", "lasttime"));
        } catch (ParseException e) {
            return null;
        }
    }
}
