package com.github.saphyra.integration.cases.auth;

import org.junit.Test;

public class FilterTest {

    @Test
    public void accessAllowedUri_allowedMethod_byRest() {

    }

    @Test
    public void accessProtectedPath_notLoggedIn_byRest() {
        //No cookie
        //AccessToken not found
        //AccessToken expired
    }

    @Test
    public void accessProtectedPath_noPermission_byRest() {

    }

    @Test
    public void accessGranted() {

    }
}
