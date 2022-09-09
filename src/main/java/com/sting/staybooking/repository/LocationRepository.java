package com.sting.staybooking.repository;

import com.sting.staybooking.model.Location;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LocationRepository extends ElasticsearchRepository<Location,Long>, CustomLocationRepository{
}
