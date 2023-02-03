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
public class Category {

    private String categoryName;
    private String categoryComments;
    private Set<Clue> clues;
}
