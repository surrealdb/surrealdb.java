package test.driver.model;

import com.surrealdb.driver.geometry.Point;
import lombok.Value;

@Value
public class City {

    Point location;
    String name;
    int population;

}
