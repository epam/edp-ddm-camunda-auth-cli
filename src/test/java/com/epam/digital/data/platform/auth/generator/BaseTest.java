package com.epam.digital.data.platform.auth.generator;

import com.epam.digital.data.platform.auth.generator.dto.rest.AuthorizationCreateDto;
import com.epam.digital.data.platform.auth.generator.enums.Resource;
import java.util.List;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTest {

  protected final String jwtToken = "token";
  protected final String bpmsUrl = "http://localhost:8081";

  protected AuthorizationCreateDto buildCreateDto(Resource resource,
      List<String> permissions, int authType, String resourceId, String groupId) {
    return AuthorizationCreateDto.builder()
        .resourceType(resource.getValue())
        .permissions(permissions)
        .type(authType)
        .resourceId(resourceId)
        .groupId(groupId)
        .build();
  }
}
