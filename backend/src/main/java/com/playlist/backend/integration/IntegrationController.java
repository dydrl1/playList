package com.playlist.backend.integration;

import com.playlist.backend.integration.dto.ExternalTrackDto;
import com.playlist.backend.integration.dto.ProviderType;
import com.playlist.backend.integration.service.SearchServiceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/integrations")
public class IntegrationController {

    private final SearchServiceRegistry registry;

    @GetMapping("/search")
    public List<ExternalTrackDto> search(
            @RequestParam ProviderType provider,
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
      // Limit 방어
      int safeLimit = Math.max(1, Math.min(limit, 30));
      return registry.get(provider).search(query,safeLimit);
    }
}
