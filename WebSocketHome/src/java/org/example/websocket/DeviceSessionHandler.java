/**
 *   Copyright 2016 Royce Remulla
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.example.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;

import org.example.model.Device;

/**
 * @author royce
 */
@ApplicationScoped
public class DeviceSessionHandler {


    private int deviceId = 0;

    private final Set<Session> sessions = new HashSet<>();
    private final Set<Device> devices = new HashSet<>();


    public void addSession(final Session session) {
        sessions.add(session);

        for (final Device device : devices) {
            final JsonObject addMessage = createAddMessage(device);
            sendToSession(session, addMessage);
        }
    }

    public void removeSession(final Session session) {
        sessions.remove(session);
    }

    /**
     * Retrieve the list of devices and their attributes.
     *
     * @return
     */
    public List<Device> getDevices() {
        return new ArrayList<>(devices);
    }

    /**
     * Add a device to the application.
     *
     * @param device
     */
    public void addDevice(final Device device) {
        device.setId(deviceId);
        devices.add(device);
        deviceId++;
        final JsonObject addMessage = createAddMessage(device);
        sendToAllConnectedSessions(addMessage);
    }

    /**
     * Remove a device from the application.
     *
     * @param id
     */
    public void removeDevice(final int id) {
        final Device device = getDeviceById(id);
        if (device != null) {
            devices.remove(device);
            final JsonProvider provider = JsonProvider.provider();
            final JsonObject removeMessage = provider
                .createObjectBuilder()
                .add("action", "remove")
                .add("id", id)
                .build();
            sendToAllConnectedSessions(removeMessage);
        }
    }

    /**
     * Toggle the device status.
     *
     * @param id
     */
    public void toggleDevice(final int id) {
        final JsonProvider provider = JsonProvider.provider();
        final Device device = getDeviceById(id);
        if (device != null) {
            if ("On".equals(device.getStatus())) {
                device.setStatus("Off");
            } else {
                device.setStatus("On");
            }
            final JsonObject updateDevMessage = provider
                .createObjectBuilder()
                .add("action", "toggle")
                .add("id", device.getId())
                .add("status", device.getStatus())
                .build();
            sendToAllConnectedSessions(updateDevMessage);
        }
    }

    /**
     * Retrieve a device with a specific identifier.
     *
     * @param id
     * @return
     */
    private Device getDeviceById(final int id) {
        for (final Device device : devices) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }

    /**
     * Build a JSON message for adding a device to the application.
     *
     * @param device
     * @return
     */
    private JsonObject createAddMessage(final Device device) {
        final JsonProvider provider = JsonProvider.provider();
        final JsonObject addMessage = provider
            .createObjectBuilder()
            .add("action", "add")
            .add("id", device.getId())
            .add("name", device.getName())
            .add("type", device.getType())
            .add("status", device.getStatus())
            .add("description", device.getDescription())
            .build();
        return addMessage;
    }

    /**
     * Send an event message to all connected clients.
     *
     * @param message
     */
    private void sendToAllConnectedSessions(final JsonObject message) {
        for (final Session session : sessions) {
            sendToSession(session, message);
        }
    }

    /**
     * Send an event message to a client.
     *
     * @param session
     * @param message
     */
    private void sendToSession(final Session session,
                               final JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (final IOException ex) {
            sessions.remove(session);
            Logger.getLogger(DeviceSessionHandler.class.getName()).log(
                Level.SEVERE,
                null,
                ex);
        }
    }

}
