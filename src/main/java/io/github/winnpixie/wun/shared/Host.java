package io.github.winnpixie.wun.shared;

import java.io.IOException;
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
    private static final int PACKET_HEADER_SIZE = 1 + 4; // packet_id(1) + payload_length(4)
    private static final int PACKET_MAX_PAYLOAD_SIZE = PACKET_MAX_SIZE - PACKET_HEADER_SIZE;

    private final Map<Byte, PacketDeserializer<? extends Packet>> idToPacket = new HashMap<>();
    private final Map<Class<? extends Packet>, Byte> packetToId = new HashMap<>();

    private final Map<Byte, BiConsumer<SocketAddress, ? extends Packet>> handlers = new HashMap<>();
    private final Deque<QueuedPacket> queue = new ConcurrentLinkedDeque<>();

    private final DatagramChannel channel;
    private final Selector selector;

    private boolean processing;
    private SocketAddress remote;

    public Host() throws IOException {
        this.channel = DatagramChannel.open();
        this.selector = Selector.open();

        channel.configureBlocking(false);
        channel.register(selector, channel.validOps());
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void bind(int port) throws IOException {
        channel.bind(new InetSocketAddress(port));
    }

    public void connect(SocketAddress remote) throws IOException {
        this.remote = remote;
        channel.connect(remote);
    }

    public <T extends Packet> void registerPacket(byte id, Class<T> cls, PacketDeserializer<T> supplier, BiConsumer<SocketAddress, T> handler) {
        idToPacket.put(id, supplier);
        packetToId.put(cls, id);
        handlers.put(id, handler);
    }

    public <T extends Packet> void send(T packet) {
        send(packet, remote);
    }

    public <T extends Packet> void send(T packet, SocketAddress... recipients) {
        for (SocketAddress recipient : recipients) {
            queue.add(new QueuedPacket(packet, recipient));
        }
    }

    public <T extends Packet> void sendNow(T packet, SocketAddress... recipients) throws IOException {
        sendNow(packet, channel, recipients);
    }

    public <T extends Packet> void sendNow(T packet, DatagramChannel channel, SocketAddress... recipients) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(PACKET_MAX_SIZE);

        // HEADER
        buffer.put(packetToId.get(packet.getClass()));

        // TODO: Implement packet fragmentation
        ByteBuffer payloadBuffer = ByteBuffer.allocate(PACKET_MAX_PAYLOAD_SIZE);
        packet.serialize(payloadBuffer);
        payloadBuffer.flip();

        // HEADER
        buffer.putInt(payloadBuffer.remaining());

        // CONTENT
        buffer.put(payloadBuffer);

        for (SocketAddress recipient : recipients) {
            buffer.flip();
            channel.send(buffer, recipient);
        }
    }

    public void process() throws IOException {
        this.processing = true;

        while (processing) {
            int queue = selector.selectNow();
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

        byte packetIdIn = buffer.get();
        int lengthIn = buffer.getInt();
        if (lengthIn > PACKET_MAX_PAYLOAD_SIZE) {
            // TODO: Implement packet defragmentation
            return;
        }

        PacketDeserializer<? extends Packet> deserializer = idToPacket.get(packetIdIn);
        if (deserializer == null) {
            // unknown packet ???
            processing = false;
        } else {
            Packet packetObj = deserializer.deserialize(buffer);
            ((BiConsumer<SocketAddress, Packet>) handlers.get(packetIdIn))
                    .accept(addressIn, packetObj);
        }
    }

    private void write(SelectionKey key) throws IOException {
        if (queue.isEmpty()) {
            return;
        }

        QueuedPacket queued = queue.remove();
        sendNow(queued.packet(), (DatagramChannel) key.channel(), queued.recipient());
    }

    public void flush() throws IOException {
        while (!queue.isEmpty()) {
            QueuedPacket queued = queue.remove();
            sendNow(queued.packet(), queued.recipient());
        }
    }

    public void stop() {
        processing = false;

        queue.clear();
    }
}
