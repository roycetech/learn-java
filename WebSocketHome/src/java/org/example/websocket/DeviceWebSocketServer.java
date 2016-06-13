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

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.example.model.Device;

/**
 *
 * @author royce
 */
@ApplicationScoped
@ServerEndpoint("/actions")
public class DeviceWebSocketServer {


    @Inject
    private DeviceSessionHandler sessionHandler;


    @OnOpen
    public void open(final Session session) {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(final Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(final Throwable error) {
        Logger.getLogger(DeviceWebSocketServer.class.getName()).log(
            Level.SEVERE,
            null,
            error);
    }

    @OnMessage
    public void handleMessage(final String message, final Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            final JsonObject jsonMessage = reader.readObject();

            if ("add".equals(jsonMessage.getString("action"))) {
                final Device device = new Device();
                device.setName(jsonMessage.getString("name"));
                device.setDescription(jsonMessage.getString("description"));
                device.setType(jsonMessage.getString("type"));
                device.setStatus("Off");
                sessionHandler.addDevice(device);
            }

            if ("remove".equals(jsonMessage.getString("action"))) {
                final int id = jsonMessage.getInt("id");
                sessionHandler.removeDevice(id);
            }

            if ("toggle".equals(jsonMessage.getString("action"))) {
                final int id = jsonMessage.getInt("id");
                sessionHandler.toggleDevice(id);
            }
        }
    }

}
