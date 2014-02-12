package com.micdm.transportlive.server.commands;

import com.micdm.transportlive.data.Service;

public class GetTransportsCommand implements Command {

    public static class Result implements Command.Result {

        public Service service;

        public Result(Service service) {
            this.service = service;
        }
    }

    public Service service;

    public GetTransportsCommand(Service service) {
        this.service = service;
    }
}
