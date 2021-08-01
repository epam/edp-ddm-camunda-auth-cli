package com.epam.digital.data.platform.auth.generator.service;

import static com.epam.digital.data.platform.auth.generator.service.BpmsAuthServiceImpl.AUTH_TYPE_GRANT;
import static com.epam.digital.data.platform.auth.generator.service.BpmsAuthServiceImpl.PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS;
import static com.epam.digital.data.platform.auth.generator.service.BpmsAuthServiceImpl.PROCESS_INSTANCE_AUTH_SEARCH_QUERY_PARAMS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.auth.generator.BaseTest;
import com.epam.digital.data.platform.auth.generator.client.BpmsRestClient;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthDefinitionDto;
import com.epam.digital.data.platform.auth.generator.dto.configuration.ProcessDefinitionAuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.AuthResponseDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.AuthorizationCreateDto;
import com.epam.digital.data.platform.auth.generator.enums.Permission;
import com.epam.digital.data.platform.auth.generator.enums.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class BpmsAuthServiceImplTest extends BaseTest {

  @Mock
  private BpmsRestClient bpmsRestClient;

  private AuthService authService;

  @Before
  public void init() {
    authService = new BpmsAuthServiceImpl(bpmsRestClient);
  }

  @Test
  public void shouldCleanAuthorizations() {
    AuthConfigDto authConfigDto1 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_1", List.of("officer", "citizen")),
            new ProcessDefinitionAuthConfigDto("business-process", List.of("officer")))
        ));
    AuthConfigDto authConfigDto2 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_2", List.of("citizen"))
        )));
    when(bpmsRestClient.searchAuthorizationsByParams(bpmsUrl, jwtToken,
        PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS))
        .thenReturn(List.of(new AuthResponseDto("authId1", "citizen"),
            new AuthResponseDto("authId2", "officer"),
            new AuthResponseDto("authId33", "camunda-admin")));
    when(bpmsRestClient.searchAuthorizationsByParams(bpmsUrl, jwtToken,
        PROCESS_INSTANCE_AUTH_SEARCH_QUERY_PARAMS))
        .thenReturn(List.of(new AuthResponseDto("authId3", "citizen"),
            new AuthResponseDto("authId4", "officer"),
            new AuthResponseDto("authId44", "camunda-admin")));

    //when
    authService.cleanAuthorizations(bpmsUrl, jwtToken, List.of(authConfigDto1, authConfigDto2));

    verify(bpmsRestClient).searchAuthorizationsByParams(bpmsUrl, jwtToken, PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS);
    verify(bpmsRestClient).searchAuthorizationsByParams(bpmsUrl, jwtToken, PROCESS_INSTANCE_AUTH_SEARCH_QUERY_PARAMS);

    verify(bpmsRestClient).deleteAuthorization(bpmsUrl, jwtToken, "authId1");
    verify(bpmsRestClient).deleteAuthorization(bpmsUrl, jwtToken, "authId2");
    verify(bpmsRestClient).deleteAuthorization(bpmsUrl, jwtToken, "authId3");
    verify(bpmsRestClient).deleteAuthorization(bpmsUrl, jwtToken, "authId4");
  }

  @Test
  public void shouldCreateAuthorization() {
    AuthConfigDto authConfigDto1 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_1", List.of("officer", "citizen")),
            new ProcessDefinitionAuthConfigDto("business-process", List.of("officer")))
        ));
    AuthConfigDto authConfigDto2 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_2", List.of("citizen"))
        )));

    //when
    authService.createAuthorizations(bpmsUrl, jwtToken, List.of(authConfigDto1, authConfigDto2));

    AuthorizationCreateDto pdAuthCreateDto = buildCreateDto(
        Resource.PROCESS_DEFINITION,
        Arrays.asList(Permission.CREATE_INSTANCE.name(), Permission.READ.name()), AUTH_TYPE_GRANT,
        "Process_1", "officer");
    AuthorizationCreateDto pdAuthCreateDto2 = buildCreateDto(
        Resource.PROCESS_DEFINITION,
        Arrays.asList(Permission.CREATE_INSTANCE.name(), Permission.READ.name()), AUTH_TYPE_GRANT,
        "Process_1", "citizen");
    AuthorizationCreateDto pdAuthCreateDto3 = buildCreateDto(
        Resource.PROCESS_DEFINITION,
        Arrays.asList(Permission.CREATE_INSTANCE.name(), Permission.READ.name()), AUTH_TYPE_GRANT,
        "business-process", "officer");

    AuthorizationCreateDto piAuthCreateDto = buildCreateDto(
        Resource.PROCESS_INSTANCE, Collections.singletonList(Permission.CREATE.name()),
        AUTH_TYPE_GRANT, "*", "officer");
    AuthorizationCreateDto piAuthCreateDto2 = buildCreateDto(
        Resource.PROCESS_INSTANCE, Collections.singletonList(Permission.CREATE.name()),
        AUTH_TYPE_GRANT, "*", "citizen");

    //verify process definition authorizations creation
    verify(bpmsRestClient).createAuthorization(bpmsUrl, jwtToken, pdAuthCreateDto);
    verify(bpmsRestClient).createAuthorization(bpmsUrl, jwtToken, pdAuthCreateDto2);
    verify(bpmsRestClient).createAuthorization(bpmsUrl, jwtToken, pdAuthCreateDto3);
    //verify process instance authorizations creation
    verify(bpmsRestClient).createAuthorization(bpmsUrl, jwtToken, piAuthCreateDto);
    verify(bpmsRestClient).createAuthorization(bpmsUrl, jwtToken, piAuthCreateDto2);
  }
}