package com.aflorzy.jeopardy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Round {

    private String title;
    private Set<Category> categories;
    private Set<Score> finalScores;
    private Set<Clue> clues;
}
