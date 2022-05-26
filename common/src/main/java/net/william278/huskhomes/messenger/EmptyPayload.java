package net.william278.huskhomes.messenger;

import com.google.gson.GsonBuilder;

public class EmptyPayload implements MessageSerializable {
    @Override
    public String toJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
