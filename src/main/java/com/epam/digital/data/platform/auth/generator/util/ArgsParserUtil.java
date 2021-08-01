package com.epam.digital.data.platform.auth.generator.util;

import java.util.Optional;
import org.springframework.boot.ApplicationArguments;

public final class ArgsParserUtil {

  public static String getSingleParamOrThrow(String key, String exceptionMsg,
      ApplicationArguments args) {
    var param = args.getOptionValues(key);
    return Optional.ofNullable(param).map(p -> p.get(0))
        .orElseThrow(() -> new IllegalArgumentException(exceptionMsg));
  }
}
