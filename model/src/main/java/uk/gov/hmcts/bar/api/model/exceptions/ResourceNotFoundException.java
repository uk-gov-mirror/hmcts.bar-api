package uk.gov.hmcts.bar.api.model.exceptions;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String idName;
    private final Object idValue;

    public ResourceNotFoundException(String resourceName, String idName, Object idValue) {
        this.resourceName = resourceName;
        this.idName = idName;
        this.idValue = idValue;
    }
}
