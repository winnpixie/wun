package io.github.winnpixie.wun.program.server;

import io.github.winnpixie.wun.shared.Host;
import io.github.winnpixie.wun.shared.packets.client.ClientDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientHeartbeatPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientIdentityPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientMessagePacket;
import io.github.winnpixie.wun.shared.packets.server.ServerConfigurationPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerMessagePacket;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Map<SocketAddress, Connection> connections = new ConcurrentHashMap<>();
    private final int heartBeatTimeout = 30000;

    private final Host host;

    public Server(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    public Map<SocketAddress, Connection> getConnections() {
        return connections;
    }

    public int getHeartBeatTimeout() {
        return heartBeatTimeout;
    }

    public void start() throws IOException {
        host.bind(42069);
        System.out.printf("Listening on %s", host.getChannel().getLocalAddress());

        ServerPacketHandler packetHandler = new ServerPacketHandler(this);
        host.registerPacket((byte) 0x1, ClientIdentityPacket.class,
                ClientIdentityPacket::new, packetHandler::handleIdentity);
        host.registerPacket((byte) 0x2, ServerConfigurationPacket.class,
                null, null);
        host.registerPacket((byte) 0x3, ClientHeartbeatPacket.class,
                ClientHeartbeatPacket::new, packetHandler::handleHeartbeat);
        host.registerPacket((byte) 0x4, ServerMessagePacket.class,
                null, null);
        host.registerPacket((byte) 0x5, ClientMessagePacket.class,
                ClientMessagePacket::new, packetHandler::handleMessage);
        host.registerPacket((byte) 0x6, ServerDisconnectPacket.class,
                null, null);
        host.registerPacket((byte) 0x7, ClientDisconnectPacket.class,
                ClientDisconnectPacket::new, packetHandler::handleDisconnect);

        Thread networkThread = new Thread(() -> {
            try {
                host.process();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        networkThread.start();

        while (networkThread.isAlive()) {
            List<SocketAddress> expired = new ArrayList<>();
            connections.forEach((source, connection) -> {
                long elapsed = System.currentTimeMillis() - connection.getHeartBeat();
                if (elapsed > heartBeatTimeout) {
                    host.send(new ServerDisconnectPacket("Poor heart-rate."), source);
                    expired.add(source);

                    System.out.printf("TMOT: %s%n", source);
                }
            });

            expired.forEach(connections::remove);
        }
    }
}
