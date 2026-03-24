package io.github.winnpixie.wun.shared;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;

public class NetworkManager {
    private static final int PACKET_MAX_SIZE = 1500 - 8 - 20; // mtu(1500) - udp_header(8) - ip_header(20)
    private static final int PACKET_HEADER_SIZE = 1 + 8 + 4; // packet_id(1) + peer_id(8) + payload_length(4)
    private static final int PACKET_MAX_PAYLOAD_SIZE = PACKET_MAX_SIZE - PACKET_HEADER_SIZE;

    private final Map<Byte, PacketHandler<? extends Packet>> idToPacket = new HashMap<>();
    private final Map<Class<? extends Packet>, Byte> packetToId = new HashMap<>();
    private final Map<Byte, BiConsumer<InetSocketAddress, Packet>> packetHandler = new HashMap<>();
    private final Deque<QueuedPacket> packetWriteQueue = new ConcurrentLinkedDeque<>();

    private final DatagramChannel channel;
    private final Selector selector;

    private boolean listening;
    private InetSocketAddress host;

    private long peerId = -1L;

    /**
     * Configures this {@link NetworkManager} to run in server mode.
     *
     * @param port
     * @throws IOException
     */
    public NetworkManager(int port) throws IOException {
        this();

        channel.bind(new InetSocketAddress(port));
    }

    /**
     * Configures this {@link NetworkManager} to run in client mode.
     *
     * @param address
     * @throws IOException
     */
    public NetworkManager(InetSocketAddress address) throws IOException {
        this();

        this.host = address;

        channel.connect(host);
    }

    private NetworkManager() throws IOException {
        this.channel = DatagramChannel.open();
        this.selector = Selector.open();

        channel.configureBlocking(false);
        channel.register(selector, channel.validOps());
    }

    public long getPeerId() {
        return peerId;
    }

    public void setPeerId(long peerId) {
        this.peerId = peerId;
    }

    public <T extends Packet> void registerPacket(byte id, Class<T> cls, PacketHandler<T> supplier, BiConsumer<InetSocketAddress, T> handler) {
        idToPacket.put(id, supplier);
        packetToId.put(cls, id);
        packetHandler.put(id, (BiConsumer<InetSocketAddress, Packet>) handler);
    }

    public <T extends Packet> void sendPacket(T packet) {
        sendPacket(packet, host);
    }

    public <T extends Packet> void sendPacket(T packet, InetSocketAddress... recipients) {
        for (InetSocketAddress recipient : recipients) {
            packetWriteQueue.add(new QueuedPacket(packet, recipient));
        }
    }

    public void listen() throws IOException {
        this.listening = true;

        while (listening) {
            int queue = selector.select();
            if (queue < 1) {
                continue;
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();

                if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }

                selectedKeys.remove();
            }
        }
    }

    private void read(SelectionKey key) {
        try {
            DatagramChannel keyChannel = (DatagramChannel) key.channel();

            ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);
            buffer.clear();
            SocketAddress address = keyChannel.receive(buffer);
            if (address == null) {
                return;
            }

            buffer.flip();
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer.array()))) {
                byte packetIdIn = input.readByte();
                long peerIdIn = input.readLong();
                int length = input.readInt();
                if (length > PACKET_MAX_PAYLOAD_SIZE) {
                    // TODO: Implement packet defragmentation
                    return;
                }

                PacketHandler<? extends Packet> deserializer = idToPacket.get(packetIdIn);
                if (deserializer == null) {
                    // unknown packet ???
                    listening = false;
                } else {
                    Packet packetObj = deserializer.process(input);
                    packetObj.setPeerId(peerIdIn);
                    packetHandler.get(packetIdIn).accept((InetSocketAddress) address, packetObj);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey key) {
        if (packetWriteQueue.isEmpty()) {
            return;
        }

        DatagramChannel keyChannel = (DatagramChannel) key.channel();

        QueuedPacket queued = packetWriteQueue.remove();
        Packet packet = queued.packet();

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);
        buffer.clear();

        // HEADER
        buffer.put(packetToId.get(packet.getClass()));
        buffer.putLong(peerId);

        try (ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
             DataOutputStream stream = new DataOutputStream(payloadStream)) {
            packet.serialize(stream);

            byte[] payload = payloadStream.toByteArray();
            if (payload.length > PACKET_MAX_PAYLOAD_SIZE) {
                // TODO: Implement fragmentation
                return;
            }

            // HEADER
            buffer.putInt(payload.length);

            // CONTENT
            buffer.put(payload);

            buffer.flip();
            keyChannel.send(buffer, queued.recipient());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        listening = false;

        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
