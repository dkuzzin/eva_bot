package ru.eva.server.registration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eva.server.registration.dto.MyRegistrationResponse;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/me")
    public List<MyRegistrationResponse> getMyRegistrations(@RequestHeader("X-Max-User-Id") Long maxUserId) {
        return registrationService.getMyRegistrations(maxUserId);
    }
}