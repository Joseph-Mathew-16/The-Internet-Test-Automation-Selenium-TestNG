package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtilities {

    /**
     * This method returns the current time in the format "yyyy-MM-dd hh:mm:ss a".
     *
     * @return A string representing the current time formatted as "yyyy-MM-dd
     * hh:mm:ss a".
     */
    public String getCurrentTimeforReportLogs() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }
}
