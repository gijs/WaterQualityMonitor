package wqm.radio.util;

import com.rapplogic.xbee.api.XBeeAddress64;

/**
 * Date: 11/4/13
 * Time: 7:25 AM
 *
 * @author NigelB
 */
public class AddressUtil {

    public static String getStringAddress(XBeeAddress64 address) {
        int[] addr = address.getAddress();
        return String.format("%02X %02X %02X %02X %02X %02X %02X %02X",
                addr[0],
                addr[1],
                addr[2],
                addr[3],
                addr[4],
                addr[5],
                addr[6],
                addr[7]);
    }

    public static String getCompactStringAddress(XBeeAddress64 address)
    {
        int[] addr = address.getAddress();
        return String.format("%02X%02X%02X%02X%02X%02X%02X%02X",
                addr[0],
                addr[1],
                addr[2],
                addr[3],
                addr[4],
                addr[5],
                addr[6],
                addr[7]);
    }
}
