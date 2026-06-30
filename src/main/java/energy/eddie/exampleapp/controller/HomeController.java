package energy.eddie.exampleapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @SuppressWarnings("java:S6856")
    @GetMapping(value = { "/", "/{path:[^\\.]*}", "/**/{path:[^\\.]*}" })
    public String vue(
            Model model,
            @Value("${example-app.backend-base-url}") String exampleAppBackendUrl,
            @Value("${example-app.keycloak.url}") String keycloakUrl,
            @Value("${example-app.keycloak.realm}") String keycloakRealm,
            @Value("${example-app.keycloak.client}") String keycloakClient,
            @Value("${example-app.eddie.public-url}") String eddieUrl,
            @Value("${example-app.eddie.historic-dataneed-id}") String eddieHistoricDataneedId,
            @Value("${example-app.eddie.realtime-dataneed-id}") String eddieRealtimeDataneedId
    ) {
        model.addAttribute("exampleAppBackendUrl", exampleAppBackendUrl);
        model.addAttribute("keycloakUrl", keycloakUrl);
        model.addAttribute("keycloakRealm", keycloakRealm);
        model.addAttribute("keycloakClient", keycloakClient);
        model.addAttribute("eddieUrl", eddieUrl);
        model.addAttribute("eddieHistoricDataneedId", eddieHistoricDataneedId);
        model.addAttribute("eddieRealtimeDataneedId", eddieRealtimeDataneedId);

        return "index";
    }
}
