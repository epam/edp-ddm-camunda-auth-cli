package com.epam.digital.data.platform.auth.generator.dto.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDefinitionDto {

  @JsonProperty("process_definitions")
  private List<ProcessDefinitionAuthConfigDto> processDefinitions;
}
