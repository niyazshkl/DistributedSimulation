package starterExample;

import inputport.nio.manager.NIOManager;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.listeners.SocketChannelAcceptListener;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.nio.SocketChannelBound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class PrintServer implements SocketChannelAcceptListener, SocketChannelReadListener {
    protected NIOManager nioManager = NIOManagerFactory.getSingleton();

    public PrintServer(int aServerPort) {
        this.setTracing();
        this.initialize(aServerPort);
    }

    protected void setTracing() {
        FactoryTraceUtility.setTracing();
        NIOTraceUtility.setTracing();
    }

    protected void initialize(int aServerPort) {
        try {
            ServerSocketChannel aServerFactoryChannel = ServerSocketChannel.open();
            InetSocketAddress anInternetSocketAddress = new InetSocketAddress(aServerPort);
            aServerFactoryChannel.socket().bind(anInternetSocketAddress);
            SocketChannelBound.newCase(this, aServerFactoryChannel, anInternetSocketAddress);
            this.nioManager.enableListenableAccepts(aServerFactoryChannel, 1, new SocketChannelAcceptListener[]{this});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void socketChannelAccepted(ServerSocketChannel aServerSocketChannel, SocketChannel aSocketChannel) {
        this.nioManager.addReadListener(aSocketChannel, this);
    }
    @Override
    public void socketChannelRead(SocketChannel aSocketChannel, ByteBuffer aMessage, int aLength) {
//        String aMessageString = new String(aMessage.array(), aMessage.position(), aLength);

        String aMessageString = byteBufferToString(aMessage, aLength);

        System.out.println(aMessageString + "<--" + aSocketChannel);

        /*
        I think NIOManager does this internally:
        aMessage.clear(); called after read to allow byteBUffer reuse in both read/write
        */
    }

    private String byteBufferToString(ByteBuffer byteBuffer, int length){
        return new String(byteBuffer.array(), byteBuffer.position(), length);
    }
}