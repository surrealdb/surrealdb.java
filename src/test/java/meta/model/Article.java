package meta.model;

import com.surrealdb.types.SurrealRecord;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class Article extends SurrealRecord {

    @NotNull String name;

}
