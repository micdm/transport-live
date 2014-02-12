package com.micdm.transportlive.server.handlers;

import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;
import com.micdm.transportlive.server.commands.GetTransportsCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GetTransportsCommandHandler extends CommandHandler {

    public GetTransportsCommandHandler() {
        super(Backend.FIRST);
    }

    @Override
    public GetTransportsCommand.Result handle() {
        try {
            String response = sendRequest("searchAllRouteTypes");
            JSONArray typesJson = (JSONArray) new JSONTokener(response).nextValue();
            Service service = ((GetTransportsCommand)command).service;
            for (int i = 0; i < typesJson.length(); i += 1) {
                JSONObject typeJson = typesJson.getJSONObject(i);
                Transport transport = new Transport(typeJson.getInt("typeId"), getType(typeJson.getString("typeName")));
                service.transports.add(transport);
            }
            return new GetTransportsCommand.Result(service);
        } catch (JSONException e) {
            throw new RuntimeException("can't parse JSON");
        }
    }

    private Transport.Type getType(String name) {
        if (name.equals("Автобус")) {
            return Transport.Type.BUS;
        }
        if (name.equals("Троллейбус")) {
            return Transport.Type.TROLLEYBUS;
        }
        if (name.equals("Трамвай")) {
            return Transport.Type.TRAM;
        }
        if (name.equals("Маршрутное такси")) {
            return Transport.Type.TAXI;
        }
        throw new RuntimeException("unknown transport type");
    }
}
