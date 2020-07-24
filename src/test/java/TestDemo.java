import com.baseboot.common.utils.ByteUtil;
import org.junit.Test;

import java.util.Arrays;

public class TestDemo {//0, 0, 1, -33 479   [0, 1, -33, 0]

    //c++  479  [-33,1,0,0]   10003 [0, 0, 19, 39]/19,39,0,0
    //java 479  [0, 0, 1, -33]  10003 [0, 0, 39, 19]

    @Test
    public void testUnsigned() {
        int i1 = 479 >>> 2;
        byte b1 = (byte) 191;
        System.out.println(b1);
        System.out.println(i1);
        byte[] bytes = intToByteArray(479);//[0, 0, 39, 19]
        System.out.println(Arrays.toString(bytes));//19,39,0,0
        byte[] b={0, 0, 19, 39};//-65,-67,1,0  479 [191,189,1.0]
        int i = ByteUtil.bytes2IntBigEndian(b);
        System.out.println(i);


    }

    int ConvertTo32(int la, int lb, int lc, int ld) {
        la = 0XFFFFFF | (la << 24);
        lb = 0XFF00FFFF | (lb << 16);
        lc = 0XFFFF00FF | (lc << 8);
        ld = 0XFFFFFF00 | ld;
        return la & lb & lc & ld;
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
}
