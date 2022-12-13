package meta.model;

import com.surrealdb.types.SurrealEdgeRecord;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Value
@EqualsAndHashCode(callSuper = true)
public class Contribution extends SurrealEdgeRecord {

    @NotNull Instant time;

}
