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
public class Game {

    private String gameId;
    private String title;
    private Set<Contestant> contestants;
    private Round jeopardy;
    private Round doubleJeopardy;
    private Final finalJeopardy;

}
