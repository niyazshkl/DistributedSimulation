package starterExample;

import inputport.nio.manager.NIOManager;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.listeners.SocketChannelConnectListener;
import inputport.nio.manager.listeners.SocketChannelWriteListener;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class PrintClient implements SocketChannelConnectListener, SocketChannelWriteListener {
    protected NIOManager nioManager = NIOManagerFactory.getSingleton();
    protected SocketChannel socketChannel;
    protected Scanner scanner;

    public PrintClient(int aServerPort) {
        this.scanner = new Scanner(System.in);
        this.setTracing();
        this.initialize(aServerPort);
    }

    public void processInput() {
        this.scanner = new Scanner(System.in);

        while(true) {
            System.out.println("Enter a line of input to be printed remotely");
            String aNextLine = this.scanner.nextLine();
//            ByteBuffer aWriteMessage = ByteBuffer.wrap(aNextLine.getBytes());
            ByteBuffer aWriteMessage = stringToByteBuffer(aNextLine);

            this.nioManager.write(this.socketChannel, aWriteMessage, new SocketChannelWriteListener[]{this});
        }
    }

    private ByteBuffer stringToByteBuffer(String givenString){
        return ByteBuffer.wrap(givenString.getBytes());
    }

    protected void setTracing() {
        FactoryTraceUtility.setTracing();
        NIOTraceUtility.setTracing();
    }

    protected void initialize(int aServerPort) {
        try {
            this.socketChannel = SocketChannel.open();
            InetAddress aServerAddress = InetAddress.getByName("localhost");
            this.nioManager.connect(this.socketChannel, aServerAddress, aServerPort, 0, new SocketChannelConnectListener[]{this});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void connected(SocketChannel aSocketChannel) {
        /*
        The selector thread in NIOManager calls into this
        We should not block/take too long so that other threads waiting for NIOManager are not blocked
        We therefore ask slector thread to delegate work into another thead to continue work the client wants to do
        Happens only once, so we can do the creation here without having to worry about overwriting memory
        */
        Thread delegatedThread = new Thread(()->{
           processInput();
        });
        delegatedThread.start();
    }
    @Override
    public void notConnected(SocketChannel theSocketChannel, Exception e) {
        e.printStackTrace();
    }
    @Override
    public void written(SocketChannel socketChannel, ByteBuffer aWriteBuffer, int sendId) {
/*
        I think NIOManager does this internally:
        aWriteBuffer.flip(); called after writing into buffer to allow byteBUffer reuse in both read/write
        Then idk what else to put here I think nothing?
 */
    }
}
