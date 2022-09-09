package com.sting.staybooking.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.sting.staybooking.exception.GeoCodingException;
import com.sting.staybooking.exception.InvalidStayAddressException;
import com.sting.staybooking.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeoCodingService {
    private final GeoApiContext geoApiContext;

    @Autowired
    public GeoCodingService(GeoApiContext geoApiContext){
        this.geoApiContext=geoApiContext;
    }

    public Location getLatLng(Long id, String address) throws GeoCodingException {
        try {
            GeocodingResult result = GeocodingApi.geocode(geoApiContext, address).await()[0];
            if (result.partialMatch) {
                throw new InvalidStayAddressException("Failed to find stay address");
            }
            return new Location(id, new GeoPoint(result.geometry.location.lat, result.geometry.location.lng));
        } catch (IOException | ApiException | InterruptedException e) {
            e.printStackTrace();
            throw new GeoCodingException("Failed to encode stay address");
        }
    }

}
