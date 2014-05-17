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
import java.util.ArrayList;
import java.util.List;

public class SelectedStationStore {

    private static class ContentHandler extends DefaultHandler {

        private final Service service;
        public final List<SelectedStationInfo> selected = new ArrayList<SelectedStationInfo>();

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
                selected.add(new SelectedStationInfo(transport, route, direction, station));
            }
        }

        private int getTransportId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("transport"));
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("route"));
        }

        private int getDirectionId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("direction"));
        }

        private int getStationId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("station"));
        }
    }

    private static final String FILE_NAME = "selected_stations.xml";

    private final Context context;

    public SelectedStationStore(Context context) {
        this.context = context;
    }

    public List<SelectedStationInfo> load(Service service) {
        try {
            InputStream input = context.openFileInput(FILE_NAME);
            List<SelectedStationInfo> selected = unserialize(input, service);
            input.close();
            return selected;
        } catch (SAXException e) {
            return new ArrayList<SelectedStationInfo>();
        } catch (IOException e) {
            return new ArrayList<SelectedStationInfo>();
        }
    }

    private List<SelectedStationInfo> unserialize(InputStream input, Service service) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler(service);
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.selected;
    }

    public void put(List<SelectedStationInfo> selected) {
        try {
            OutputStream output = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            serialize(output, selected);
            output.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private void serialize(OutputStream output, List<SelectedStationInfo> selected) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag("", "stations");
        for (SelectedStationInfo info: selected) {
            serializer.startTag("", "station");
            serializer.attribute("", "transport", String.valueOf(info.transport.id));
            serializer.attribute("", "route", String.valueOf(info.route.number));
            serializer.attribute("", "direction", String.valueOf(info.direction.id));
            serializer.attribute("", "station", String.valueOf(info.station.id));
            serializer.endTag("", "station");
        }
        serializer.endTag("", "stations");
        serializer.endDocument();
    }
}
