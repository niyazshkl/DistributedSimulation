package starterExample;

import java.nio.ByteBuffer;

public abstract class NIOUtilities {
    static String byteBufferToString(ByteBuffer byteBuffer){
        int len = byteBuffer.limit() - byteBuffer.position();
        return new String(byteBuffer.array(), byteBuffer.position(), len);
    }
    static ByteBuffer stringToByteBuffer(String givenString){
        return ByteBuffer.wrap(givenString.getBytes());
    }
    protected final static String CLIENT_HOST_NAME = "localhost";
}
