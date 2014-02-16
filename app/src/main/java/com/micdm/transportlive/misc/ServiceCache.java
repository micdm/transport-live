package com.micdm.transportlive.misc;

import android.content.Context;

import com.micdm.transportlive.data.Direction;
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

    private static Service cached;

    public static Service get(Context context) {
        if (cached == null) {
            cached = getFromCache(context);
        }
        return cached;
    }

    private static Service getFromCache(Context context) {
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

    private static Service readServiceFromFile(FileInputStream file) throws IOException {
        ObjectInputStream input = new ObjectInputStream(file);
        Service service = readService(input);
        input.close();
        return service;
    }

    private static Service readService(ObjectInputStream input) throws IOException {
        Service service = new Service();
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            service.transports.add(readTransport(input));
        }
        return service;
    }

    private static Transport readTransport(ObjectInputStream input) throws IOException {
        Transport transport = new Transport(input.readInt(), getTransportType(input.readUTF()), input.readUTF());
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            transport.routes.add(readRoute(input));
        }
        return transport;
    }

    private static Transport.Type getTransportType(String value) {
        for (Transport.Type type: Transport.Type.values()) {
            if (value.equals(type.toString())) {
                return type;
            }
        }
        throw new RuntimeException("unknown transport type");
    }

    private static Route readRoute(ObjectInputStream input) throws IOException {
        Route route = new Route(input.readInt(), input.readBoolean());
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            route.directions.add(readDirection(input));
        }
        return route;
    }

    private static Direction readDirection(ObjectInputStream input) throws IOException {
        Direction direction = new Direction(input.readInt());
        int count = input.readInt();
        for (int i = 0; i < count; i += 1) {
            direction.stations.add(readStation(input));
        }
        return direction;
    }

    private static Station readStation(ObjectInputStream input) throws IOException {
        return new Station(input.readUTF(), input.readInt(), input.readInt());
    }

    public static void set(Context context, Service service) {
        cached = service;
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

    private static void writeServiceToFile(FileOutputStream file, Service service) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(file);
        writeService(output, service);
        output.close();
    }

    private static void writeService(ObjectOutputStream output, Service service) throws IOException {
        output.writeInt(service.transports.size());
        for (Transport transport: service.transports) {
            writeTransport(output, transport);
        }
    }

    private static void writeTransport(ObjectOutputStream output, Transport transport) throws IOException {
        output.writeInt(transport.id);
        output.writeUTF(transport.type.toString());
        output.writeUTF(transport.code);
        output.writeInt(transport.routes.size());
        for (Route route: transport.routes) {
            writeRoute(output, route);
        }
    }

    private static void writeRoute(ObjectOutputStream output, Route route) throws IOException {
        output.writeInt(route.number);
        output.writeBoolean(route.isChecked);
        output.writeInt(route.directions.size());
        for (Direction direction: route.directions) {
            writeDirection(output, direction);
        }
    }

    private static void writeDirection(ObjectOutputStream output, Direction direction) throws IOException {
        output.writeInt(direction.id);
        output.writeInt(direction.stations.size());
        for (Station station: direction.stations) {
            writeStation(output, station);
        }
    }

    private static void writeStation(ObjectOutputStream output, Station station) throws IOException {
        output.writeUTF(station.name);
        output.writeInt(station.latitude);
        output.writeInt(station.longitude);
    }
}
