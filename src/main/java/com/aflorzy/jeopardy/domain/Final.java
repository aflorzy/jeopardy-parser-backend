package com.aflorzy.jeopardy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Final {

    private String title;

    private String categoryName;
    private String categoryComments;
    private String clueText;
    private String correctResponse;
    private String tapeDate;

    private List<Score> finalScores;
}
