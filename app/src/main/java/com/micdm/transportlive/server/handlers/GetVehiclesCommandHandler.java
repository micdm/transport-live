package com.micdm.transportlive.server.handlers;

import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
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

public class GetVehiclesCommandHandler extends CommandHandler {

    public GetVehiclesCommandHandler() {
        super(Backend.SECOND);
    }

    @Override
    public GetVehiclesCommand.Result handle() {
        Service service = ((GetVehiclesCommand)command).service;
        HashMap<String, String> params = getRequestParams();
        if (params == null) {
            return new GetVehiclesCommand.Result(service);
        }
        try {
            String response = sendRequest("getRoutesVehicles", params);
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

    private HashMap<String, String> getRequestParams() {
        String[] keys = getSelectedDirectionKeys();
        if (keys.length == 0) {
            return null;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ids", StringUtils.join(keys, '|'));
        return params;
    }

    private String[] getSelectedDirectionKeys() {
        ArrayList<String> keys = new ArrayList<String>();
        for (Transport transport: ((GetVehiclesCommand)command).service.transports) {
            for (Route route: transport.routes) {
                if (!route.isSelected) {
                    continue;
                }
                for (Direction direction: route.directions) {
                    keys.add(String.format("%s;%s", direction.id, getTransportId(transport)));
                }
            }
        }
        return keys.toArray(new String[keys.size()]);
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
        int id = Integer.valueOf(parser.getAttributeValue("", "id"));
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
