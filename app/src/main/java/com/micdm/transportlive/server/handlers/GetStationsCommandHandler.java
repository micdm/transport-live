package com.micdm.transportlive.server.handlers;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.commands.GetStationsCommand;

import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class GetStationsCommandHandler extends CommandHandler {

    public GetStationsCommandHandler(Context context) {
        super(context, Backend.SECOND);
    }

    @Override
    public GetStationsCommand.Result handle() {
        Service service = ((GetStationsCommand)command).service;
        Transport transport = ((GetStationsCommand)command).transport;
        Route route = ((GetStationsCommand)command).route;
        HashMap<String, String> params = getRequestParams(transport, route);
        try {
            String response = sendRequest("getRouteStations", params);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(IOUtils.toInputStream(response), null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, "", "stations");
            Direction direction = null;
            Iterator<Direction> directions = route.directions.iterator();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, "", "station");
                Station station = parseStation(parser);
                if (parser.getAttributeValue("", "end") != null) {
                    if (direction == null) {
                        direction = directions.next();
                        direction.stations.add(station);
                    } else {
                        direction.stations.add(station);
                        direction = null;
                    }
                } else {
                    direction.stations.add(station);
                }
                parser.nextTag();
            }
            return new GetStationsCommand.Result(service);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("can't parse XML");
        } catch (IOException e) {
            throw new RuntimeException("can't parse XML");
        }
    }

    private HashMap<String, String> getRequestParams(Transport transport, Route route) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", String.valueOf(getTransportId(transport)));
        params.put("id1", String.valueOf(route.directions.get(0).id));
        params.put("id2", String.valueOf(route.directions.get(1).id));
        return params;
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

    private Station parseStation(XmlPullParser parser) {
        String name = parser.getAttributeValue("", "name");
        int latitude = Integer.valueOf(parser.getAttributeValue("", "lat0"));
        int longitude = Integer.valueOf(parser.getAttributeValue("", "lon0"));
        return new Station(name, latitude, longitude);
    }
}
