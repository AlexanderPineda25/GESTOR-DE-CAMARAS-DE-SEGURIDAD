package Camaras.VIDEOCAMARAS.domain.model.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String authority;

    RoleType(String authority) {
        this.authority = authority;
    }
}
