package com.micdm.transportlive.misc;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

public class ServiceLoader {

    private static class ContentHandler extends DefaultHandler {

        public Service service;
        private Transport transport;
        private Route route;
        private Point point;
        private Direction direction;
        private Station station;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("service")) {
                service = new Service();
            }
            if (localName.equals("transport")) {
                transport = new Transport(getTransportId(attrs), getTransportType(attrs), getTransportCode(attrs));
            }
            if (localName.equals("route")) {
                route = new Route(getRouteNumber(attrs));
            }
            if (localName.equals("point")) {
                point = new Point(getPointLatitude(attrs), getPointLongitude(attrs));
            }
            if (localName.equals("direction")) {
                direction = new Direction(getDirectionId(attrs));
            }
            if (localName.equals("station")) {
                station = new Station(getStationName(attrs), getStationLatitude(attrs), getStationLongitude(attrs));
            }
        }

        private int getTransportId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("id"));
        }

        private Transport.Type getTransportType(Attributes attrs) {
            String value = attrs.getValue("type");
            for (Transport.Type type: Transport.Type.values()) {
                if (value.equals(type.toString())) {
                    return type;
                }
            }
            throw new RuntimeException("unknown transport type");
        }

        private String getTransportCode(Attributes attrs) {
            return attrs.getValue("code");
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("number"));
        }

        private int getPointLatitude(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("lat"));
        }

        private int getPointLongitude(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("lon"));
        }

        private int getDirectionId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("id"));
        }

        private String getStationName(Attributes attrs) {
            return attrs.getValue("name");
        }

        private int getStationLatitude(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("lat"));
        }

        private int getStationLongitude(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("lon"));
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (localName.equals("station")) {
                direction.stations.add(station);
                station = null;
            }
            if (localName.equals("direction")) {
                route.directions.add(direction);
                direction = null;
            }
            if (localName.equals("point")) {
                route.points.add(point);
                point = null;
            }
            if (localName.equals("route")) {
                transport.routes.add(route);
                route = null;
            }
            if (localName.equals("transport")) {
                service.transports.add(transport);
                transport = null;
            }
        }
    }

    private Context context;
    private Service service;

    public ServiceLoader(Context context) {
        this.context = context;
    }

    public Service load() {
        if (service == null) {
            service = loadFromAssets();
        }
        return service;
    }

    private Service loadFromAssets() {
        try {
            InputStream input = context.getAssets().open("service.xml");
            Service service = unserialize(input);
            input.close();
            return service;
        } catch (FileNotFoundException e) {
            return null;
        } catch (StreamCorruptedException e) {
            return null;
        } catch (SAXException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Service unserialize(InputStream input) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler();
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.service;
    }
}
