package com.hotel.hotel.controller;

import com.hotel.hotel.entity.Room;
import com.hotel.hotel.entity.RoomNotFoundException;
import com.hotel.hotel.entity.RoomStatus;
import com.hotel.hotel.repository.RoomRepository;
import com.hotel.hotel.service.CheckoutTokenService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final CheckoutTokenService checkoutTokenService;

    // QR 체크아웃
    @PostMapping("/checkout/{slug}")
    public String checkout(
            @Parameter(description = "방 슬러그", example = "9f3b-...")
            @PathVariable("slug") String slug,
            @Parameter(description = "QR로 발급된 토큰")
            @RequestParam("token") String token) {

        rejectNumericSlug(slug);

        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new RoomNotFoundException("Room not found by slug: " + slug));

        String tokenRoom = checkoutTokenService.validate(token);
        if (!room.getRoomNumber().equals(tokenRoom)) {
            throw new RoomNotFoundException("Room not found: " + room.getRoomNumber());
        }

        if (room.getStatus() == RoomStatus.CHECKED_OUT) {
            return "Already checked out";
        }

        room.setStatus(RoomStatus.CHECKED_OUT);
        roomRepository.save(room);

        return "Checkout completed";
    }

    @GetMapping("/checkout/{slug}/token")
    public String issueToken(
            @Parameter(description = "방 슬러그", example = "9f3b-...")
            @PathVariable("slug") String slug) {
        // Issue a short-lived token for the specific room; QR는 이 슬러그 기반 고정 URL 사용
        rejectNumericSlug(slug);
        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new RoomNotFoundException("Room not found by slug: " + slug));
        return checkoutTokenService.issue(room.getRoomNumber());
    }

    private void rejectNumericSlug(String slug) {
        if (slug != null && slug.matches("\\d+")) {
            throw new RoomNotFoundException("Room not found by slug: " + slug);
        }
    }

    @GetMapping("/rooms/{slug}/number")
    public String getRoomNumber(
            @Parameter(description = "방 슬러그", example = "9f3b-...")
            @PathVariable("slug") String slug) {
        rejectNumericSlug(slug);
        return roomRepository.findBySlug(slug)
                .orElseThrow(() -> new RoomNotFoundException("Room not found by slug: " + slug))
                .getRoomNumber();
    }

}
