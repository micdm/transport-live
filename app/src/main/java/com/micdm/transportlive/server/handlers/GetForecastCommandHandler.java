package com.micdm.transportlive.server.handlers;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Forecast;
import com.micdm.transportlive.data.ForecastVehicle;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.misc.Utils;
import com.micdm.transportlive.server.commands.GetForecastCommand;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.HashMap;

public class GetForecastCommandHandler extends CommandHandler {

    private static class ContentHandler extends DefaultHandler {

        private Service service;
        public Forecast forecast = new Forecast();

        public ContentHandler(Service service) {
            this.service = service;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("forecast")) {
                Transport transport = service.getTransportByCode(getTransportCode(attrs));
                Route route = transport.getRouteByNumber(getRouteNumber(attrs));
                int arrivalTime = getArrivalTime(attrs);
                forecast.vehicles.add(new ForecastVehicle(transport, route, arrivalTime));
            }
        }

        private String getTransportCode(Attributes attrs) {
            return attrs.getValue("", "route_type");
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "route_num"));
        }

        private int getArrivalTime(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("", "arr_time"));
        }
    }

    public GetForecastCommandHandler(Context context) {
        super(context);
    }

    @Override
    public GetForecastCommand.Result handle() {
        try {
            Service service = ((GetForecastCommand) command).service;
            SelectedStationInfo selected = ((GetForecastCommand) command).selected;
            HashMap<String, String> params = getRequestParams(selected);
            String response = sendRequest("getStationForecasts", params);
            ContentHandler handler = new ContentHandler(service);
            Xml.parse(response, handler);
            return new GetForecastCommand.Result(handler.forecast);
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }

    private HashMap<String, String> getRequestParams(SelectedStationInfo selected) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", String.valueOf(Utils.getTransportDriveType(selected.transport)));
        params.put("id", String.valueOf(selected.station.id));
        return params;
    }
}
