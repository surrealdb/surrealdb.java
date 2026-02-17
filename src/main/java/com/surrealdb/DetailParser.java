package com.surrealdb;

import java.util.LinkedHashMap;
import java.util.Map;

// Note: java.lang.Object is used explicitly throughout because
// com.surrealdb.Object shadows it in this package.

/**
 * Minimal recursive-descent JSON parser for error {@code details} strings.
 *
 * <p>The details format uses a small subset of JSON: strings, objects with
 * string keys and string/object/number values, and numbers. This parser
 * handles exactly that subset without pulling in an external JSON library.
 */
final class DetailParser {

	private final String json;
	private int pos;

	private DetailParser(String json) {
		this.json = json;
		this.pos = 0;
	}

	/**
	 * Parses a JSON details string into a Java object tree.
	 *
	 * @param json the JSON string (may be {@code null})
	 * @return {@code null}, a {@code String}, a {@code Number}, or a
	 *         {@code Map<String, Object>}
	 */
	static java.lang.Object parseDetailsJson(String json) {
		if (json == null || json.isEmpty()) {
			return null;
		}
		DetailParser parser = new DetailParser(json.trim());
		return parser.parseValue();
	}

	private java.lang.Object parseValue() {
		skipWhitespace();
		if (pos >= json.length()) {
			return null;
		}
		char c = json.charAt(pos);
		if (c == '"') {
			return parseString();
		} else if (c == '{') {
			return parseObject();
		} else if (c == 'n' && json.startsWith("null", pos)) {
			pos += 4;
			return null;
		} else if (c == '-' || (c >= '0' && c <= '9')) {
			return parseNumber();
		}
		return null;
	}

	private String parseString() {
		pos++; // skip opening quote
		StringBuilder sb = new StringBuilder();
		while (pos < json.length()) {
			char c = json.charAt(pos);
			if (c == '\\' && pos + 1 < json.length()) {
				pos++;
				char escaped = json.charAt(pos);
				switch (escaped) {
					case '"':  sb.append('"'); break;
					case '\\': sb.append('\\'); break;
					case '/':  sb.append('/'); break;
					case 'n':  sb.append('\n'); break;
					case 't':  sb.append('\t'); break;
					case 'r':  sb.append('\r'); break;
					default:   sb.append(escaped); break;
				}
				pos++;
			} else if (c == '"') {
				pos++; // skip closing quote
				return sb.toString();
			} else {
				sb.append(c);
				pos++;
			}
		}
		return sb.toString();
	}

	private Map<String, java.lang.Object> parseObject() {
		pos++; // skip '{'
		Map<String, java.lang.Object> map = new LinkedHashMap<>();
		skipWhitespace();
		if (pos < json.length() && json.charAt(pos) == '}') {
			pos++;
			return map;
		}
		while (pos < json.length()) {
			skipWhitespace();
			if (pos >= json.length() || json.charAt(pos) != '"') {
				break;
			}
			String key = parseString();
			skipWhitespace();
			if (pos < json.length() && json.charAt(pos) == ':') {
				pos++;
			}
			java.lang.Object value = parseValue();
			map.put(key, value);
			skipWhitespace();
			if (pos < json.length() && json.charAt(pos) == ',') {
				pos++;
			} else {
				break;
			}
		}
		skipWhitespace();
		if (pos < json.length() && json.charAt(pos) == '}') {
			pos++;
		}
		return map;
	}

	private Number parseNumber() {
		int start = pos;
		boolean isFloat = false;
		if (pos < json.length() && json.charAt(pos) == '-') {
			pos++;
		}
		while (pos < json.length() && json.charAt(pos) >= '0' && json.charAt(pos) <= '9') {
			pos++;
		}
		if (pos < json.length() && json.charAt(pos) == '.') {
			isFloat = true;
			pos++;
			while (pos < json.length() && json.charAt(pos) >= '0' && json.charAt(pos) <= '9') {
				pos++;
			}
		}
		String numStr = json.substring(start, pos);
		if (isFloat) {
			return Double.parseDouble(numStr);
		}
		long val = Long.parseLong(numStr);
		if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
			return (int) val;
		}
		return val;
	}

	private void skipWhitespace() {
		while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
			pos++;
		}
	}
}
