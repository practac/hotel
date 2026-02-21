package com.hotel.hotel.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.hotel.hotel.controller.dto.LoginRequest;
import com.hotel.hotel.entity.Room;
import com.hotel.hotel.entity.RoomNotFoundException;
import com.hotel.hotel.entity.RoomStatus;
import com.hotel.hotel.repository.RoomRepository;
import com.hotel.hotel.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoomRepository roomRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        if (auth == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtService.generate(request.username());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/rooms")
    public List<Room> listRooms() {
        return roomRepository.findAll();
    }

    @PostMapping("/reset/{slug}")
    public String reset(@PathVariable("slug") String slug) {
        if (slug != null && slug.matches("\\d+")) {
            throw new RoomNotFoundException("Room not found by slug: " + slug);
        }
        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new RoomNotFoundException("Room not found by slug: " + slug));
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);
        return "Room reset to OCCUPIED";
    }
}
