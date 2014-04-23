package com.micdm.transportlive.server.cities;

public class CityConfig {

    public static class Backend {

        public String host;
        public String path;

        public Backend(String host, String path) {
            this.host = host;
            this.path = path;
        }
    }

    public static CityConfig CITY_RYAZAN = new CityConfig("ryazan", new Backend("78.31.72.3", "/bus/common/map6/%s.php"));
    public static CityConfig CITY_TOMSK = new CityConfig("tomsk", new Backend("83.222.106.126", "/bus/common/map6/%s.php"));

    public String id;
    public Backend backend;

    private CityConfig(String id, Backend backend) {
        this.id = id;
        this.backend = backend;
    }
}
