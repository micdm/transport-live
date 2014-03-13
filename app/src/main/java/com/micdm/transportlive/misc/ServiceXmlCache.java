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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.StringWriter;

public class ServiceXmlCache {

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

    private static final String CACHE_FILE_NAME = "service.bin";

    private Context context;
    private Service service;

    public ServiceXmlCache(Context context) {
        this.context = context;
    }

    public Service get() {
        if (service == null) {
            service = getFromCache();
        }
        return service;
    }

    private Service getFromCache() {
        try {
            InputStream input = context.getAssets().open("service.xml");
            Service service = readServiceFromFile(input);
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

    private Service readServiceFromFile(InputStream input) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler();
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.service;
    }

    public void put(Service service) {
        this.service = service;
        try {
            FileOutputStream output = context.openFileOutput(CACHE_FILE_NAME, Context.MODE_PRIVATE);
            writeServiceToFile(output, service);
            output.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }

    private void writeServiceToFile(FileOutputStream file, Service service) throws IOException {
        StringWriter output = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        //serializer.setOutput(file, "utf-8");
        serializer.setOutput(output);
        serializer.startDocument("utf-8", true);
        writeService(serializer, service);
        serializer.endDocument();
    }

    private void writeService(XmlSerializer serializer, Service service) throws IOException {
        serializer.startTag("", "service");
        serializer.startTag("", "transports");
        for (Transport transport: service.transports) {
            writeTransport(serializer, transport);
        }
        serializer.endTag("", "transports");
        serializer.endTag("", "service");
    }

    private void writeTransport(XmlSerializer serializer, Transport transport) throws IOException {
        serializer.startTag("", "transport");
        serializer.attribute("", "id", String.valueOf(transport.id));
        serializer.attribute("", "type", String.valueOf(transport.type));
        serializer.attribute("", "code", String.valueOf(transport.code));
        serializer.startTag("", "routes");
        for (Route route: transport.routes) {
            writeRoute(serializer, route);
        }
        serializer.endTag("", "routes");
        serializer.endTag("", "transport");
    }

    private void writeRoute(XmlSerializer serializer, Route route) throws IOException {
        serializer.startTag("", "route");
        serializer.attribute("", "number", String.valueOf(route.number));
        serializer.startTag("", "points");
        for (Point point: route.points) {
            writePoint(serializer, point);
        }
        serializer.endTag("", "points");
        serializer.startTag("", "directions");
        for (Direction direction: route.directions) {
            writeDirection(serializer, direction);
        }
        serializer.endTag("", "directions");
        serializer.endTag("", "route");
    }

    private void writePoint(XmlSerializer serializer, Point point) throws IOException {
        serializer.startTag("", "point");
        serializer.attribute("", "lat", String.valueOf(point.latitude));
        serializer.attribute("", "lon", String.valueOf(point.longitude));
        serializer.endTag("", "point");
    }

    private void writeDirection(XmlSerializer serializer, Direction direction) throws IOException {
        serializer.startTag("", "direction");
        serializer.attribute("", "id", String.valueOf(direction.id));
        serializer.startTag("", "stations");
        for (Station station: direction.stations) {
            writeStation(serializer, station);
        }
        serializer.endTag("", "stations");
        serializer.endTag("", "direction");
    }

    private void writeStation(XmlSerializer serializer, Station station) throws IOException {
        serializer.startTag("", "station");
        serializer.attribute("", "name", String.valueOf(station.name));
        serializer.attribute("", "lat", String.valueOf(station.latitude));
        serializer.attribute("", "lon", String.valueOf(station.longitude));
        serializer.endTag("", "station");
    }
}
