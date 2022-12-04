package meta.model;

import com.surrealdb.types.SurrealEdgeRecord;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class Relationship extends SurrealEdgeRecord {

    List<String> tags;

    public Relationship(String... tags) {
        this.tags = List.of(tags);
    }
}
