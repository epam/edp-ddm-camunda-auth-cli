package com.epam.digital.data.platform.auth.generator.dto.rest;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorizationCreateDto {

  private Integer type;
  private String groupId;
  private String resourceId;
  private Integer resourceType;
  private List<String> permissions;
}
