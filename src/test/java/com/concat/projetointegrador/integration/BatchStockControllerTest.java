package com.concat.projetointegrador.integration;

import com.concat.projetointegrador.dto.BatchStockOrdinationDTO;
import com.concat.projetointegrador.dto.InboundOrderDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/batchstock-test.sql"})
public class BatchStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenNumberOfDaysAndSectorId_whenFilterBatchStock_thenReturnMatchBatchStocks() throws Exception {
        List batchStockResponseDTOList = setupMvc();
        assertEquals(3, batchStockResponseDTOList.size());
    }

    @Test
    public void givenCategoryType_whenFilterBatchStock_thenReturnMatchBatchStocks() throws Exception {
        MvcResult response = mockMvc.perform(get("/batchstock/duedate")
                .with(
                        user("Supervisor")
                                .password("123")
                )
                .param("days", "40")
                .param("sectorId", "1")
                .param("category", "congelados")
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String json = response.getResponse().getContentAsString();
        List batchStockResponseDTOList = objectMapper.readValue(json, List.class);

        assertEquals(3, batchStockResponseDTOList.size());
    }

    @Test
    public void givenSortAsAsc_whenFilterBatchStock_thenReturnBatchStockByDueDateAsc() throws Exception {
        List batchStockResponseDTOList = setupMvc();
        List<LocalDate> dueDateList = new ArrayList<>();
        LocalDate firstDate = LocalDate.of(2022, 5, 10);

        batchStockResponseDTOList.forEach(linkedhasmap -> {
            LocalDate dueDate = LocalDate.parse((CharSequence) ((LinkedHashMap) linkedhasmap).get("dueDate"));
            dueDateList.add(dueDate);
        });

        for (int i = 0; i < dueDateList.size(); i++) {
            assertEquals(firstDate.plusDays(i), dueDateList.get(i));
        }
    }

    @Test
    public void shouldReturnTheProductsByMoreSold() throws Exception {
        String payload = "[{\"quantity\":0,\"product\":{\"name\":\"frango\",\"volume\":1,\"price\":20.00,\"category\":\"CONGELADOS\"}},{\"quantity\":0,\"product\":{\"name\":\"frango\",\"volume\":1,\"price\":20.00,\"category\":\"CONGELADOS\"}},{\"quantity\":0,\"product\":{\"name\":\"frango\",\"volume\":1,\"price\":20.00,\"category\":\"CONGELADOS\"}}]";

        MvcResult response = mockMvc.perform(get("/batchstock/stock")
                .with(
                        user("Supervisor")
                                .password("123")
                )
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        String jsonReturned = response.getResponse().getContentAsString();

        assertEquals(payload, jsonReturned);
    }

    @Test
    public void givenSortAsDesc_whenFilterBatchStock_thenReturnBatchStockByDueDateDesc() throws Exception {
        MvcResult response = mockMvc.perform(get("/batchstock/duedate")
                        .with(
                                user("Supervisor")
                                        .password("123")
                        )
                        .param("days", "40")
                        .param("sectorId", "1")
                        .param("asc", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String json = response.getResponse().getContentAsString();
        List batchStockResponseDTOList = objectMapper.readValue(json, List.class);
        List<LocalDate> dueDateList = new ArrayList<>();
        LocalDate firstDate = LocalDate.of(2022, 5, 12);

        batchStockResponseDTOList.forEach(linkedhasmap -> {
            LocalDate dueDate = LocalDate.parse((CharSequence) ((LinkedHashMap) linkedhasmap).get("dueDate"));
            dueDateList.add(dueDate);
        });

        for (int i = 0; i < dueDateList.size(); i++) {
            assertEquals(firstDate.minusDays(i), dueDateList.get(i));
        }
    }

    private List setupMvc() throws Exception {
        MvcResult response = mockMvc.perform(get("/batchstock/duedate")
                        .with(
                                user("Supervisor")
                                        .password("123")
                        )
                        .param("days", "40")
                        .param("sectorId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String json = response.getResponse().getContentAsString();

        return objectMapper.readValue(json, List.class);
    }
}
