package com.hotel.hotel.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hotel.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    Optional<Room> findBySlug(String slug);
}
