package com.aflorzy.jeopardy.controller;

import com.aflorzy.jeopardy.JeopardyApplication;
import com.aflorzy.jeopardy.domain.Category;
import com.aflorzy.jeopardy.domain.Game;
import com.aflorzy.jeopardy.domain.Season;
import com.aflorzy.jeopardy.service.GameService;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("api/")
@Getter
@Setter
@AllArgsConstructor
public class GameController {

    private GameService gameService;

    /*
        Episodes are numbered 1-7694 at the time of this writing
        54 episodes are missing, which only 0.702%. 99.3% are in order
        Can select a random episode between [1, 7694+] with ~99.3% accuracy
        Would have to request again with another number if the request fails
     */

    @GetMapping("game")
    public Game parseGame(@RequestParam int gameId) {
        return gameService.getGame(gameId);
    }

    @GetMapping("season/{type}")
    public List<String> getSeasonIDs(@PathVariable String type) {
        return gameService.getAllSeasonIDs(type);
    }

    @GetMapping("season/latest")
    public Season getLatestSeason() {
        List<String> seasonList = gameService.getAllSeasonIDs("latest");
        return gameService.getSeason(seasonList.get(0));
    }

    @GetMapping("season/previous")
    public Season getPreviousSeason() {
        List<String> seasonList = gameService.getAllSeasonIDs("previous");
        return gameService.getSeason(seasonList.get(0));
    }

    @GetMapping("season")
    public Season getSeason(@RequestParam String seasonId) {
        return gameService.getSeason(seasonId);
    }

    /*
    Get season list
    Get random season from that list
    Get random game from that season
    Return that game
     */
    @GetMapping("game/random")
    public Game getRandomGame() {
        return gameService.getRandomGame();
    }

    /*
    Get random game
    Get random round (Jeopardy/Double Jeopardy)
    Get random category from that round
     */
    @GetMapping("category/random")
    public Category getRandomCategory() {
        return gameService.getRandomCategory();
    }

}
