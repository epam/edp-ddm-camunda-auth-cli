package com.epam.digital.data.platform.auth.generator.service;

import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import java.util.List;

/**
 * Service that manages authorizations based on specified config files.
 */
public interface AuthService {

  /**
   * Remove authorizations for specific roles based on config files.
   * @param authConfigDtoList deserialized list of config yaml files.
   */
  void cleanAuthorizations(String clientUrl, String jwtToken, List<AuthConfigDto> authConfigDtoList);

  /**
   * Create authorizations for specific roles and process definition based on config files.
   * @param authConfigDtoList deserialized list of config yaml files.
   */
  void createAuthorizations(String clientUrl, String jwtToken, List<AuthConfigDto> authConfigDtoList);
}
