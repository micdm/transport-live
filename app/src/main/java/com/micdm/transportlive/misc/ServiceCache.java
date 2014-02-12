package com.micdm.transportlive.misc;

import android.content.Context;
import com.micdm.transportlive.data.Direction;
import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

import java.io.*;

public class ServiceCache {

    private static final String CACHE_FILE_NAME = "service.bin";

    public static ServiceCache getInstance(Context context) {
        return new ServiceCache(context);
    }

    private Context context;

    private ServiceCache(Context context) {
        this.context = context;
    }

    public Service get() {
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
        Transport transport = new Transport(input.readInt(), getTransportType(input.readUTF()));
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
            route.directions.add(readDirection(input));
        }
        return route;
    }

    private Direction readDirection(ObjectInputStream input) throws IOException {
        return new Direction(input.readInt(), input.readUTF(), input.readUTF());
    }

    public void set(Service service) {
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
        output.writeInt(transport.routes.size());
        for (Route route: transport.routes) {
            writeRoute(output, route);
        }
    }

    private void writeRoute(ObjectOutputStream output, Route route) throws IOException {
        output.writeInt(route.number);
        output.writeBoolean(route.isChecked);
        output.writeInt(route.directions.size());
        for (Direction direction: route.directions) {
            writeDirection(output, direction);
        }
    }

    private void writeDirection(ObjectOutputStream output, Direction direction) throws IOException {
        output.writeInt(direction.id);
        output.writeUTF(direction.start);
        output.writeUTF(direction.finish);
    }
}
