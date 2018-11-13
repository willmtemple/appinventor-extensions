package edu.colorado.playfulcomputation.blockytalky;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;


class BTBLEEncoder{
    public static final int BTBLE_INT_TYPE = 1;
    public static final int BTBLE_FLOAT_TYPE = 2;
    public static final int BTBLE_STRING_TYPE = 3;
    private static final int BTBLE_MAX_PACKET_LEN = 20;

    public static BTMessage DecodeMessage(List<Integer> bytesAsIntegerList){

        //convert the parameter to an array of bytes
        byte[] bytes = new byte[bytesAsIntegerList.size()];
        for (int i = 0; i < bytes.length ; i++ ){
            bytes[i] = bytesAsIntegerList.get(i).byteValue();
        }

        String key = ExtractKey(bytes);


        byte typeByte = bytes[7];

        switch(typeByte){
            case BTBLE_INT_TYPE:
                int intValue = ExtractIntValue(bytes);
                System.out.println("BTBLE Received: " + key + " -> " + intValue );
                return new BTMessage(key, intValue);
            case BTBLE_FLOAT_TYPE:
                double floatValue = ExtractFloatValue(bytes);
                return new BTMessage(key, floatValue);
            case BTBLE_STRING_TYPE:
                String stringValue = ExtractStringValue(bytes);
                System.out.println("BTBLE Received: " + key + " -> " + stringValue );
                return new BTMessage(key, stringValue);
            default:
                return null;
        }
    }

    private static String ExtractKey(byte[] bytes){
        String key = "";
        // extract the key, one byte at a time
        byte[] charAsByteArray = new byte[1];
        for(int i = 0; i < 6; ++i) {
            if (bytes[i] == 0) break;
            charAsByteArray[0] = bytes[i];
            key += new String( charAsByteArray );
        }
        return key;
    }

    private static int ExtractIntValue(byte[] msg){
        int ret = (msg[11] << 24) | (msg[10] << 16) | (msg[9] << 8) | msg[8];
        System.out.println("Extracted int value: " + ret);
        return ret;
    }

    private static String ExtractStringValue(byte[] msg){
        return new String(java.util.Arrays.copyOfRange(msg, 7, BTBLE_MAX_PACKET_LEN)).trim();
    }

    private static double ExtractFloatValue(byte[] msg){
        return 42.0;
    }

    public static List<Integer> EncodeMessage(BTMessage message){
        byte[] keyBytesPossiblyLong = ConvertStringToBytes(message.key);
        byte[] keyBytes = TruncateBytesArray(keyBytesPossiblyLong, 5);


        byte[] outBuffer = TruncateBytesArray(keyBytes, BTBLE_MAX_PACKET_LEN);

        if (message.isIntType()){
            outBuffer[7] = BTBLE_INT_TYPE;
            byte[] intBytes = ConvertIntToBytes(message.intValue);

            for (int i = 0; i < intBytes.length; i++){
                outBuffer[8+i] = intBytes[i];
            }
        } else if (message.isStringType()){
            outBuffer[7] = BTBLE_STRING_TYPE;
            byte[] stringBytes = TruncateBytesArray(ConvertStringToBytes(message.stringValue), 10);

            for (int i = 0; i < stringBytes.length; i++){
                outBuffer[8+i] = stringBytes[i];
            }
        } else if (message.isFloatType()){
            outBuffer[7] = BTBLE_FLOAT_TYPE;
            byte[] doubleBytes = ConvertDoubleToBytes(message.floatValue);
            for (int i = 0; i < doubleBytes.length; i++){
                outBuffer[8+i] = doubleBytes[i];
            }
        } else {
            //TODO: Somehow report error to user.
        }

        List<Integer> outList = new LinkedList<Integer>();
        for (int i = 0; i < outBuffer.length; i++){
            outList.add( new Integer(outBuffer[i]) );
        }

        return outList;
    }

    private static byte[] ConvertIntToBytes(int i){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
    }

    private static byte[] ConvertDoubleToBytes(double d){
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(d).array();
    }

    private static byte[] ConvertStringToBytes(String str){
        try{
            return str.getBytes("UTF-8");
        } catch(UnsupportedEncodingException e){
            System.err.println(e);
            return null;
        }
    }

    private static byte[] TruncateBytesArray(byte[] str, int maxLength){
        return java.util.Arrays.copyOf(str, maxLength);
    }
}