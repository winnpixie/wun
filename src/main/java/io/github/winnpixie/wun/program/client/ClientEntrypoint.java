package io.github.winnpixie.wun.program.client;

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
import java.util.Scanner;

public class ClientEntrypoint {
    private static Host host;

    private ClientEntrypoint() {
    }

    public static void run() {
        InetSocketAddress target = new InetSocketAddress("localhost", 42069);
        try {
            Host host = new Host(target);
            ClientEntrypoint.host = host;

            host.registerPacket((byte) 0x1, ClientConnectPacket.class,
                    null, null);
            host.registerPacket((byte) 0x2, ServerConfigurePeerPacket.class,
                    ServerConfigurePeerPacket::new, ClientEntrypoint::handleConfigureConnection);
            host.registerPacket((byte) 0x3, ClientHeartbeatPacket.class,
                    null, null);
            host.registerPacket((byte) 0x4, ServerMessagePacket.class,
                    ServerMessagePacket::new, ClientEntrypoint::handleMessage);
            host.registerPacket((byte) 0x5, ClientMessagePacket.class,
                    null, null);
            host.registerPacket((byte) 0x6, ServerDisconnectPacket.class,
                    ServerDisconnectPacket::new, ClientEntrypoint::handleDisconnect);
            host.registerPacket((byte) 0x7, ClientDisconnectPacket.class,
                    null, null);

            Thread networkThread = new Thread(() -> {
                try {
                    host.process();
                } catch (IOException e) {
                    host.stop();

                    e.printStackTrace();
                }
            });
            networkThread.start();

            host.sendPacket(new ClientConnectPacket());

            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (line.equalsIgnoreCase("/quit")) {
                        host.sendPacket(new ClientDisconnectPacket());
                        host.stop();

                        break;
                    } else {
                        host.sendPacket(new ClientMessagePacket(line));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleConfigureConnection(Peer source, ServerConfigurePeerPacket packet) {
        int id = packet.getNewPeerId();
        host.setPeerId(id);

        new Thread(() -> {
            long lastBeat = System.currentTimeMillis();

            while (host.isProcessing()) {
                if (System.currentTimeMillis() - lastBeat > packet.getHeartBeatInterval()) {
                    host.sendPacket(new ClientHeartbeatPacket());

                    lastBeat = System.currentTimeMillis();
                }
            }
        }).start();

        System.out.printf("Peer Id set to %d%n", id);
    }

    private static void handleMessage(Peer source, ServerMessagePacket packet) {
        System.out.println(packet.getMessage());
    }

    private static void handleDisconnect(Peer source, ServerDisconnectPacket packet) {
        host.stop();

        System.out.println("DISC: %s".formatted(packet.getReason()));
    }
}
