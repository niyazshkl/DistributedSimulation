package starterExample;

import java.nio.ByteBuffer;

public abstract class NiyazNIOUtilities {
    static String byteBufferToString(ByteBuffer byteBuffer, int length){
        return new String(byteBuffer.array(), byteBuffer.position(), length);
    }
    static ByteBuffer stringToByteBuffer(String givenString){
        return ByteBuffer.wrap(givenString.getBytes());
    }
    protected final static String CLIENT_HOST_NAME = "localhost";
}
