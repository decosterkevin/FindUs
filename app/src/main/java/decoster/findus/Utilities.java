package decoster.findus;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by kevin on 22.02.18.
 */

public class Utilities {
    public static String getDateToString(Long timestamp) {
        Date date = new Date(timestamp);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    public static Info getUser(String id, List<Info> data) {
        for(Info info: data) {
            if(info.getUserId().equals(id)) {
                return info;
            }
        }
        return null;
    }


}
