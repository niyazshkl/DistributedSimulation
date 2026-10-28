package starterExample;

import inputport.nio.manager.NIOManager;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.listeners.SocketChannelAcceptListener;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import inputport.nio.manager.listeners.SocketChannelWriteListener;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.nio.SocketChannelBound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class PrintServer extends NIOUtilities implements SocketChannelAcceptListener, SocketChannelReadListener, SocketChannelWriteListener {
    protected NIOManager nioManager = NIOManagerFactory.getSingleton();
    protected Set<SocketChannel> clients = new HashSet<>();
    protected volatile String currentString;
    protected  String senderAddress;
    Thread delegatedThread = new Thread(()->{
        try {
            synchronized (this){
            this.wait();
            while (true){
                echoToEachClient();
                this.wait();
            }
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + "ENDED OPERATIONS!");
            throw new RuntimeException(e);
        }
    });

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
            this.nioManager.enableListenableAccepts(aServerFactoryChannel, SelectionKey.OP_READ, new SocketChannelAcceptListener[]{this});// this already does nioManager.enableReads(resultingSocketChannel)
            delegatedThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static String getSenderAddress(SocketChannel aSocketChannel) {
        try {
            return aSocketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void socketChannelAccepted(ServerSocketChannel aServerSocketChannel, SocketChannel aSocketChannel) {
        /*
        Extension 1: make the server echo the string to each client
        how:
        - Need to have stored the clients somehow (a type of collection -> List/Map/Set: choice is set)
        - need to iterate
            - long -> delegate work to local thread so selector thread doesn't wait: else causes starvation)
         */
        clients.add(aSocketChannel);//will use later the printer wants to echo to each client.

//        this.nioManager.enableReads(aSocketChannel);
/*
ServerSocketChanel (tells that the child will be a reader) -> SocketChannel (connected & reader)
Since we
No longer needed because when nioManager.enableListenableAcccepts(),
it already said that the "listenable" "will enable" and passed in READ_OP
*/

        this.nioManager.addReadListener(aSocketChannel, this);

    }

    private void echoToEachClient(){
        for (SocketChannel socketChannel: clients){
            if (getSenderAddress(socketChannel).equals(this.senderAddress)) continue; //skip the sender
            ByteBuffer converted = stringToByteBuffer(this.currentString);
            this.nioManager.write(socketChannel, converted, this);
        }
    }
    @Override
    public void socketChannelRead(SocketChannel aSocketChannel, ByteBuffer aMessage, int aLength) {
        String aMessageString = byteBufferToString(aMessage);
        this.currentString = aMessageString;
        this.senderAddress = getSenderAddress(aSocketChannel); //aSocketChannel is this (server) endPoint so the sender is client and receiver is server.
        System.out.println(aMessageString + "<--" + aSocketChannel);
        /*
        I think NIOManager does this internally:
        aMessage.clear(); post read; means that aMessage ByteBuffer is ready to be overwritten
        */

        /*
        Extension 1: make the server echo the string to each client
        how:
        - Need to have stored the clients somehow (a type of collection -> List/Map/Set: choice is set)
        - need to iterate
            - long -> delegate work to local thread so selector thread doesn't wait: else causes starvation)
         */
        synchronized (this){
        this.notify(); //delegate thread wakes up to do work.
        }

    }

    @Override
    public void written(SocketChannel socketChannel, ByteBuffer byteBuffer, int i) {
//        byteBuffer.clear(); // done internally by nioManager
        String sentString = byteBufferToString(byteBuffer);
        System.out.println(socketChannel + " {wrote: " + sentString + "} --> (sendId / i ): " + i);
   }
}