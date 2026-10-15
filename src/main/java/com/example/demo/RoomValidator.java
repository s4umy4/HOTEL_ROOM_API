package com.example.demo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RoomValidator {

	static final Set<String> TYPES =
        Set.of("Single", "Double", "Suite", "Deluxe", "Family");
	private static final List<String> FIELDS = List.of(
			"name", "location", "type", "pricePerNight", "capacity", "available", "roomNumber");

	private RoomValidator() {
	}

	static Map<String, Object> validate(Map<String, Object> body, boolean full) {
		Map<String, Object> out = new LinkedHashMap<>();
		for (String field : FIELDS) {
			if (!body.containsKey(field)) {
				if (full) {
					throw ApiException.badRequest(field + " is required");
				}
				continue;
			}
			Object v = body.get(field);
			switch (field) {
				case "name" -> out.put(field, text(v, field, 3, 60));
				case "location" -> out.put(field, text(v, field, 2, 40));
				case "roomNumber" -> out.put(field, text(v, field, 4, 20));
				case "type" -> out.put(field, type(v));
				case "pricePerNight" -> out.put(field, price(v));
				case "capacity" -> out.put(field, capacity(v));
				case "available" -> out.put(field, available(v));
				default -> {
				}
			}
		}
		if (out.isEmpty()) {
			throw ApiException.badRequest("Provide at least one valid field");
		}
		return out;
	}

	private static String text(Object v, String field, int min, int max) {
		if (!(v instanceof String s)) {
			throw ApiException.badRequest(field + " must be a string");
		}
		String t = s.trim();
		if (t.length() < min || t.length() > max) {
			throw ApiException.badRequest(field + " must be " + min + " to " + max + " characters");
		}
		return t;
	}

	private static String type(Object v) {
		if (!(v instanceof String s) || !TYPES.contains(s)) {
			throw ApiException.badRequest(
    "type must be one of Single, Double, Suite, Deluxe, Family");
		}
		return s;
	}

	private static Number price(Object v) {
		if (!(v instanceof Number n)) {
			throw ApiException.badRequest("pricePerNight must be a number");
		}
		double d = n.doubleValue();
		if (!(d > 0) || d > 100000) {
			throw ApiException.badRequest("pricePerNight must be greater than 0 and at most 100000");
		}
		return n;
	}

	private static Integer capacity(Object v) {
		if (!(v instanceof Number n)) {
			throw ApiException.badRequest("capacity must be an integer");
		}
		double d = n.doubleValue();
		if (d != Math.floor(d) || d < 1 || d > 10) {
			throw ApiException.badRequest("capacity must be a whole number from 1 to 10");
		}
		return (int) d;
	}

	private static Boolean available(Object v) {
		if (!(v instanceof Boolean b)) {
			throw ApiException.badRequest("available must be true or false");
		}
		return b;
	}
}
