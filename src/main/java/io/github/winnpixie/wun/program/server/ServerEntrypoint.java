package io.github.winnpixie.wun.program.server;

import io.github.winnpixie.wun.shared.Host;
import io.github.winnpixie.wun.shared.Peer;
import io.github.winnpixie.wun.shared.packets.client.ClientConnectPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientHeartbeatPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientMessagePacket;
import io.github.winnpixie.wun.shared.packets.server.ServerConfigurePeerPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerMessagePacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerEntrypoint {
    private static final AtomicInteger peerId = new AtomicInteger(1);
    private static final Map<Integer, Connection> peers = new ConcurrentHashMap<>();
    private static final long heartBeatTimeout = 15000;

    private static Host host;

    private ServerEntrypoint() {
    }

    public static void run() {
        try {
            Host host = new Host(42069);
            ServerEntrypoint.host = host;

            host.registerPacket((byte) 0x1, ClientConnectPacket.class,
                    ClientConnectPacket::new, ServerEntrypoint::handleConnect);
            host.registerPacket((byte) 0x2, ServerConfigurePeerPacket.class,
                    null, null);
            host.registerPacket((byte) 0x3, ClientHeartbeatPacket.class,
                    ClientHeartbeatPacket::new, ServerEntrypoint::handleHeartbeat);
            host.registerPacket((byte) 0x4, ServerMessagePacket.class,
                    null, null);
            host.registerPacket((byte) 0x5, ClientMessagePacket.class,
                    ClientMessagePacket::new, ServerEntrypoint::handleMessage);
            host.registerPacket((byte) 0x6, ServerDisconnectPacket.class,
                    null, null);
            host.registerPacket((byte) 0x7, ClientDisconnectPacket.class,
                    ClientDisconnectPacket::new, ServerEntrypoint::handleDisconnect);

            host.setPeerId(0);

            Thread networkThread = new Thread(() -> {
                try {
                    host.process();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            networkThread.start();

            while (networkThread.isAlive()) {
                List<Integer> expired = new ArrayList<>();
                peers.forEach((id, connection) -> {
                    long elapsed = System.currentTimeMillis() - connection.getHeartBeat();
                    if (elapsed > heartBeatTimeout) {
                        host.sendPacket(new ServerDisconnectPacket("Poor heart-rate."), connection.getPeer().address());
                        expired.add(id);

                        System.out.printf("TMOT: #%d%n", id);
                    }
                });

                expired.forEach(peers::remove);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleConnect(Peer source, ClientConnectPacket packet) {
        int newPeerId = ServerEntrypoint.peerId.getAndIncrement();
        peers.put(newPeerId, new Connection(source));

        InetSocketAddress address = source.address();
        host.sendPacket(new ServerConfigurePeerPacket(newPeerId, 5000), address);

        System.out.printf("CONN: %s:%d [#%d]%n", address.getHostString(), address.getPort(), newPeerId);
    }

    private static void handleHeartbeat(Peer source, ClientHeartbeatPacket packet) {
        peers.get(source.id()).setHeartBeat(System.currentTimeMillis());

        System.out.printf("BEAT: #%d%n", source.id());
    }

    private static void handleMessage(Peer source, ClientMessagePacket packet) {
        String message = packet.getMessage();
        if (message.equalsIgnoreCase("/close")) {
            host.sendPacket(new ServerDisconnectPacket("Requested by user."), source.address());
            peers.remove(source.id());

            System.out.printf("REMV: %d%n", source.id());
            return;
        }

        if (message.equalsIgnoreCase("/stop")) {
            host.stop();
        }

        ServerMessagePacket outbound = new ServerMessagePacket("[#%d] %s".formatted(source.id(), message));
        peers.forEach((id, connection) -> {
            if (id == source.id()) {
                return;
            }

            host.sendPacket(outbound, connection.getPeer().address());
        });

        System.out.printf("MESG: [#%d] %s%n", source.id(), message);
    }

    private static void handleDisconnect(Peer source, ClientDisconnectPacket packet) {
        peers.remove(source.id());

        System.out.printf("DISC: #%d%n", source.id());
    }
}
