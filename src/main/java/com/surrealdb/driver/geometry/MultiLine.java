package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MultiLines can be used to store multiple {@code lines} in a single value.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multiline">SurrealDB Docs - MultiLine</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.5">GeoJSON - MultiLine</a>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiLine implements GeometryPrimitive {

    @NotNull ImmutableList<Line> lines;

    public static @NotNull MultiLine from(@NotNull Collection<Line> lines) {
        return new MultiLine(ImmutableList.copyOf(lines));
    }

    public static @NotNull MultiLine from(Line @NotNull ... lines) {
        return new MultiLine(ImmutableList.copyOf(lines));
    }

    public static @NotNull MultiLine from(@NotNull Line line) {
        return new MultiLine(ImmutableList.of(line));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final List<Line> lines = new ArrayList<>();

        /**
         * @param line The line to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addLine(Line line) {
            lines.add(line);
            return this;
        }

        /**
         * @param lines The lines to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addLines(@NotNull Collection<Line> lines) {
            this.lines.addAll(lines);
            return this;
        }

        /**
         * @param lines The lines to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addLines(Line... lines) {
            Collections.addAll(this.lines, lines);
            return this;
        }

        /**
         * @param line The line to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeLine(Line line) {
            lines.remove(line);
            return this;
        }

        /**
         * @param lines The lines to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeLines(@NotNull Collection<Line> lines) {
            this.lines.removeAll(lines);
            return this;
        }

        /**
         * @param lines The lines to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeLines(@NotNull Line... lines) {
            for (Line line : lines) {
                this.lines.remove(line);
            }
            return this;
        }

        /**
         * Creates and returns a new {@code MultiLine} with the lines added to this {@code Builder}. The
         * {@code Builder's} backing list is copied, meaning that changes to this {@code Builder} will not be reflected
         * in the returned {@code MultiLine}.
         *
         * @return A new {@code MultiLine} instance
         */
        public @NotNull MultiLine build() {
            return MultiLine.from(lines);
        }
    }
}
