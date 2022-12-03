package meta.model;

import com.google.common.collect.ImmutableList;
import com.surrealdb.types.Id;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
@RequiredArgsConstructor
public class Relationship {

    @Nullable Id id;
    List<String> tags;

    public Relationship(@Nullable Id id, String... tags) {
        this.id = id;
        this.tags = ImmutableList.copyOf(tags);
    }

    public Relationship(String... tags) {
        id = null;
        this.tags = ImmutableList.copyOf(tags);
    }
}
