package com.epam.digital.data.platform.auth.generator.dto.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionAuthConfigDto {

  @JsonProperty("process_definition_id")
  private String processDefinitionId;
  private List<String> roles;
}
