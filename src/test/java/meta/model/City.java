package meta.model;

import com.google.common.collect.ImmutableList;
import com.surrealdb.geometry.Point;
import lombok.Value;

@Value
public class City {

    public static final City TOKYO = new City(Point.fromGeoHash("xn76urx6"), "Tokyo", 37);
    public static final City DELHI = new City(Point.fromGeoHash("ttnghcy0"), "Delhi", 28);
    public static final City SHANGHAI = new City(Point.fromGeoHash("wtw3sjq6"), "Shanghai", 25);
    public static final City SAO_PAULO = new City(Point.fromGeoHash("6gyf4bdx"), "São Paulo", 21);
    public static final City MEXICO_CITY = new City(Point.fromGeoHash("9g3w81t7"), "Mexico City", 21);
    public static final City CAIRO = new City(Point.fromGeoHash("stq4yv3j"), "Cairo", 20);
    public static final City MUMBAI = new City(Point.fromGeoHash("te7ud2ev"), "Mumbai", 20);
    public static final City BEIJING = new City(Point.fromGeoHash("wx4g0bm6"), "Beijing", 20);
    public static final City DHAKA = new City(Point.fromGeoHash("wh0r3qs3"), "Dhaka", 20);
    public static final City OSAKA = new City(Point.fromGeoHash("xn0m77v9"), "Osaka", 19);
    public static final City NEW_YORK = new City(Point.fromGeoHash("dr5regw3"), "New York", 19);
    public static final City KARACHI = new City(Point.fromGeoHash("tkrtkvgh"), "Karachi", 19);
    public static final City BUENOS_AIRES = new City(Point.fromGeoHash("69y7pkxf"), "Buenos Aires", 19);
    public static final City CHONGQING = new City(Point.fromGeoHash("wm7b0tu3"), "Chongqing", 19);
    public static final City ISTANBUL = new City(Point.fromGeoHash("sxk973m6"), "Istanbul", 18);

    Point location;
    String name;
    int population;

    public static ImmutableList<City> allCityCenters() {
        return ImmutableList.of(
            TOKYO,
            DELHI,
            SHANGHAI,
            SAO_PAULO,
            MEXICO_CITY,
            CAIRO,
            MUMBAI,
            BEIJING,
            DHAKA,
            OSAKA,
            NEW_YORK,
            KARACHI,
            BUENOS_AIRES,
            CHONGQING,
            ISTANBUL
        );
    }

}
