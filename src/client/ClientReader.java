package client;

import server.KeyValue;
import server.Reader;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.trickOrTreat.LocalCommandObserved;

import java.util.concurrent.BlockingQueue;

@Tags({DistributedTags.CLIENT_READ_THREAD, DistributedTags.NIO})
public class ClientReader implements Reader {
    Client client;
    private BlockingQueue<String> commands;

    public ClientReader(Client client, BlockingQueue<String> commands) {
        this.client = client;
        this.commands = commands;
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
                String command = this.commands.take(); //blocking until non empty
                LocalCommandObserved.newCase(this, command);
                this.client.processSimulationCommand(command);
            } catch (InterruptedException e) {
                System.exit(-1);
            }
        }
    }
}

