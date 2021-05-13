package com.example.PoloniexTrader.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Currency {
  private long id;
  private String name;
  private int disabled;
  private int delisted;
  private int frozen;
}