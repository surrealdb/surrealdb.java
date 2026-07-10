package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class Leaderboard {

	public RecordId id;
	public TreeMap<String, Long> scores;
	public LinkedHashMap<String, String> labels;
	public SortedMap<String, Long> ranks;

	public Leaderboard() {
	}

	public Leaderboard(TreeMap<String, Long> scores, LinkedHashMap<String, String> labels,
			SortedMap<String, Long> ranks) {
		this.scores = scores;
		this.labels = labels;
		this.ranks = ranks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Leaderboard leaderboard = (Leaderboard) o;
		return Objects.equals(id, leaderboard.id) && Objects.equals(scores, leaderboard.scores)
				&& Objects.equals(labels, leaderboard.labels) && Objects.equals(ranks, leaderboard.ranks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, scores, labels, ranks);
	}

	@Override
	public String toString() {
		return "id: " + id + ", scores: " + scores + ", labels: " + labels + ", ranks: " + ranks;
	}
}
