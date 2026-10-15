package com.example.demo;

import java.util.LinkedHashMap;
import java.util.Map;

public record Room(long id, String name, String location, String type, Number pricePerNight,
		int capacity, boolean available, String roomNumber) {

	public Map<String, Object> toJson() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", id);
		m.put("name", name);
		m.put("location", location);
		m.put("type", type);
		m.put("pricePerNight", pricePerNight);
		m.put("capacity", capacity);
		m.put("available", available);
		m.put("roomNumber", roomNumber);
		return m;
	}
}
