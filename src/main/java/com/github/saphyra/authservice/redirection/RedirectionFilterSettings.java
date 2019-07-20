package com.github.saphyra.authservice.redirection;

import com.github.saphyra.authservice.redirection.domain.ProtectedUri;
import com.github.saphyra.authservice.redirection.domain.RedirectionContext;

public interface RedirectionFilterSettings {
    ProtectedUri getProtectedUri();

    boolean shouldRedirect(RedirectionContext redirectionContext);

    String getRedirectionPath(RedirectionContext redirectionContext);

    Integer getFilterOrder();
}
