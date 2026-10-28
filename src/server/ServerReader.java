package server;

import util.annotations.Tags;
import util.tags.DistributedTags;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

@Tags({DistributedTags.SERVER_READ_THREAD, DistributedTags.NIO})
public class ServerReader implements Reader{
    Server server;
    private BlockingQueue<KeyValue<SocketAddress, ByteBuffer>> blockingQueue;

    public ServerReader(Server server, BlockingQueue<KeyValue<SocketAddress, ByteBuffer>> blockingQueue) {
        this.server = server;
        this.blockingQueue = blockingQueue;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (true){
            try {
                KeyValue<SocketAddress, ByteBuffer> data = this.blockingQueue.take(); //blocking until non empty

                SocketAddress client = data.getKey();
                ByteBuffer command = data.getValue();

                this.server.nioBroadcast(client, command);
            } catch (InterruptedException e) {
                System.exit(-1);
            }
        }
    }
}
