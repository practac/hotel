package com.hotel.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.hotel.hotel.entity.Room;
import com.hotel.hotel.entity.RoomNotFoundException;
import com.hotel.hotel.entity.RoomStatus;
import com.hotel.hotel.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public String checkout(String roomNumber) {

        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException("방이 존재하지 않습니다: " + roomNumber));

        if (room.getStatus() == RoomStatus.CHECKED_OUT) {
            return "이미 체크아웃된 방입니다.";
        }

        if (room.getStatus() == RoomStatus.AVAILABLE) {
            return "현재 투숙 중이 아닙니다.";
        }

        // OCCUPIED 상태일 때만 체크아웃 가능
        room.setStatus(RoomStatus.CHECKED_OUT);
        roomRepository.save(room);

        return "체크아웃 완료";
    }
}
