package org.sen.senWorlds.world;

public class WorldData {

    private final String name;
    private final String environment; // normal, nether, end, flat, void

    public WorldData(String name, String environment) {
        this.name = name;
        this.environment = environment.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getEnvironment() {
        return environment;
    }
}
