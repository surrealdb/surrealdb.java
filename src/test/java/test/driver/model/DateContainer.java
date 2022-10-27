package test.driver.model;

import lombok.Value;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * Used for testing date (de)serialization.
 *
 * @author Damian Kocher
 */
@Value
public class DateContainer {

    Instant instant;
    OffsetDateTime offsetDateTime;
    ZonedDateTime zonedDateTime;

}
