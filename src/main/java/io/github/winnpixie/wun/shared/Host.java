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

public class Host {
    private static final int PACKET_MAX_SIZE = 1500 - 8 - 20; // mtu(1500) - udp_header(8) - ip_header(20)
    private static final int PACKET_HEADER_SIZE = 4 + 1 + 4; //  peer_id(4) + packet_id(1) + payload_length(4)
    private static final int PACKET_MAX_PAYLOAD_SIZE = PACKET_MAX_SIZE - PACKET_HEADER_SIZE;

    private final Map<Byte, PacketDeserializer<? extends Packet>> idToPacket = new HashMap<>();
    private final Map<Class<? extends Packet>, Byte> packetToId = new HashMap<>();
    private final Map<Byte, BiConsumer<Peer, Packet>> packetHandler = new HashMap<>();
    private final Deque<QueuedPacket> packetWriteQueue = new ConcurrentLinkedDeque<>();

    private final DatagramChannel channel;
    private final Selector selector;

    private boolean processing;
    private InetSocketAddress address;

    private int peerId = -1;

    /**
     * Configures this {@link Host} to run in server mode.
     *
     * @param port
     * @throws IOException
     */
    public Host(int port) throws IOException {
        this();

        channel.bind(new InetSocketAddress(port));
    }

    /**
     * Configures this {@link Host} to run in client mode.
     *
     * @param address
     * @throws IOException
     */
    public Host(InetSocketAddress address) throws IOException {
        this();

        this.address = address;

        channel.connect(this.address);
    }

    private Host() throws IOException {
        this.channel = DatagramChannel.open();
        this.selector = Selector.open();

        channel.configureBlocking(false);
        channel.register(selector, channel.validOps());
    }

    public boolean isProcessing() {
        return processing;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public <T extends Packet> void registerPacket(byte id, Class<T> cls, PacketDeserializer<T> supplier, BiConsumer<Peer, T> handler) {
        idToPacket.put(id, supplier);
        packetToId.put(cls, id);
        packetHandler.put(id, (BiConsumer<Peer, Packet>) handler);
    }

    public <T extends Packet> void sendPacket(T packet) {
        sendPacket(packet, address);
    }

    public <T extends Packet> void sendPacket(T packet, InetSocketAddress... recipients) {
        for (InetSocketAddress recipient : recipients) {
            packetWriteQueue.add(new QueuedPacket(packet, recipient));
        }
    }

    public void process() throws IOException {
        this.processing = true;

        while (processing) {
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

        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException {
        DatagramChannel keyChannel = (DatagramChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);
        buffer.clear();

        SocketAddress addressIn = keyChannel.receive(buffer);
        if (addressIn == null) {
            return;
        }

        buffer.flip();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer.array()))) {
            int peerIdIn = input.readInt();
            byte packetIdIn = input.readByte();
            int length = input.readInt();
            if (length > PACKET_MAX_PAYLOAD_SIZE) {
                // TODO: Implement packet defragmentation
                return;
            }

            PacketDeserializer<? extends Packet> deserializer = idToPacket.get(packetIdIn);
            if (deserializer == null) {
                // unknown packet ???
                processing = false;
            } else {
                Packet packetObj = deserializer.deserialize(input);
                packetHandler.get(packetIdIn).accept(new Peer(peerIdIn, (InetSocketAddress) addressIn), packetObj);
            }
        }
    }

    private void write(SelectionKey key) throws IOException {
        if (packetWriteQueue.isEmpty()) {
            return;
        }

        DatagramChannel keyChannel = (DatagramChannel) key.channel();

        QueuedPacket queued = packetWriteQueue.remove();
        Packet packet = queued.packet();

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);
        buffer.clear();

        // HEADER
        buffer.putInt(peerId);
        buffer.put(packetToId.get(packet.getClass()));

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
        }
    }

    public void stop() {
        processing = false;
    }
}
