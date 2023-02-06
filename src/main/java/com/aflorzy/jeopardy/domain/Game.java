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
public class Game {

    private int gameId;
    private String title;
    private List<Contestant> contestants;
    private Round jeopardy;
    private Round doubleJeopardy;
    private Final finalJeopardy;

}
