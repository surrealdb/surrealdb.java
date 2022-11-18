package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * MultiLines can be used to store multiple {@code lines} in a single value.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multiline">SurrealDB Docs - MultiLine</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.5">GeoJSON - MultiLine</a>
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiLineString implements GeometryPrimitive, Iterable<LineString> {

    @NotNull ImmutableList<LineString> lines;

    public static @NotNull MultiLineString from(@NotNull Collection<LineString> lines) {
        return new MultiLineString(ImmutableList.copyOf(lines));
    }

    public static @NotNull MultiLineString from(LineString @NotNull ... lines) {
        return new MultiLineString(ImmutableList.copyOf(lines));
    }

    public static @NotNull MultiLineString from(@NotNull LineString line) {
        return new MultiLineString(ImmutableList.of(line));
    }

    public int getLineCount() {
        return lines.size();
    }

    public @NotNull LineString getLine(int index) {
        return lines.get(index);
    }

    @Override
    public @NotNull Iterator<LineString> iterator() {
        return lines.iterator();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<LineString> lines = new ArrayList<>();

        /**
         * @param line The line to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addLine(LineString line) {
            lines.add(line);
            return this;
        }

        /**
         * @param lines The lines to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addLines(@NotNull Collection<LineString> lines) {
            this.lines.addAll(lines);
            return this;
        }

        /**
         * @param lines The lines to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addLines(LineString... lines) {
            Collections.addAll(this.lines, lines);
            return this;
        }

        /**
         * @param line The line to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeLine(LineString line) {
            lines.remove(line);
            return this;
        }

        /**
         * @param lines The lines to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeLines(@NotNull Collection<LineString> lines) {
            this.lines.removeAll(lines);
            return this;
        }

        /**
         * @param lines The lines to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeLines(@NotNull LineString @NonNull ... lines) {
            for (LineString line : lines) {
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
        public @NotNull MultiLineString build() {
            return MultiLineString.from(lines);
        }
    }
}
