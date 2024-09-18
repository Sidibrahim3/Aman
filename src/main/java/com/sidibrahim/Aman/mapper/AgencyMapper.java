package com.sidibrahim.Aman.mapper;

import com.sidibrahim.Aman.dto.AgencyDto;
import com.sidibrahim.Aman.entity.Agency;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AgencyMapper {

    public Agency toAgency(AgencyDto agencyDto) {
        return Agency.builder()
                .id(agencyDto.getId())
                .name(agencyDto.getName())
                .agencyCode(agencyDto.getAgencyCode())
                .address(agencyDto.getAddress())
                .build();
    }

    public AgencyDto toAgencyDto(Agency agency) {
        return AgencyDto.builder()
                .id(agency.getId())
                .name(agency.getName())
                .agencyCode(agency.getAgencyCode())
                .address(agency.getAddress())
                .createDate(agency.getCreateDate())
                .build();
    }

    public List<AgencyDto> toAgencyDtos(List<Agency> agencyList) {
        return agencyList.stream().map(this::toAgencyDto).toList();
    }

    public Page<AgencyDto> toAgencyDtos(Page<Agency> agencyList) {
        return agencyList.map(this::toAgencyDto);
    }

    public List<Agency> toAgencyList(List<AgencyDto> agencyDtoList) {
        return agencyDtoList.stream().map(this::toAgency).toList();
    }

}
