package com.micdm.transportlive.server.handlers;

import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.data.Vehicle;
import com.micdm.transportlive.server.commands.GetVehiclesCommand;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GetVehiclesCommandHandler extends CommandHandler {

    public GetVehiclesCommandHandler() {
        super(Backend.SECOND);
    }

    @Override
    public GetVehiclesCommand.Result handle() {
        try {
            String response = sendRequest("getRoutesVehicles", getRequestParams());
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(IOUtils.toInputStream(response), null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, "", "vehicles");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                Vehicle vehicle = parseVehicle(parser);
                parser.nextTag();
            }
            return new GetVehiclesCommand.Result(null);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("can't parse XML");
        } catch (IOException e) {
            throw new RuntimeException("can't parse XML");
        }
    }

    private HashMap<String, String> getRequestParams() {
        String[] keys = getSelectedDirectionKeys();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ids", StringUtils.join(keys, '|'));
        return params;
    }

    private String[] getSelectedDirectionKeys() {
        ArrayList<String> keys = new ArrayList<String>();
        for (Transport transport: ((GetVehiclesCommand)command).service.transports) {
            for (Route route: transport.routes) {
                if (!route.isChecked) {
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

    private Vehicle parseVehicle(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, "", "veh");
        int id = Integer.valueOf(parser.getAttributeValue("", "id"));
        String number = parser.getAttributeValue("", "gos_num");
        int latitude = Integer.valueOf(parser.getAttributeValue("", "lat"));
        int longitude = Integer.valueOf(parser.getAttributeValue("", "lon"));
        return new Vehicle(id, number, latitude, longitude);
    }
}
