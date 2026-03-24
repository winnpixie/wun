package io.github.winnpixie.wun.server;

import io.github.winnpixie.wun.client.ClientConnectPacket;
import io.github.winnpixie.wun.client.ClientDisconnectPacket;
import io.github.winnpixie.wun.client.ClientHeartbeatPacket;
import io.github.winnpixie.wun.client.ClientMessagePacket;
import io.github.winnpixie.wun.shared.NetworkManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerEntrypoint {
    private static final AtomicLong peerId = new AtomicLong(1);
    private static final Map<Long, Peer> peers = new ConcurrentHashMap<>();
    private static final long heartBeatTimeout = 15000;

    private static NetworkManager networkManager;
    private static Thread receiveThread;

    public static void run() {
        try {
            NetworkManager networkManager = new NetworkManager(42069);
            ServerEntrypoint.networkManager = networkManager;

            networkManager.registerPacket((byte) 0x1, ClientConnectPacket.class,
                    ClientConnectPacket::deserialize, ServerEntrypoint::handleConnect);
            networkManager.registerPacket((byte) 0x2, ServerConfigurePeerPacket.class,
                    null, null);
            networkManager.registerPacket((byte) 0x3, ClientHeartbeatPacket.class,
                    ClientHeartbeatPacket::deserialize, ServerEntrypoint::handleHeartbeat);
            networkManager.registerPacket((byte) 0x4, ServerMessagePacket.class,
                    null, null);
            networkManager.registerPacket((byte) 0x5, ClientMessagePacket.class,
                    ClientMessagePacket::deserialize, ServerEntrypoint::handleMessage);
            networkManager.registerPacket((byte) 0x6, ServerDisconnectPacket.class,
                    null, null);
            networkManager.registerPacket((byte) 0x7, ClientDisconnectPacket.class,
                    ClientDisconnectPacket::deserialize, ServerEntrypoint::handleDisconnect);

            networkManager.setPeerId(0);

            receiveThread = new Thread(() -> {
                try {
                    networkManager.listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            while (receiveThread.isAlive()) {
                List<Long> expired = new ArrayList<>();
                peers.forEach((id, peer) -> {
                    long elapsed = System.currentTimeMillis() - peer.getHeartBeat();
                    if (elapsed > heartBeatTimeout) {
                        networkManager.sendPacket(new ServerDisconnectPacket(), peer.getAddress());
                        System.out.printf("TMOT: #%d%n", id);
                        expired.add(id);
                    }
                });

                expired.forEach(peers::remove);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleConnect(InetSocketAddress source, ClientConnectPacket packet) {
        long newPeerId = ServerEntrypoint.peerId.getAndIncrement();
        peers.put(newPeerId, new Peer(newPeerId, source));
        networkManager.sendPacket(new ServerConfigurePeerPacket(5000, newPeerId), source);

        System.out.printf("CONN: %s:%d [#%d]%n", source.getHostString(), source.getPort(), newPeerId);
    }

    private static void handleHeartbeat(InetSocketAddress source, ClientHeartbeatPacket packet) {
        peers.get(packet.getPeerId()).setHeartBeat(System.currentTimeMillis());

        System.out.printf("BEAT: #%d%n", packet.getPeerId());
    }

    private static void handleMessage(InetSocketAddress source, ClientMessagePacket packet) {
        String message = packet.getMessage();
        ServerMessagePacket outbound = new ServerMessagePacket("[#%d] %s".formatted(packet.getPeerId(), message));
        peers.forEach((id, peer) -> {
            if (peer.getId() == packet.getPeerId()) {
                return;
            }

            networkManager.sendPacket(outbound, peer.getAddress());
        });

        System.out.printf("MESG: [#%d] %s%n", packet.getPeerId(), message);
    }

    private static void handleDisconnect(InetSocketAddress source, ClientDisconnectPacket packet) {
        peers.remove(packet.getPeerId());

        System.out.printf("DISC: #%d%n", packet.getPeerId());
    }
}
