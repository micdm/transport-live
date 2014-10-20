package com.micdm.transportlive.stores;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.SelectedStation;
import com.micdm.transportlive.data.service.Direction;
import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
import com.micdm.transportlive.data.service.Station;
import com.micdm.transportlive.data.service.Transport;
import com.micdm.transportlive.misc.RandomItemSelector;
import com.micdm.transportlive.misc.Utils;

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
        public final List<SelectedStation> selected = new ArrayList<SelectedStation>();

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
                selected.add(new SelectedStation(transport.getId(), route.getNumber(), direction.getId(), station.getId(), true));
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

    private static final String FILE_NAME = "selected_stations_1.2.1.xml";
    private static final int DEFAULT_COUNT = 3;

    private final Context context;

    public SelectedStationStore(Context context) {
        this.context = context;
    }

    public List<SelectedStation> load(Service service) {
        if (!isFileExist()) {
            return getDefaultStations(service);
        }
        try {
            InputStream input = context.openFileInput(FILE_NAME);
            List<SelectedStation> selected = unserialize(input, service);
            input.close();
            return selected;
        } catch (SAXException e) {
            return new ArrayList<SelectedStation>();
        } catch (IOException e) {
            return new ArrayList<SelectedStation>();
        }
    }

    private boolean isFileExist() {
        return context.getFileStreamPath(FILE_NAME).exists();
    }

    private List<SelectedStation> getDefaultStations(Service service) {
        List<SelectedStation> stations = new ArrayList<SelectedStation>();
        while (stations.size() < DEFAULT_COUNT) {
            Transport transport = RandomItemSelector.get(service.getTransports());
            Route route = RandomItemSelector.get(transport.getRoutes());
            Direction direction = RandomItemSelector.get(route.getDirections());
            Station station = RandomItemSelector.get(direction.getStations());
            int transportId = transport.getId();
            int routeNumber = route.getNumber();
            int stationId = station.getId();
            if (!Utils.isStationSelected(stations, transportId, stationId)) {
                stations.add(new SelectedStation(transportId, routeNumber, direction.getId(), stationId, true));
            }
        }
        return stations;
    }

    private List<SelectedStation> unserialize(InputStream input, Service service) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler(service);
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.selected;
    }

    public void put(List<SelectedStation> selected) {
        try {
            OutputStream output = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            serialize(output, selected);
            output.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private void serialize(OutputStream output, List<SelectedStation> selected) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag("", "stations");
        for (SelectedStation station: selected) {
            serializer.startTag("", "station");
            serializer.attribute("", "transport", String.valueOf(station.getTransportId()));
            serializer.attribute("", "route", String.valueOf(station.getRouteNumber()));
            serializer.attribute("", "direction", String.valueOf(station.getDirectionId()));
            serializer.attribute("", "station", String.valueOf(station.getStationId()));
            serializer.endTag("", "station");
        }
        serializer.endTag("", "stations");
        serializer.endDocument();
    }
}
