package com.aflorzy.jeopardy.controller;

import com.aflorzy.jeopardy.JeopardyApplication;
import com.aflorzy.jeopardy.domain.Game;
import com.aflorzy.jeopardy.domain.Season;
import com.aflorzy.jeopardy.service.GameService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/")
@Getter
@Setter
@AllArgsConstructor
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private GameService gameService;

    @GetMapping("game")
    public Game parseGame(@RequestParam String gameId) {
        try {
            Document doc = Jsoup.connect("https://j-archive.com/showgame.php?game_id=" + gameId).get();
            log(doc.title());
            Game game = new Game();
            game.setGameId(gameId);

            Element content = doc.getElementById("content");
            game.setTitle(content.getElementById("game_title").text());
            game.setContestants(gameService.getContestants(content));

//		Jeopardy! Round
            Element first = content.getElementById("jeopardy_round");
            game.setJeopardy(gameService.getRoundData(first));

//		Double Jeopardy! Round
            Element second = content.getElementById("double_jeopardy_round");
            game.setDoubleJeopardy(gameService.getRoundData(second));

//		Final Jeopardy! Round
            Element last = content.getElementById("final_jeopardy_round");
            game.setFinalJeopardy(gameService.getFinalData(last));

            log(game.toString());
            return game;
        } catch (IOException e) {
            logger.error("Could not parse game with id " + gameId);
            return null;
        }
    }

    @GetMapping("season")
    public Season getGameIds(@RequestParam int seasonNumber) {
        try {
//            List<Season> seasonList = new ArrayList<>();
//            for(int i = 1; i <= 5; i++) {
                Document doc = Jsoup.connect("https://j-archive.com/showseason.php?season=" + seasonNumber).get();
                Element content = doc.getElementById("content");
                Elements rows = content.getElementsByTag("tr");
                Set<Integer> gameIDs = new HashSet<>();
                for (Element row : rows) {
                    String gameURL = row.getElementsByTag("a").attr("href");
                    String gameIdKey = "game_id=";
                    int gameId = Integer.parseInt(gameURL.substring(gameURL.indexOf(gameIdKey) + gameIdKey.length()));
                    gameIDs.add(gameId);
                }
//                seasonList.add(new Season(i, gameIDs));
//                Thread.sleep(2000);
//            }
            return new Season(seasonNumber, gameIDs);
        } catch(IOException e) {
            logger.error("Could not parse season " + seasonNumber);
            return null;
        }
//        catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException(e);
//        }
    }

    public static void log(String message) {
        logger.info(message);
    }
}
