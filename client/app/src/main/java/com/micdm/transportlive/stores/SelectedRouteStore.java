package com.micdm.transportlive.stores;

import android.content.Context;
import android.util.Xml;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.SelectedRouteInfo;
import com.micdm.transportlive.data.Service;
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

public class SelectedRouteStore {

    private static class ContentHandler extends DefaultHandler {

        private final Service service;
        public final List<SelectedRouteInfo> selected = new ArrayList<SelectedRouteInfo>();

        public ContentHandler(Service service) {
            this.service = service;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("route")) {
                Transport transport = service.getTransportById(getTransportId(attrs));
                Route route = transport.getRouteByNumber(getRouteNumber(attrs));
                selected.add(new SelectedRouteInfo(transport, route));
            }
        }

        private int getTransportId(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("transport"));
        }

        private int getRouteNumber(Attributes attrs) {
            return Integer.valueOf(attrs.getValue("route"));
        }
    }

    private static final String FILE_NAME = "selected_routes.xml";

    private final Context context;

    public SelectedRouteStore(Context context) {
        this.context = context;
    }

    public List<SelectedRouteInfo> load(Service service) {
        try {
            InputStream input = context.openFileInput(FILE_NAME);
            List<SelectedRouteInfo> selected = unserialize(input, service);
            input.close();
            return selected;
        } catch (SAXException e) {
            return new ArrayList<SelectedRouteInfo>();
        } catch (IOException e) {
            return new ArrayList<SelectedRouteInfo>();
        }
    }

    private List<SelectedRouteInfo> unserialize(InputStream input, Service service) throws SAXException, IOException {
        ContentHandler handler = new ContentHandler(service);
        Xml.parse(input, Xml.Encoding.UTF_8, handler);
        return handler.selected;
    }

    public void put(List<SelectedRouteInfo> selected) {
        try {
            OutputStream output = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            serialize(output, selected);
            output.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private void serialize(OutputStream output, List<SelectedRouteInfo> selected) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag("", "routes");
        for (SelectedRouteInfo info: selected) {
            serializer.startTag("", "route");
            serializer.attribute("", "transport", String.valueOf(info.transport.id));
            serializer.attribute("", "route", String.valueOf(info.route.number));
            serializer.endTag("", "route");
        }
        serializer.endTag("", "routes");
        serializer.endDocument();
    }
}
