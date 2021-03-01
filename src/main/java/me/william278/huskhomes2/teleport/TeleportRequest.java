package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;

import java.time.Instant;

public class TeleportRequest {

    private String senderName;
    private long requestExpiryTime;
    private String requestType;

    public TeleportRequest(String senderName, String requestType) {
        this.senderName = senderName;
        if (requestType.equalsIgnoreCase("tpa") || requestType.equalsIgnoreCase("tpahere")) {
            this.requestType = requestType.toLowerCase();
        }
        long currentUnixTime = Instant.now().getEpochSecond();
        requestExpiryTime = currentUnixTime + HuskHomes.getSettings().getTeleportRequestExpiryTime();
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRequestType() {
        return requestType;
    }

    public boolean getExpired() {
        long currentUnixTime = Instant.now().getEpochSecond();
        return currentUnixTime > requestExpiryTime;
    }
}
