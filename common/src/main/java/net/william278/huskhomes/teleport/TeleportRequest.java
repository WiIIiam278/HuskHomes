/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.teleport;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Represents a request between players for one user to teleport to another, or vice versa.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TeleportRequest {

    @Expose
    @SerializedName("requester_name")
    private String requesterName;
    @Expose
    @SerializedName("requester_position")
    private Position requesterPosition;
    @Expose
    @SerializedName("expiry_time")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long expiryTime;
    @Expose
    private Type type;
    @Expose
    private Status status;
    @Expose
    @SerializedName("recipient_name")
    private String recipientName;

    /**
     * Create a teleport request.
     *
     * @param requester The user making the request
     */
    public TeleportRequest(@NotNull OnlineUser requester, @NotNull TeleportRequest.Type type, final long expiryTime) {
        this.setRequesterName(requester.getName());
        this.setRequesterPosition(requester.getPosition());
        this.setType(type);
        this.setStatus(Status.PENDING);
        this.expiryTime = expiryTime;
    }

    /**
     * Returns if the request has now expired.
     *
     * @return {@code true} the request has passed its expiry time
     */
    public boolean hasExpired() {
        return Instant.now().isAfter(Instant.ofEpochSecond(expiryTime));
    }


    /**
     * Types of teleport requests ({@code /tpa} or {@code /tpahere}).
     */
    public enum Type {
        /**
         * The request is a {@code /tpa} request, where the requester is requesting to teleport <i>to</i> the recipient.
         */
        TPA,
        /**
         * The request is a {@code /tpahere} request, where the requester is requesting that the recipient teleport
         * <i>to them</i>.
         */
        TPA_HERE
    }

    /**
     * The current status of a teleport request.
     */
    public enum Status {
        /**
         * The request is currently pending and can be accepted or declined.
         */
        PENDING,
        /**
         * The teleport request has been accepted by the recipient and teleportation will commence.
         */
        ACCEPTED,
        /**
         * The recipient has declined the teleport request.
         */
        DECLINED,
        /**
         * The recipient was ignoring teleport requests at the time it was sent;
         * the request has been automatically declined.
         */
        IGNORED
    }

}
