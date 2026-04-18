package io.github.winnpixie.wun.program.client;

import io.github.winnpixie.wun.shared.packets.client.ClientHeartbeatPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerConfigurationPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerMessagePacket;

import java.net.SocketAddress;

public class ClientPacketHandler {
    private final Client client;

    ClientPacketHandler(Client client) {
        this.client = client;
    }

    void handleConfigureConnection(SocketAddress source, ServerConfigurationPacket packet) {
        Thread heartBeatThread = new Thread(() -> {
            long lastBeat = System.currentTimeMillis();

            while (client.getHost().isProcessing()) {
                if (System.currentTimeMillis() - lastBeat > packet.getHeartBeatInterval() / 2) {
                    client.getHost().send(new ClientHeartbeatPacket());

                    lastBeat = System.currentTimeMillis();
                }
            }
        });
        heartBeatThread.start();

        System.out.printf("BEAT set to %d%n", packet.getHeartBeatInterval());
    }

    void handleMessage(SocketAddress source, ServerMessagePacket packet) {
        System.out.println(packet.getMessage());
    }

    void handleDisconnect(SocketAddress source, ServerDisconnectPacket packet) {
        client.getHost().stop();

        System.out.println("DISC: %s".formatted(packet.getReason()));
    }
}
