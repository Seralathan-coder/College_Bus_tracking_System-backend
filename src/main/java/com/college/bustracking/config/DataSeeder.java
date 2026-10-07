package com.college.bustracking.config;

import com.college.bustracking.entity.Bus;
import com.college.bustracking.entity.BusStatus;
import com.college.bustracking.entity.Role;
import com.college.bustracking.entity.Route;
import com.college.bustracking.entity.RouteStatus;
import com.college.bustracking.entity.Stop;
import com.college.bustracking.entity.User;
import com.college.bustracking.repository.BusRepository;
import com.college.bustracking.repository.RouteRepository;
import com.college.bustracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        String adminEmail = environment.getProperty("app.seed.admin-email", "admin@college.edu");
        ensureUser("College Admin", adminEmail,
                environment.getProperty("app.seed.admin-password", "Admin@123"), Role.ADMIN);
        User driver = ensureUser("Ravi Kumar", "driver@college.edu", "Driver@123", Role.DRIVER);
        ensureUser("Anita Student", "student@college.edu", "Student@123", Role.STUDENT);

        if (busRepository.count() > 0) return;
        Bus bus = busRepository.save(Bus.builder().busNumber("CB-01").driver(driver)
                .status(BusStatus.IDLE).currentLat(11.3410).currentLng(77.7172).build());

        Route route = Route.builder()
                .routeName("Erode to College")
                .driver(driver)
                .bus(bus)
                .status(RouteStatus.ACTIVE)
                .build();
        route.getStops().add(Stop.builder()
                .stopName("Erode Bus Stand")
                .latitude(11.3410)
                .longitude(77.7172)
                .sequenceOrder(1)
                .route(route)
                .build());
        route.getStops().add(Stop.builder()
                .stopName("Perundurai")
                .latitude(11.2750)
                .longitude(77.6050)
                .sequenceOrder(2)
                .route(route)
                .build());
        route.getStops().add(Stop.builder()
                .stopName("College Gate")
                .latitude(11.2700)
                .longitude(77.5900)
                .sequenceOrder(3)
                .route(route)
                .build());
        routeRepository.save(route);
    }

    private User ensureUser(String name, String email, String password, Role role) {
        return userRepository.findByEmail(email.toLowerCase()).orElseGet(() -> userRepository.save(User.builder()
                .name(name).email(email.toLowerCase()).password(passwordEncoder.encode(password)).role(role).build()));
    }
}
