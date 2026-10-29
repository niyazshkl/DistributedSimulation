package shared;

import java.nio.ByteBuffer;

public abstract class NIOUtilities {
    public static String byteBufferToString(ByteBuffer byteBuffer){
        int len = byteBuffer.limit() - byteBuffer.position();
        return new String(byteBuffer.array(), byteBuffer.position(), len);
    }
    public static ByteBuffer stringToByteBuffer(String givenString){
        return ByteBuffer.wrap(givenString.getBytes());
    }
    public final static String CLIENT_HOST_NAME = "localhost";
}