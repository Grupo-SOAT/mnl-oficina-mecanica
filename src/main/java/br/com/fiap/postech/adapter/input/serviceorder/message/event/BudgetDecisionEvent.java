package br.com.fiap.postech.adapter.input.serviceorder.message.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDecisionEvent {

    @JsonProperty("serviceOrderId")
    private Long serviceOrderId;

    @JsonProperty("decision")
    private String decision;
}
