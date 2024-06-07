package sample.sharpImitation;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
/**
 * Java method imitating C#BinaryReader
 * @author RKGG
 * @version 1.0
 * */
public class BinaryReaderJ {


    private static File file;
    private static InputStream in;
    private static DataInputStream dStream;

    public BinaryReaderJ(File file){
        this.file = file;
        try {
            in = new FileInputStream(file);
            dStream = new DataInputStream(in);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Read a byte
     * @author RKGG */
    public static int read(){
        int b = 0;
        try {
            b = dStream.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Read length at once
     * Return byte[] with length length
     * @author RKGG
     * @param length length
     * @return returns byte[]
     * */
    public static byte[] read(int length){
        byte[] bs = new byte[length];
        byte[] rs = new byte[length];
        try {
            dStream.read(bs,0,length);
            System.arraycopy(bs, 0, rs, 0, length);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * Read 2byte and return 1 int
     * @author RKGG
     * @return int
     * */
    public static int readInt16(){
        byte[] bs = new byte[2];
        int temp = 0;
        try {
            dStream.read(bs,0,2);
            temp = HexUtil.byte2int(bs);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * Read 4byte and return 1 int
     * @author RKGG
     * @return returns int
     * */
    public static int readInt32(){
        byte[] bs = new byte[4];
        int temp = 0;
        try {
            dStream.read(bs,0,4);
            temp = HexUtil.byteArrayToInt(bs, 0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return temp;
    }

    public int getAvaible() {
        try {
            return dStream.available();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Read a byte[] according to the index position to read 2bytes and return Int
     * @author RKGG
     * @return int
     * */
    public static int ToInt16(byte[] b,int start){
        byte[] bs = new byte[2];
        int temp = 0;
        System.arraycopy(b, start, bs, 0, 2);
        temp = HexUtil.byte2int(bs);
        return temp;
    }

    /**
     * Read a byte[] according to the index position to read 4bytes and return to Int
     * @author RKGG

     * @return int
     * */
    public static int ToInt32(byte[] b,int start){
        byte[] bs = new byte[4];
        int temp = 0;
        System.arraycopy(b, start, bs, 0, 4);
        temp = HexUtil.byteArrayToInt(bs, 0);
        return temp;
    }

    /**
     * Read 8byte and return 1 double
     * @author RKGG
     * @return double
     * */
    public static double readDouble(){
        byte[] bs = new byte[8];
        double b = 0;
        try {
            dStream.read(bs,0,8);
            b = HexUtil.byteArrayToDouble(bs, 0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Read a byte[] 8byte according to the index position and return Double
     * @author RKGG

     * @return double
     * */
    public static double ToDouble(byte[] b,int start){
        byte[] bs = new byte[8];
        System.arraycopy(b, start, bs, 0, 8);
        double d = HexUtil.byteArrayToDouble(bs, 0);
        return d;
    }

    /**
     * Read a byte[] with a length of length*8
     * Returns a double[] of length length
     * @author RKGG
     * @param length length

     * */
    public static double[] readDoubles(int length){
        byte[] bs = new byte[length*8];
        double[] d = new double[length];
        try {
            dStream.read(bs,0,bs.length);
            d = HexUtil.getData(bs, 0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d;
    }

    /**
     * Read 4byte and return 1 float
     * @author RKGG
     * @return return float
     * */
    public static float readFloat() throws IOException{
        byte[] bs = new byte[4];
        float f = 0;
        try {
            dStream.read(bs,0,4);
            f = HexUtil.byteArrayToFloat(bs, 0, 0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return f;
    }

    /**
     * Read a byte[] according to the index position to read 4bytes and return 1 float
     * @author RKGG

     * @return return float
     * */
    public static float ToFloat(byte[] b,int start){
        byte[] bs = new byte[4];
        System.arraycopy(b, start, bs, 0, 4);
        float f = HexUtil.byteArrayToFloat(bs, 0, 0);
        return f;
    }

    /**
     * Read a byte[] of length length*4
     * Return a float[] of length length
     * @author RKGG
     * @param length
     * @return float[]
     * */
    public static float[] readFloats(int length){
        byte[] bs = new byte[length*4];
        float[] fs = new float[length];
        try {
            dStream.read(bs,0,bs.length);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String s = new String(bs);
        String[] ssr = s.split(",");
        for (int i = 0; i < fs.length; i++) {
            fs[i] = Float.parseFloat(ssr[i]);
        }
        return fs;
    }

    /**
     * Read 1byte to 1char
     * @author RKGG
     * @return char
     * */
    public static char readChar(){
        byte[] bs = new byte[1];
        char ch = 0;
        try {
            dStream.read(bs,0,1);
            ch = HexUtil.byteArrayToChar(bs);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ch;
    }

    /**
     * Read byte[] of length length
     * Returns the char[] with length length
     * @author RKGG
     * @param length
     * @return char[]
     * */
    public static char[] readChars(int length){
        byte[] bs = new byte[length];
        char[] ch = new char[length];
        try {
            dStream.read(bs,0,bs.length);
            ch = HexUtil.getChars(bs);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ch;
    }


    /**
     * Read byte[] of a certain length l
     * Return a String of length l
     * @author RKGG
     * @param length
     * @return String
     * */
    public static String readString(int length){
        byte[] bs = new byte[length];
        String string = "";
        try {
            dStream.read(bs,0,bs.length);
            string = new String(bs);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return string;
    }


    /**
     * Read double in date format
     * Return date format
     * @author RKGG
     * @param format
     * @return String
     * */
    public static String readDate(String format){
        byte[] bs = new byte[8];
        String time = "";
        double time_d = 0;
        try {
            dStream.read(bs,0,8);
            time_d = HexUtil.byteArrayToDouble(bs, 0);
            time = HexUtil.doubleToDate(time_d, format);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;
    }

    /**
     * Read 4byte to 1boolean
     * @author RKGG

     * @param start
     * @return boolean
     * */
    public static boolean ToBoolean(byte[] b,int start){
        byte[] bs = new byte[4];
        System.arraycopy(b, start, bs, 0, 4);
        boolean tmp = HexUtil.byteArrayToBoolean(bs, 0);
        return tmp;
    }

    /**
     * Off flow
     * @author RKGG
     * */
    public static void close(){
        try {
            dStream.close();
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
