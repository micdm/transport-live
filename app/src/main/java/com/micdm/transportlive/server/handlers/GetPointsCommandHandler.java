package com.micdm.transportlive.server.handlers;

import android.util.Xml;

import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.commands.GetPointsCommand;

import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

public class GetPointsCommandHandler extends CommandHandler {

    public GetPointsCommandHandler() {
        super(Backend.SECOND);
    }

    @Override
    public GetPointsCommand.Result handle() {
        Service service = ((GetPointsCommand)command).service;
        Transport transport = ((GetPointsCommand)command).transport;
        Route route = ((GetPointsCommand)command).route;
        HashMap<String, String> params = getRequestParams(transport, route);
        try {
            String response = sendRequest("getSubRoutePolyline", params);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(IOUtils.toInputStream(response), null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, "", "poly");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, "", "node");
                route.points.add(parsePoint(parser));
                parser.nextTag();
            }
            return new GetPointsCommand.Result(service);
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

    private Point parsePoint(XmlPullParser parser) {
        int latitude = Integer.valueOf(parser.getAttributeValue("", "lat"));
        int longitude = Integer.valueOf(parser.getAttributeValue("", "lon"));
        return new Point(latitude, longitude);
    }
}
