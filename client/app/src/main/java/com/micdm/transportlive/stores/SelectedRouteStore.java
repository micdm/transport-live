package com.micdm.transportlive.stores;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.SelectedRoute;
import com.micdm.transportlive.data.service.Route;
import com.micdm.transportlive.data.service.Service;
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

public class SelectedRouteStore {

    private static class ContentHandler extends DefaultHandler {

        private final Service service;
        public final List<SelectedRoute> selected = new ArrayList<SelectedRoute>();

        public ContentHandler(Service service) {
            this.service = service;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("route")) {
                Transport transport = service.getTransportById(getTransportId(attrs));
                Route route = transport.getRouteByNumber(getRouteNumber(attrs));
                selected.add(new SelectedRoute(transport.getId(), route.getNumber()));
            }
        }

        private int getTransportId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("transport"));
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("route"));
        }
    }

    private static final String FILE_NAME = "selected_routes_1.2.1.xml";
    private static final int DEFAULT_COUNT = 3;

    private final Context context;

    public SelectedRouteStore(Context context) {
        this.context = context;
    }

    public List<SelectedRoute> load(Service service) {
        if (!isFileExist()) {
            return getDefaultRoutes(service);
        }
        try {
            InputStream input = context.openFileInput(FILE_NAME);
            List<SelectedRoute> selected = unserialize(input, service);
            input.close();
            return selected;
        } catch (SAXException e) {
            return new ArrayList<SelectedRoute>();
        } catch (IOException e) {
            return new ArrayList<SelectedRoute>();
        }
    }

    private boolean isFileExist() {
        return context.getFileStreamPath(FILE_NAME).exists();
    }

    private List<SelectedRoute> getDefaultRoutes(Service service) {
        List<SelectedRoute> routes = new ArrayList<SelectedRoute>();
        while (routes.size() < DEFAULT_COUNT) {
            Transport transport = RandomItemSelector.get(service.getTransports());
            Route route = RandomItemSelector.get(transport.getRoutes());
            int transportId = transport.getId();
            int routeNumber = route.getNumber();
            if (!Utils.isRouteSelected(routes, transportId, routeNumber)) {
                routes.add(new SelectedRoute(transportId, routeNumber));
            }
        }
        return routes;
    }

    private List<SelectedRoute> unserialize(InputStream input, Service service) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler(service);
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.selected;
    }

    public void put(List<SelectedRoute> selected) {
        try {
            OutputStream output = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            serialize(output, selected);
            output.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private void serialize(OutputStream output, List<SelectedRoute> selected) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag("", "routes");
        for (SelectedRoute route: selected) {
            serializer.startTag("", "route");
            serializer.attribute("", "transport", String.valueOf(route.getTransportId()));
            serializer.attribute("", "route", String.valueOf(route.getRouteNumber()));
            serializer.endTag("", "route");
        }
        serializer.endTag("", "routes");
        serializer.endDocument();
    }
}
