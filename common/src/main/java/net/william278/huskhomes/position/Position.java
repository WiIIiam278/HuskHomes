package net.william278.huskhomes.position;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.william278.huskhomes.messenger.MessageSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

/**
 * Represents a position - a {@link Location} somewhere on the proxy network or server
 */
public class Position extends Location implements MessageSerializable {

    /**
     * The {@link Server} the position is on
     */
    public Server server;

    public Position(double x, double y, double z, float yaw, float pitch,
                    @NotNull World world, @NotNull Server server) {
        super(x, y, z, yaw, pitch, world);
        this.server = server;
    }

    public Position() {
    }

    public String toJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static Position fromJson(String json) {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(World.class, new TypeAdapter<World>() {
                    @Override
                    public void write(JsonWriter jsonWriter, World world) throws IOException {
                        jsonWriter.beginObject();
                        jsonWriter.name("name");
                        jsonWriter.value(world.name);
                        jsonWriter.name("uuid");
                        jsonWriter.value(world.uuid.toString());
                        jsonWriter.endObject();
                    }

                    @Override
                    public World read(JsonReader jsonReader) throws IOException {
                        final World readWorld = new World();
                        jsonReader.beginObject();
                        String fieldName = null;
                        while (jsonReader.hasNext()) {
                            JsonToken token = jsonReader.peek();
                            if (token.equals(JsonToken.NAME)) {
                                fieldName = jsonReader.nextName();
                            }
                            if (fieldName != null) {
                                if (fieldName.equals("name")) {
                                    jsonReader.peek();
                                    readWorld.name = jsonReader.nextString();
                                }
                                if (fieldName.equals("uuid")) {
                                    jsonReader.peek();
                                    readWorld.uuid = UUID.fromString(jsonReader.nextString());
                                }
                            }
                        }
                        jsonReader.endObject();
                        return readWorld;
                    }
                })
                .registerTypeAdapter(Server.class, new TypeAdapter<Server>() {
                    @Override
                    public void write(JsonWriter jsonWriter, Server world) throws IOException {
                        jsonWriter.beginObject();
                        jsonWriter.name("name");
                        jsonWriter.value(world.name);
                        jsonWriter.endObject();
                    }

                    @Override
                    public Server read(JsonReader jsonReader) throws IOException {
                        final Server readServer = new Server();
                        jsonReader.beginObject();
                        String fieldName = null;
                        while (jsonReader.hasNext()) {
                            JsonToken token = jsonReader.peek();
                            if (token.equals(JsonToken.NAME)) {
                                fieldName = jsonReader.nextName();
                            }
                            if (fieldName != null) {
                                if (fieldName.equals("name")) {
                                    jsonReader.peek();
                                    readServer.name = jsonReader.nextString();
                                }
                            }
                        }
                        jsonReader.endObject();
                        return readServer;
                    }
                })
                .create().fromJson(json, Position.class);
    }
}
