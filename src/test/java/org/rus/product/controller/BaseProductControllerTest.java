package org.rus.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.rus.product.dto.CreateProductRequest;
import org.rus.product.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseProductControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected UUID createTestProduct(String name, double price, Integer count, Double discount) throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name(name)
                .price(price)
                .count(count)
                .discount(discount)
                .build();

        String responseJson = mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponse response = objectMapper.readValue(responseJson, ProductResponse.class);
        return response.getId();
    }

    protected UUID createFullTestProduct(CreateProductRequest request) throws Exception {
        String responseJson = mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponse response = objectMapper.readValue(responseJson, ProductResponse.class);
        return response.getId();
    }

}