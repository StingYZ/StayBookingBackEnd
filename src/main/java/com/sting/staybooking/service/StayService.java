package com.sting.staybooking.service;

import com.sting.staybooking.exception.StayDeleteException;
import com.sting.staybooking.exception.StayNotExistException;
import com.sting.staybooking.model.*;
import com.sting.staybooking.repository.LocationRepository;
import com.sting.staybooking.repository.ReservationRepository;
import com.sting.staybooking.repository.StayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StayService {

    private StayRepository stayRepository;
    private ImageStorageService imageStorageService;
    private LocationRepository locationRepository;
    private ReservationRepository reservationRepository;
    private GeoCodingService geoCodingService;



    @Autowired
    public StayService(StayRepository stayRepository,LocationRepository locationRepository,  ReservationRepository reservationRepository, ImageStorageService imageStorageService,GeoCodingService geoCodingService){
        this.stayRepository = stayRepository;
        this.imageStorageService=imageStorageService;
        this.geoCodingService = geoCodingService;
        this.locationRepository = locationRepository;
        this.reservationRepository = reservationRepository;

    }

    public List<Stay> listByUser(String username){
        return stayRepository.findByHost(new User.Builder().setUsername(username).build());
    }

    public Stay findByIdAndHost(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }
        return stay;
    }

    public void add(Stay stay, MultipartFile[] images) {
        List<StayImage> stayImages=new ArrayList<>();

        List<String> mediaLinks = Arrays.stream(images).parallel().map(image -> imageStorageService.save(image)).collect(Collectors.toList());

        for (String mediaLink : mediaLinks) {
            stayImages.add(new StayImage(mediaLink, stay));
        }

        stay.setImages(stayImages);
        stayRepository.save(stay);

        Location location = geoCodingService.getLatLng(stay.getId(), stay.getAddress());
        locationRepository.save(location);

    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }
        List<Reservation> reservations = reservationRepository.findByStayAndCheckoutDateAfter(stay, LocalDate.now());
        if (reservations != null && reservations.size() > 0) {
            throw new StayDeleteException("Cannot delete stay with active reservation");
        }

        stayRepository.deleteById(stayId);
    }

}
