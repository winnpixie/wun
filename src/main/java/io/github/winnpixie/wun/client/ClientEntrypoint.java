package io.github.winnpixie.wun.client;

import io.github.winnpixie.wun.server.ServerConfigurePeerPacket;
import io.github.winnpixie.wun.server.ServerDisconnectPacket;
import io.github.winnpixie.wun.server.ServerMessagePacket;
import io.github.winnpixie.wun.shared.NetworkManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class ClientEntrypoint {
    private static NetworkManager networkManager;
    private static Thread receiveThread;

    public static void run() {
        InetSocketAddress target = new InetSocketAddress("localhost", 42069);
        try {
            NetworkManager networkManager = new NetworkManager(target);
            ClientEntrypoint.networkManager = networkManager;

            networkManager.registerPacket((byte) 0x1, ClientConnectPacket.class,
                    null, null);
            networkManager.registerPacket((byte) 0x2, ServerConfigurePeerPacket.class,
                    ServerConfigurePeerPacket::deserialize, ClientEntrypoint::handleConfigureConnection);
            networkManager.registerPacket((byte) 0x3, ClientHeartbeatPacket.class,
                    null, null);
            networkManager.registerPacket((byte) 0x4, ServerMessagePacket.class,
                    ServerMessagePacket::deserialize, ClientEntrypoint::handleMessage);
            networkManager.registerPacket((byte) 0x5, ClientMessagePacket.class,
                    null, null);
            networkManager.registerPacket((byte) 0x6, ServerDisconnectPacket.class,
                    ServerDisconnectPacket::deserialize, ClientEntrypoint::handleDisconnect);
            networkManager.registerPacket((byte) 0x7, ClientDisconnectPacket.class,
                    null, null);

            receiveThread = new Thread(() -> {
                try {
                    networkManager.listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            networkManager.sendPacket(new ClientConnectPacket());

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.equalsIgnoreCase("quit")) {
                    networkManager.sendPacket(new ClientDisconnectPacket());
                    networkManager.stop();

                    try {
                        receiveThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                } else {
                    networkManager.sendPacket(new ClientMessagePacket(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleConfigureConnection(InetSocketAddress source, ServerConfigurePeerPacket packet) {
        long id = packet.getNewPeerId();
        networkManager.setPeerId(id);

        new Thread(() -> {
            long lastBeat = System.currentTimeMillis();

            while (!Thread.interrupted()) {
                if (System.currentTimeMillis() - lastBeat > packet.getHeartBeatInterval()) {
                    networkManager.sendPacket(new ClientHeartbeatPacket());

                    lastBeat = System.currentTimeMillis();
                }
            }
        }).start();

        System.out.printf("Peer Id set to %d%n", id);
    }

    private static void handleMessage(InetSocketAddress source, ServerMessagePacket packet) {
        System.out.println(packet.getMessage());
    }

    private static void handleDisconnect(InetSocketAddress source, ServerDisconnectPacket packet) {
        networkManager.stop();

        System.out.println("DISC: Disconnected by server.");
    }
}
