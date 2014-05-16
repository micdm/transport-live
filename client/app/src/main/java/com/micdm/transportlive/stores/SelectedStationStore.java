package com.micdm.transportlive.stores;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedStationInfo;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SelectedStationStore {

    private static class ContentHandler extends DefaultHandler {

        private Service service;
        public SelectedStationInfo selected;

        public ContentHandler(Service service) {
            this.service = service;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("station")) {
                Transport transport = service.getTransportById(getTransportId(attrs));
                Route route = transport.getRouteByNumber(getRouteNumber(attrs));
                Direction direction = route.getDirectionById(getDirectionId(attrs));
                Station station = direction.getStationById(getStationId(attrs));
                selected = new SelectedStationInfo(transport, route, direction, station);
            }
        }

        private int getTransportId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("transport"));
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("route"));
        }

        private int getDirectionId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("course"));
        }

        private int getStationId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("station"));
        }
    }

    private static final String FILE_NAME = "selected_station.xml";

    private Context context;

    public SelectedStationStore(Context context) {
        this.context = context;
    }

    public SelectedStationInfo load(Service service) {
        try {
            InputStream input = context.openFileInput(FILE_NAME);
            SelectedStationInfo selected = unserialize(input, service);
            input.close();
            return selected;
        } catch (SAXException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private SelectedStationInfo unserialize(InputStream input, Service service) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler(service);
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.selected;
    }

    public void put(SelectedStationInfo selected) {
        try {
            OutputStream output = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            serialize(output, selected);
            output.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private void serialize(OutputStream output, SelectedStationInfo selected) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag("", "station");
        serializer.attribute("", "transport", String.valueOf(selected.transport.id));
        serializer.attribute("", "route", String.valueOf(selected.route.number));
        serializer.attribute("", "course", String.valueOf(selected.direction.id));
        serializer.attribute("", "station", String.valueOf(selected.station.id));
        serializer.endTag("", "station");
        serializer.endDocument();
    }
}
