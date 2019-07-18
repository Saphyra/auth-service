package com.github.saphyra.authservice.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpMethod;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleSetting {
    @NonNull
    private final String uri;

    @NonNull
    private final Set<HttpMethod> protectedMethods;

    @NonNull
    private final Set<String> roles;

    public static RoleSettingBuilder builder() {
        return new RoleSettingBuilder();
    }

    public static class RoleSettingBuilder {
        private String uri;
        private final Set<HttpMethod> protectedMethods = new HashSet<>();
        private final Set<String> roles = new HashSet<>();

        public RoleSettingBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public RoleSettingBuilder addProtectedMethod(HttpMethod method) {
            protectedMethods.add(method);
            return this;
        }

        public RoleSettingBuilder addAllProtectedMethods() {
            protectedMethods.addAll(Arrays.asList(HttpMethod.values()));
            return this;
        }

        public RoleSettingBuilder addRole(String role) {
            roles.add(role);
            return this;
        }

        public RoleSetting build() {
            RoleSetting roleSetting = new RoleSetting(uri, protectedMethods, roles);
            log.info("Created roleSetting: {}", roleSetting);
            return roleSetting;
        }
    }
}
