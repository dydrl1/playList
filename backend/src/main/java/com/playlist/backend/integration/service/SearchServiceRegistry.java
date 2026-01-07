package com.playlist.backend.integration.service;

import com.playlist.backend.integration.dto.ExternalTrackDto;
import com.playlist.backend.integration.dto.ProviderType;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchServiceRegistry {

    private final Map<ProviderType, ExternalSearchService> map = new EnumMap<>(ProviderType.class);


    public SearchServiceRegistry(List<ExternalSearchService> services){
        for (ExternalSearchService s : services){
            map.put(s.provider(), s) ;
        }
    }

    public ExternalSearchService get(ProviderType provider){
        ExternalSearchService service = map.get(provider);
        if(service == null) {
            throw new IllegalArgumentException("Unsupported provider" + provider);
        }
        return service;
    }
}
