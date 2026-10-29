package com.epl.auth.services;

import com.epl.auth.configurations.AuthProperties;
import com.epl.auth.repositories.AppUserRepository;
import com.epl.auth.v1.CreateUserRequest;
import com.epl.auth.v1.User;
import com.epl.auth.v1.UserType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates the first user ({@code epl.auth.bootstrap-admin}) while iam.app_user is empty, so someone can sign in
 * and create the others. The password must be changed at first login.
 */
@RequiredArgsConstructor
@Component
@Profile("!migrate")
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository userRepository;
    private final UserService userService;
    private final AuthProperties authProperties;

    @Override
    public void run(ApplicationArguments args) {
        var properties = authProperties.bootstrapAdmin();
        if (properties.password() == null || properties.password().isBlank() || userRepository.count() > 0) {
            return;
        }
        userService.create(CreateUserRequest.newBuilder()
                .setUser(User.newBuilder()
                        .setUsername(properties.username())
                        .setDisplayName("Administrator")
                        .setType(UserType.USER_TYPE_INTERNAL))
                .setInitialPassword(properties.password())
                .build());
        logger.warn("Created the bootstrap user '{}'; change its password at first login", properties.username());
    }
}
