package com.utp.portal.util.constants;

import java.util.Set;

public class Constants {

  private Constants() {
  }

  public static final String CLAIM_USER_ID = "userId";
  public static final String CLAIM_ROLES = "roles";
  public static final String ROLE_NAME_SAE = "ROLE_SAE";

  public static final String ROLE_INTERNAL_SERVICE = "ROLE_INTERNAL_SERVICE";
  public static final Set<String> ROLES_ALLOWED_TO_READ_ANY_USER = Set.of(ROLE_NAME_SAE);
}
