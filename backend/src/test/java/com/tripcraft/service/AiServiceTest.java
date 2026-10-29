package com.tripcraft.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiServiceTest {

  private JdbcTemplate db;
  private PlanBuilderService planBuilderService;
  private ObjectMapper mapper;
  private AiService aiService;

  @BeforeEach
  void setUp() {
    db = mock(JdbcTemplate.class);
    planBuilderService = mock(PlanBuilderService.class);
    mapper = new ObjectMapper();
    aiService = new AiService(db, planBuilderService, mapper, null, null, null);
  }

  @Test
  void fallbackBudgetPlanWithSufficientParametersGeneratesTieredPlans() {
    List<Map<String, Object>> mockPlans = List.of(
        new HashMap<>(Map.of("id", 1L, "title", "Coorg Serenity", "destinationName", "Coorg", "price", new BigDecimal("5000"), "durationDays", 3)),
        new HashMap<>(Map.of("id", 2L, "title", "Ooty Escapes", "destinationName", "Ooty", "price", new BigDecimal("7000"), "durationDays", 3)),
        new HashMap<>(Map.of("id", 3L, "title", "Mysore Heritage", "destinationName", "Mysore", "price", new BigDecimal("9000"), "durationDays", 3))
    );
    when(planBuilderService.build(any(), anyInt(), anyInt(), any())).thenReturn(mockPlans);

    Map<String, Object> request = Map.of("prompt", "Budget Rs 25,000 for 4 people, 3 days, near Coorg");
    Map<String, Object> result = aiService.budget(request, 1L);

    assertThat(result.get("provider")).isEqualTo("rule-based");
    List<?> plans = (List<?>) result.get("plans");
    assertThat(plans).hasSize(3);

    Map<?, ?> firstPlan = (Map<?, ?>) plans.get(0);
    assertThat(firstPlan.get("tier")).isEqualTo("Budget");
    assertThat(firstPlan.get("explanation")).isNotNull();
  }

  @Test
  void incompletePromptTriggersFollowUpQuestion() {
    Map<String, Object> request = Map.of("prompt", "I want to go to a beach");
    Map<String, Object> result = aiService.budget(request, null);

    assertThat(result).containsKey("followUpQuestion");
    assertThat(result.get("followUpQuestion").toString()).contains("budget");
  }
}
