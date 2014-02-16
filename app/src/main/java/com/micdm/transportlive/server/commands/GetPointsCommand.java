package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.Route;
import com.micdm.transportlive.data.Service;
import com.micdm.transportlive.data.Transport;

public class GetPointsCommand implements Command {

    public static class Result implements Command.Result {

        public Service service;

        public Result(Service service) {
            this.service = service;
        }
    }

    public Service service;
    public Transport transport;
    public Route route;

    public GetPointsCommand(Service service, Transport transport, Route route) {
        this.service = service;
        this.transport = transport;
        this.route = route;
    }
}
