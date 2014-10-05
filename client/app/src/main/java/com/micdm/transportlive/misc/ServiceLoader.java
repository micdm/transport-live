package com.micdm.transportlive.misc;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ServiceLoader {

    private static class ContentHandler extends DefaultHandler {

        private Service service;
        private List<Transport> transports = new ArrayList<Transport>();
        private int transportId;
        private List<Station> transportStations = new ArrayList<Station>();
        private int stationId;
        private String stationName;
        private List<Route> routes = new ArrayList<Route>();
        private int routeNumber;
        private List<Direction> directions = new ArrayList<Direction>();
        private int directionId;
        private List<Station> directionStations = new ArrayList<Station>();

        private boolean isTransportDescription;

        public Service getService() {
            return service;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("transports")) {
                transports = new ArrayList<Transport>();
            }
            if (localName.equals("transport")) {
                transportId = getTransportId(attrs);
                isTransportDescription = true;
            }
            if (localName.equals("routes")) {
                routes = new ArrayList<Route>();
            }
            if (localName.equals("route")) {
                routeNumber = getRouteNumber(attrs);
            }
            if (localName.equals("directions")) {
                directions = new ArrayList<Direction>();
            }
            if (localName.equals("direction")) {
                directionId = getDirectionId(attrs);
                isTransportDescription = false;
            }
            if (localName.equals("stations")) {
                if (isTransportDescription) {
                    transportStations = new ArrayList<Station>();
                } else {
                    directionStations = new ArrayList<Station>();
                }
            }
            if (localName.equals("station")) {
                stationId = getStationId(attrs);
                stationName = getStationName(attrs);
            }
        }

        private int getTransportId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("id"));
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("number"));
        }

        private int getDirectionId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("id"));
        }

        private int getStationId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("id"));
        }

        private String getStationName(Attributes attrs) {
            return attrs.getValue("name");
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (localName.equals("station")) {
                if (isTransportDescription) {
                    transportStations.add(new Station(stationId, stationName));
                } else {
                    directionStations.add(getStationById(stationId));
                }
            }
            if (localName.equals("direction")) {
                directions.add(new Direction(directionId, directionStations));
            }
            if (localName.equals("route")) {
                routes.add(new Route(routeNumber, directions));
            }
            if (localName.equals("transport")) {
                transports.add(new Transport(transportId, transportStations, routes));
            }
            if (localName.equals("service")) {
                service = new Service(transports);
            }
        }

        private Station getStationById(int id) {
            for (Station station: transportStations) {
                if (station.getId() == id) {
                    return station;
                }
            }
            throw new RuntimeException(String.format("no station %s", id));
        }
    }

    private static final String SERVICE_ASSET_NAME = "service.xml";

    private final Context context;

    public ServiceLoader(Context context) {
        this.context = context;
    }

    public Service load() {
        try {
            InputStream input = context.getAssets().open(SERVICE_ASSET_NAME);
            Service service = unserialize(input);
            input.close();
            return service;
        } catch (SAXException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Service unserialize(InputStream input) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler();
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.getService();
    }
}
