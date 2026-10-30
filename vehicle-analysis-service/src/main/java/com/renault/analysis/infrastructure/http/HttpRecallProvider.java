package com.renault.analysis.infrastructure.http;

import com.renault.analysis.application.RecallProviderUnavailableException;
import com.renault.analysis.domain.model.RecallCampaign;
import com.renault.analysis.domain.ports.RecallProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class HttpRecallProvider implements RecallProvider {

    private final RestClient restClient;

    public HttpRecallProvider(RestClient.Builder restClientBuilder,
                              @Value("${recall-provider.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public List<RecallCampaign> activeCampaignsFor(String vin) {
        ProviderRecallsResponse response;
        try {
            response = restClient.get()
                    .uri("/recalls?vin={vin}", vin)
                    .retrieve()
                    .body(ProviderRecallsResponse.class);
        } catch (RestClientException e) {
            throw new RecallProviderUnavailableException("Fournisseur de rappels injoignable", e);
        }
        if (response == null || response.campaigns() == null) {
            throw new RecallProviderUnavailableException("Réponse invalide du fournisseur de rappels");
        }
        return response.campaigns().stream()
                .filter(campaign -> "ACTIVE".equalsIgnoreCase(campaign.status()))
                .map(campaign -> new RecallCampaign(campaign.code(), campaign.description()))
                .toList();
    }

    record ProviderRecallsResponse(String vin, List<ProviderCampaign> campaigns) {}
    record ProviderCampaign(String code, String description, String status) {}
}