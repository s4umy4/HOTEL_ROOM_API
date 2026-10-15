package com.example.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody Map<String, Object> body) {
        return json(HttpStatus.CREATED, service.create(body).toJson());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable String id) {
        return json(HttpStatus.OK, service.get(id).toJson());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> replace(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        return json(HttpStatus.OK, service.replace(id, body).toJson());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patch(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        return json(HttpStatus.OK, service.patch(id, body).toJson());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

    @GetMapping
    public ResponseEntity<Object> list(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String available,
            @RequestParam(required = false) String minPricePerNight,
            @RequestParam(required = false) String maxPricePerNight,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order) {

        long pageNo = page == null ? 1 : positive(page, "page");
        long pageSize = limit == null ? 10 : positive(limit, "limit");

        if (pageSize > 50) {
            throw ApiException.badRequest("limit must be at most 50");
        }

        if (type != null && !RoomValidator.TYPES.contains(type)) {
            throw ApiException.badRequest(
                    "type must be one of Single, Double, Suite, Deluxe, Family");
        }

        Boolean availableFilter = null;

        if (available != null) {
            if (available.equals("true")) {
                availableFilter = true;
            } else if (available.equals("false")) {
                availableFilter = false;
            } else {
                throw ApiException.badRequest(
                        "available must be true or false");
            }
        }

        Double min = minPricePerNight == null
                ? null : decimal(minPricePerNight, "minPricePerNight");

        Double max = maxPricePerNight == null
                ? null : decimal(maxPricePerNight, "maxPricePerNight");

        Comparator<Room> comparator = null;

        if (sort != null) {
            comparator = switch (sort) {
                case "name" ->
                    Comparator.comparing(
                            (Room r) -> r.name().toLowerCase());
                case "pricePerNight" ->
                    Comparator.comparingDouble(
                            r -> r.pricePerNight().doubleValue());
                case "capacity" ->
                    Comparator.comparingInt(Room::capacity);
                default ->
                    throw ApiException.badRequest(
                            "sort must be one of name, pricePerNight, capacity");
            };
        }

        boolean descending = false;

        if (order != null) {
            if (order.equals("desc")) {
                descending = true;
            } else if (!order.equals("asc")) {
                throw ApiException.badRequest("order must be asc or desc");
            }
        }

        String needle = search == null || search.isEmpty()
                ? null : search.toLowerCase();

        List<Room> matches = new ArrayList<>();

        for (Room r : service.all()) {
            if (type != null && !r.type().equals(type)) {
                continue;
            }

            if (availableFilter != null
                    && r.available() != availableFilter) {
                continue;
            }

            double price = r.pricePerNight().doubleValue();

            if (min != null && price < min) {
                continue;
            }

            if (max != null && price > max) {
                continue;
            }

            if (needle != null
                    && !r.name().toLowerCase().contains(needle)
                    && !r.location().toLowerCase().contains(needle)) {
                continue;
            }

            matches.add(r);
        }

        if (comparator != null) {
            matches.sort(descending
                    ? comparator.reversed() : comparator);
        }

        long total = matches.size();
        long totalPages = (total + pageSize - 1) / pageSize;

        List<Map<String, Object>> data = new ArrayList<>();

        if (pageNo <= totalPages) {
            int from = (int) ((pageNo - 1) * pageSize);
            int to = (int) Math.min(total, from + pageSize);

            for (Room r : matches.subList(from, to)) {
                data.add(r.toJson());
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("data", data);
        body.put("page", pageNo);
        body.put("limit", pageSize);
        body.put("total", total);
        body.put("totalPages", totalPages);

        return json(HttpStatus.OK, body);
    }

    private static long positive(String s, String name) {
        if (!s.matches("[0-9]+")) {
            throw ApiException.badRequest(
                    name + " must be a positive integer");
        }

        long v;

        try {
            v = Long.parseLong(s);
        } catch (NumberFormatException e) {
            v = Long.MAX_VALUE;
        }

        if (v < 1) {
            throw ApiException.badRequest(
                    name + " must be a positive integer");
        }

        return v;
    }

    private static double decimal(String s, String name) {
        try {
            return new BigDecimal(s.trim()).doubleValue();
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(name + " must be a number");
        }
    }

    private static ResponseEntity<Object> json(
            HttpStatus status, Object body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
