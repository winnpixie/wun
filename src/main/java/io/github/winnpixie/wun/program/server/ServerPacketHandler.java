package io.github.winnpixie.wun.program.server;

import io.github.winnpixie.wun.shared.packets.client.ClientDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientHeartbeatPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientIdentityPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientMessagePacket;
import io.github.winnpixie.wun.shared.packets.server.ServerConfigurationPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerMessagePacket;

import java.io.IOException;
import java.net.SocketAddress;

public class ServerPacketHandler {
    private final Server server;

    ServerPacketHandler(Server server) {
        this.server = server;
    }

    void handleIdentity(SocketAddress source, ClientIdentityPacket packet) {
        Connection connection = server.getConnections().computeIfAbsent(source, addr -> {
            server.getHost().send(new ServerConfigurationPacket(server.getHeartBeatTimeout()), addr);

            System.out.printf("CONN: %s%n", addr);
            return new Connection(packet.getUserName());
        });

        connection.setUserName(packet.getUserName());

        System.out.printf("IDEN: %s '%s'%n", source, connection.getUserName());
    }

    void handleHeartbeat(SocketAddress source, ClientHeartbeatPacket packet) {
        Connection connection = server.getConnections().get(source);
        if (connection == null) {
            return;
        }

        connection.setHeartBeat(System.currentTimeMillis());

        System.out.printf("BEAT: %s%n", source);
    }

    void handleMessage(SocketAddress source, ClientMessagePacket packet) {
        Connection sender = server.getConnections().get(source);
        String message = packet.getMessage();

        if (message.equalsIgnoreCase("/close")) {
            server.getHost().send(new ServerDisconnectPacket("Requested by user"), source);
            server.getConnections().remove(source);

            System.out.printf("REMV: %s%n", source);
        } else if (message.equalsIgnoreCase("/stop")) {
            server.getConnections().forEach((address, connection) ->
                    server.getHost().send(new ServerDisconnectPacket(String.format("Server stopped by %s", sender.getUserName())), address));

            try {
                server.getHost().flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            server.getHost().stop();
        } else {
            ServerMessagePacket outbound = new ServerMessagePacket(String.format("%s: %s", sender.getUserName(), message));
            server.getConnections().forEach((address, connection) -> {
                if (source.equals(address)) {
                    return;
                }

                server.getHost().send(outbound, address);
            });
        }

        System.out.printf("MESG: [%s] %s%n", source, message);
    }

    void handleDisconnect(SocketAddress source, ClientDisconnectPacket packet) {
        server.getConnections().remove(source);

        System.out.printf("DISC: %s%n", source);
    }
}
