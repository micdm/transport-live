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

    public static CityConfig CITY_RYAZAN = new CityConfig("ryazan", new Backend("bus62.ru", "/php/%s.php"), new Backend("78.31.72.3", "/bus/common/map6/%s.php"));
    public static CityConfig CITY_TOMSK = new CityConfig("tomsk", new Backend("bus62.ru", "/tomsk/php/%s.php"), new Backend("83.137.52.160", "/bus/common/map6/%s.php"));

    public String id;
    public Backend firstBackend;
    public Backend secondBackend;

    protected CityConfig(String id, Backend firstBackend, Backend secondBackend) {
        this.id = id;
        this.firstBackend = firstBackend;
        this.secondBackend = secondBackend;
    }
}
