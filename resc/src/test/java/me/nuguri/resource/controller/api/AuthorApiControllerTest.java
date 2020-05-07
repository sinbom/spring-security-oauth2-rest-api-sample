package me.nuguri.resource.controller.api;

import me.nuguri.resource.common.BaseIntegrationTest;
import org.junit.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorApiControllerTest extends BaseIntegrationTest {

    @Test
    public void queryAuthors_V1_Success_200() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void queryAuthors_V1_Unauthorized_401() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "invalid")
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "1")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void queryAuthors_V1_Invalid_400() throws Exception {
        mockMvc.perform(get("/api/v1/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .accept(MediaTypes.HAL_JSON)
                .queryParam("page", "01")
                .queryParam("size", "asd")
                .queryParam("sort", "zxc,nbv"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}