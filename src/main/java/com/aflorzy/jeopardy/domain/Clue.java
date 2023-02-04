package com.aflorzy.jeopardy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Clue {

    private String clueValue;
    private String clueOrderNumber;
    private String clueText;
    private String correctResponse;
    private int boardPositionX;
    private int boardPositionY;
}
