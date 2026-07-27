package dev.youthpolicy.api;

import dev.youthpolicy.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /verdicts 실제 요청/응답 왕복 (contracts/openapi.yaml). */
@WebMvcTest(VerdictsController.class)
@Import(AppConfig.class)
class VerdictsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void eligibleWhenAllAtomsSatisfied() throws Exception {
        String body = """
                {
                  "policy_id": "jutaek-dream",
                  "answers": {
                    "age": { "known": true, "value": 28 },
                    "housing_none": { "known": true, "value": true },
                    "income_self_monthly_krw": { "known": true, "approx": false, "value": 3000000 }
                  }
                }
                """;

        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policy_id").value("jutaek-dream"))
                .andExpect(jsonPath("$.verdict.state").value("eligible"))
                .andExpect(jsonPath("$.verdict.unknown_reasons").doesNotExist())
                .andExpect(jsonPath("$.reasoning.length()").value(3))
                .andExpect(jsonPath("$.application.url").exists());
    }

    @Test
    void ineligibleWhenAgeOutOfRange_applicationOmitted() throws Exception {
        String body = """
                {
                  "policy_id": "jutaek-dream",
                  "answers": {
                    "age": { "known": true, "value": 40 },
                    "housing_none": { "known": true, "value": true },
                    "income_self_monthly_krw": { "known": true, "approx": false, "value": 3000000 }
                  }
                }
                """;

        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict.state").value("ineligible"))
                .andExpect(jsonPath("$.application").doesNotExist());
    }

    @Test
    void needsReviewWhenIncomeIsApprox() throws Exception {
        String body = """
                {
                  "policy_id": "jutaek-dream",
                  "answers": {
                    "age": { "known": true, "value": 28 },
                    "housing_none": { "known": true, "value": true },
                    "income_self_monthly_krw": { "known": true, "approx": true, "value": 3000000 }
                  }
                }
                """;

        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict.state").value("needs_review"))
                .andExpect(jsonPath("$.verdict.unknown_reasons[0]").value("input_uncertain"))
                .andExpect(jsonPath("$.application").exists());
    }

    @Test
    void badRequestWhenPolicyIdUnknown() throws Exception {
        String body = """
                {
                  "policy_id": "not-a-real-policy",
                  "answers": { }
                }
                """;

        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void badRequestWhenAnswersMissing() throws Exception {
        String body = """
                { "policy_id": "jutaek-dream" }
                """;

        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void badRequestWhenBodyMalformed() throws Exception {
        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content("not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void badRequestWhenUnknownTopLevelField() throws Exception {
        String body = """
                {
                  "policy_id": "jutaek-dream",
                  "answers": { },
                  "unexpected_field": true
                }
                """;

        mockMvc.perform(post("/verdicts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
