package jma.util;

import jma.models.GPSRecord;

public class GPSUtils {
    
    private final static double EARTH_RADIUS = 6378137;//赤道半径(单位m)
    
    private static double rad(double d)
    {
       return d * Math.PI / 180.0;
    }

    //(单位m)
    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
       double radLat1 = rad(lat1);
       double radLat2 = rad(lat2);
       double a = radLat1 - radLat2;
       double b = rad(lng1) - rad(lng2);

       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
        Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
       s = s * EARTH_RADIUS;
       s = Math.round(s * 10000) / 10000;
       return s;
    }
    
    //(单位m)
    public static Double getDistance(GPSRecord a, GPSRecord b) {
        if(a == null || !a.isValid() || b == null || !b.isValid()) {
            return null;
        }
        
        return getDistance(a.getLatituede(), a.getLongitude(), 
                b.getLatituede(), b.getLongitude());
    }
}
