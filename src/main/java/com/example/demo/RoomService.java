package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class RoomService {

	private final Map<Long, Room> rooms = new LinkedHashMap<>();
	private long nextId = 1;

	public synchronized Room create(Map<String, Object> body) {
		Map<String, Object> values = RoomValidator.validate(body, true);
		ensureUnique((String) values.get("roomNumber"), -1);
		Room room = build(nextId, values);
		rooms.put(nextId, room);
		nextId++;
		return room;
	}

	public synchronized Room get(String rawId) {
		return require(rawId);
	}

	public synchronized Room replace(String rawId, Map<String, Object> body) {
		Room current = require(rawId);
		Map<String, Object> values = RoomValidator.validate(body, true);
		ensureUnique((String) values.get("roomNumber"), current.id());
		Room updated = build(current.id(), values);
		rooms.put(current.id(), updated);
		return updated;
	}

	public synchronized Room patch(String rawId, Map<String, Object> body) {
		Room current = require(rawId);
		Map<String, Object> values = RoomValidator.validate(body, false);
		Map<String, Object> merged = new HashMap<>(current.toJson());
		merged.putAll(values);
		ensureUnique((String) merged.get("roomNumber"), current.id());
		Room updated = build(current.id(), merged);
		rooms.put(current.id(), updated);
		return updated;
	}

	public synchronized void delete(String rawId) {
		Room current = require(rawId);
		rooms.remove(current.id());
	}

	public synchronized List<Room> all() {
		return new ArrayList<>(rooms.values());
	}

	private Room require(String rawId) {
		if (rawId == null || !rawId.matches("[0-9]{1,18}")) {
			throw ApiException.notFound("Record not found");
		}
		Room room = rooms.get(Long.parseLong(rawId));
		if (room == null) {
			throw ApiException.notFound("Record not found");
		}
		return room;
	}

	private void ensureUnique(String roomNumber, long selfId) {
		for (Room r : rooms.values()) {
			if (r.id() != selfId && r.roomNumber().equalsIgnoreCase(roomNumber)) {
				throw ApiException.conflict("roomNumber already exists");
			}
		}
	}

	private Room build(long id, Map<String, Object> v) {
		return new Room(id,
				(String) v.get("name"),
				(String) v.get("location"),
				(String) v.get("type"),
				(Number) v.get("pricePerNight"),
				(Integer) v.get("capacity"),
				(Boolean) v.get("available"),
				(String) v.get("roomNumber"));
	}
}
