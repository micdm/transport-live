package com.micdm.transportlive.misc;

import android.content.Context;

import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Point;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Station;
import com.micdm.transportlive.data.Transport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class ServiceCache {

    private static final String CACHE_FILE_NAME = "service.bin";

    private Context context;
    private Service service;

    public ServiceCache(Context context) {
        this.context = context;
    }

    public Service get() {
        if (service == null) {
            service = getFromCache();
        }
        return service;
    }

//    private Service getFromCache() {
//        return null;
//    }

    private Service getFromCache() {
        try {
            FileInputStream input = context.openFileInput(CACHE_FILE_NAME);
            Service service = readServiceFromFile(input);
            input.close();
            return service;
        } catch (FileNotFoundException e) {
            return null;
        } catch (StreamCorruptedException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Service readServiceFromFile(FileInputStream file) throws IOException {
        ObjectInputStream input = new ObjectInputStream(file);
        Service service = readService(input);
        input.close();
        return service;
    }

    private Service readService(ObjectInputStream input) throws IOException {
        Service service = new Service();
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            service.transports.add(readTransport(input));
        }
        return service;
    }

    private Transport readTransport(ObjectInputStream input) throws IOException {
        Transport transport = new Transport(input.readInt(), getTransportType(input.readUTF()), input.readUTF());
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            transport.routes.add(readRoute(input));
        }
        return transport;
    }

    private Transport.Type getTransportType(String value) {
        for (Transport.Type type: Transport.Type.values()) {
            if (value.equals(type.toString())) {
                return type;
            }
        }
        throw new RuntimeException("unknown transport type");
    }

    private Route readRoute(ObjectInputStream input) throws IOException {
        Route route = new Route(input.readInt(), input.readBoolean());
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            route.points.add(readPoint(input));
        }
        count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            route.directions.add(readDirection(input));
        }
        return route;
    }

    private Point readPoint(ObjectInputStream input) throws IOException {
        return new Point(input.readInt(), input.readInt());
    }

    private Direction readDirection(ObjectInputStream input) throws IOException {
        Direction direction = new Direction(input.readInt());
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            direction.stations.add(readStation(input));
        }
        return direction;
    }

    private Station readStation(ObjectInputStream input) throws IOException {
        return new Station(input.readUTF(), readPoint(input));
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
        ObjectOutputStream output = new ObjectOutputStream(file);
        writeService(output, service);
        output.close();
    }

    private void writeService(ObjectOutputStream output, Service service) throws IOException {
        output.writeInt(service.transports.size());
        for (Transport transport: service.transports) {
            writeTransport(output, transport);
        }
    }

    private void writeTransport(ObjectOutputStream output, Transport transport) throws IOException {
        output.writeInt(transport.id);
        output.writeUTF(transport.type.toString());
        output.writeUTF(transport.code);
        output.writeInt(transport.routes.size());
        for (Route route: transport.routes) {
            writeRoute(output, route);
        }
    }

    private void writeRoute(ObjectOutputStream output, Route route) throws IOException {
        output.writeInt(route.number);
        output.writeBoolean(route.isSelected);
        output.writeInt(route.points.size());
        for (Point point: route.points) {
            writePoint(output, point);
        }
        output.writeInt(route.directions.size());
        for (Direction direction: route.directions) {
            writeDirection(output, direction);
        }
    }

    private void writePoint(ObjectOutputStream output, Point point) throws IOException {
        output.writeInt(point.latitude);
        output.writeInt(point.longitude);
    }

    private void writeDirection(ObjectOutputStream output, Direction direction) throws IOException {
        output.writeInt(direction.id);
        output.writeInt(direction.stations.size());
        for (Station station: direction.stations) {
            writeStation(output, station);
        }
    }

    private void writeStation(ObjectOutputStream output, Station station) throws IOException {
        output.writeUTF(station.name);
        writePoint(output, station.location);
    }
}
