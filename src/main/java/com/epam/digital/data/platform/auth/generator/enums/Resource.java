package com.epam.digital.data.platform.auth.generator.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum Resource {

  PROCESS_DEFINITION(6),
  PROCESS_INSTANCE(8);

  private final int value;
}
