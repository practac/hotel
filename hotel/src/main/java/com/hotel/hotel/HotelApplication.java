package com.hotel.hotel;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hotel.hotel.entity.Room;
import com.hotel.hotel.entity.RoomStatus;
import com.hotel.hotel.repository.RoomRepository;
import java.util.UUID;


@SpringBootApplication
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }

    @Bean
    CommandLineRunner init(RoomRepository roomRepository) {
        return args -> {
            String[] rooms = {"101", "102", "103", "104", "105"};
            for (String rn : rooms) {
                if (roomRepository.findByRoomNumber(rn).isEmpty()) {
                    Room room = new Room();
                    room.setRoomNumber(rn);
                    room.setSlug(UUID.randomUUID().toString());
                    room.setStatus(RoomStatus.OCCUPIED);
                    roomRepository.save(room);
                }
            }

            // If any existing slugs are missing or equal to the room number, regenerate to prevent predictable links
            roomRepository.findAll().forEach(room -> {
                String slug = room.getSlug();
                boolean needsNewSlug = (slug == null || slug.isBlank())
                        || slug.equals(room.getRoomNumber())
                        || slug.matches("\\d+");
                if (needsNewSlug) {
                    room.setSlug(UUID.randomUUID().toString());
                    roomRepository.save(room);
                }
            });
        };
    }
}
