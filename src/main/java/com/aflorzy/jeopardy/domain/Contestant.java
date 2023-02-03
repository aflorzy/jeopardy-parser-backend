package com.aflorzy.jeopardy.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Contestant {

    private String playerFullName;
    private String playerOccupationAndOrigin;
    private String playerpicture;
    private String playerId;
}
