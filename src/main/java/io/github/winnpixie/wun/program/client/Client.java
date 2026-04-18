package io.github.winnpixie.wun.program.client;

import io.github.winnpixie.wun.shared.Host;
import io.github.winnpixie.wun.shared.packets.client.ClientDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientHeartbeatPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientIdentityPacket;
import io.github.winnpixie.wun.shared.packets.client.ClientMessagePacket;
import io.github.winnpixie.wun.shared.packets.server.ServerConfigurationPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerDisconnectPacket;
import io.github.winnpixie.wun.shared.packets.server.ServerMessagePacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
    private final Host host;

    private String userName = "Guest";

    public Client(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    public String getUserName() {
        return userName;
    }

    public void join() throws IOException {
        host.connect(new InetSocketAddress("localhost", 42069));

        ClientPacketHandler packetHandler = new ClientPacketHandler(this);
        host.registerPacket((byte) 0x1, ClientIdentityPacket.class,
                null, null);
        host.registerPacket((byte) 0x2, ServerConfigurationPacket.class,
                ServerConfigurationPacket::new, packetHandler::handleConfigureConnection);
        host.registerPacket((byte) 0x3, ClientHeartbeatPacket.class,
                null, null);
        host.registerPacket((byte) 0x4, ServerMessagePacket.class,
                ServerMessagePacket::new, packetHandler::handleMessage);
        host.registerPacket((byte) 0x5, ClientMessagePacket.class,
                null, null);
        host.registerPacket((byte) 0x6, ServerDisconnectPacket.class,
                ServerDisconnectPacket::new, packetHandler::handleDisconnect);
        host.registerPacket((byte) 0x7, ClientDisconnectPacket.class,
                null, null);

        Thread networkThread = new Thread(() -> {
            try {
                host.process();
            } catch (IOException ioe) {
                host.stop();

                ioe.printStackTrace();
            }
        });
        networkThread.start();

        host.send(new ClientIdentityPacket(userName));

        try (Scanner scanner = new Scanner(System.in)) {
            while (host.isProcessing() && scanner.hasNextLine()) {
                handleInput(scanner.nextLine());
            }
        }
    }

    private void handleInput(String message) throws IOException {
        if (handleCommand(message)) {
            return;
        }

        host.send(new ClientMessagePacket(message));
    }

    private boolean handleCommand(String message) throws IOException {
        if (message.indexOf('/') != 0
                || message.length() < 2) {
            return false;
        }

        String[] tokens = message.substring(1).split(" ");
        String command = tokens[0];

        String[] arguments = new String[tokens.length - 1];
        if (tokens.length > 1) {
            System.arraycopy(tokens, 1, arguments, 0, arguments.length);
        }

        if (command.equalsIgnoreCase("name")) {
            this.userName = arguments[0];
            host.send(new ClientIdentityPacket(userName));
        } else if (command.equalsIgnoreCase("quit")) {
            host.send(new ClientDisconnectPacket());
            host.flush();

            host.stop();
        } else if (command.equalsIgnoreCase("say")) {
            host.send(new ClientMessagePacket(String.join(" ", arguments)));
        }

        return true;
    }
}
