/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.auth.generator;

import com.epam.digital.data.platform.auth.generator.dto.rest.AuthorizationCreateDto;
import com.epam.digital.data.platform.auth.generator.enums.Resource;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
