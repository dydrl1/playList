package com.playlist.backend.integration.service;


import com.playlist.backend.integration.dto.ExternalTrackDto;
import com.playlist.backend.integration.dto.ProviderType;

import java.util.List;

public interface ExternalSearchService {
    ProviderType provider();
    List<ExternalTrackDto> search(String query, int limit);



}
