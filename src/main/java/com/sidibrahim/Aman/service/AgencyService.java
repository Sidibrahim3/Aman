package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.AgencyDto;
import com.sidibrahim.Aman.entity.Agency;
import com.sidibrahim.Aman.exception.GenericException;
import com.sidibrahim.Aman.mapper.AgencyMapper;
import com.sidibrahim.Aman.repository.AgencyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

    @Transactional
    public AgencyDto save(AgencyDto agencyDto){
        Agency agency = agencyMapper.toAgency(agencyDto);
        agency.setCreateDate(LocalDate.now());
        return agencyMapper.toAgencyDto(agencyRepository.save(agency));
    }

    public Page<AgencyDto> getAll(int page, int size){
        return agencyMapper.toAgencyDtos(agencyRepository.findAll(PageRequest.of(page,size)));
    }

    public void deleteById(Long id){
        agencyRepository.deleteById(id);
    }

    public AgencyDto getById(Long id){
        return agencyMapper.toAgencyDto(agencyRepository.findById(id).orElseThrow(()->new GenericException("Agency Not Found With Id : " + id)));
    }
}
