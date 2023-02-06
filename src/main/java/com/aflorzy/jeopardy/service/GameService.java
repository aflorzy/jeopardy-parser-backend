package com.aflorzy.jeopardy.service;

import com.aflorzy.jeopardy.controller.GameController;
import com.aflorzy.jeopardy.domain.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final String BASE_URL = "https://j-archive.com/";
    private static final Random generator = new Random(System.currentTimeMillis());


    public Game getRandomGame() {
        List<String> seasonList = getAllSeasonIDs("all");
        String seasonId = seasonList.get((int) (Math.floor(generator.nextDouble() * seasonList.size())));
        Season season = getSeason(seasonId);
        return getGame(season.getGameIDs().get((int) (Math.floor(generator.nextDouble() * season.getGameIDs().size()))));
    }

    public Category getRandomCategory() {
        Game game = getRandomGame();
//        1: Jeopardy
//        2: Double Jeopardy
        int roundId = (int) (Math.floor(generator.nextDouble() * 2));

        List<Category> categories;
        if (roundId == 1) {
            categories = game.getJeopardy().getCategories();
        } else {
            categories = game.getDoubleJeopardy().getCategories();
        }
        return categories.get((int) (Math.floor(generator.nextDouble() * categories.size())));
    }

    public Game getGame(int gameId) {
        try {
            Thread.sleep(250);
            Document doc = Jsoup.connect(BASE_URL + "showgame.php?game_id=" + gameId).get();
            Game game = new Game();
            game.setGameId(gameId);

            Element content = doc.getElementById("content");
            game.setTitle(content.getElementById("game_title").text());
            game.setContestants(getContestants(content));

//		Jeopardy! Round
            Element first = content.getElementById("jeopardy_round");
            game.setJeopardy(getRoundData(first));

//		Double Jeopardy! Round
            Element second = content.getElementById("double_jeopardy_round");
            game.setDoubleJeopardy(getRoundData(second));

//		Final Jeopardy! Round
            Element last = content.getElementById("final_jeopardy_round");
            game.setFinalJeopardy(getFinalData(last));

            return game;
        } catch (IOException | InterruptedException e) {
            logger.error("Could not parse game " + gameId);
            return null;
        }
    }

    public Season getSeason(String seasonId) {
        try {
            Thread.sleep(250);
            Document doc = Jsoup.connect(BASE_URL + "showseason.php?season=" + seasonId).get();
            Element content = doc.getElementById("content");
            Elements rows = content.getElementsByTag("tr");
            List<Integer> gameIDs = new ArrayList<>();
            for (Element row : rows) {
                String gameURL = row.getElementsByTag("a").attr("href");
                String gameIdKey = "game_id=";
                int gameId = Integer.parseInt(gameURL.substring(gameURL.indexOf(gameIdKey) + gameIdKey.length()));
                gameIDs.add(gameId);
            }
            return new Season(seasonId, gameIDs);
        } catch(IOException | InterruptedException e) {
            logger.error("Could not parse season " + seasonId);
            return null;
        }
    }

    /*
    Possible types:
    - Normal (1-39)
    - Special (Trebek pilots, Super J!, etc.)
    - All
    - Latest
    - Previous
     */
    public List<String> getAllSeasonIDs(String type) {
        List<String> types = Arrays.asList("all", "normal", "special", "latest", "previous");
        if (types.indexOf(type) < 0) {
//            Invalid type
            return null;
        }
        try {
            Thread.sleep(250);
            Document doc = Jsoup.connect(BASE_URL + "listseasons.php").get();
            Element content = doc.getElementById("content");

            Elements navbarItems = doc.getElementById("navbartext").getElementsByTag("a");
            if (type.equals("latest")) {
                String seasonURL = navbarItems.get(0).attr("href");
                String seasonKey = "?season=";
                int seasonIndex = seasonURL.indexOf(seasonKey) + seasonKey.length();
                String seasonId = seasonURL.substring(seasonIndex);
                return Arrays.asList(seasonId);
            } else if (type.equals("previous")) {
                String seasonURL = navbarItems.get(1).attr("href");
                String seasonKey = "?season=";
                int seasonIndex = seasonURL.indexOf(seasonKey) + seasonKey.length();
                String seasonId = seasonURL.substring(seasonIndex);
                return Arrays.asList(seasonId);
            } else {
                Elements seasons = content.getElementsByTag("tr");

                List<String> seasonIDs = new ArrayList<>();
                for(Element season: seasons) {
                    String seasonURL = season.children().first().getElementsByTag("a").attr("href");
                    String seasonKey = "?season=";
                    int seasonIndex = seasonURL.indexOf(seasonKey) + seasonKey.length();
                    String seasonId = seasonURL.substring(seasonIndex);

                    if (type.equals("all")) {
                        seasonIDs.add(seasonId);
                    } else if (type.equals("special")) {
                        int seasonIdNum = -1;
                        try {
                            seasonIdNum = Integer.parseInt(seasonId);
                        } catch (NumberFormatException e) {
    //                        Could not parse number
                        }
                        if (seasonIdNum < 0) {
    //                         Couldn't parse, must be special
                            seasonIDs.add(seasonId);
                        }
                    } else if (type.equals("normal")) {
                        int seasonIdNum = -1;
                        try {
                            seasonIdNum = Integer.parseInt(seasonId);
                        } catch (NumberFormatException e) {
    //                        Could not parse number
                        }
                        if (seasonIdNum >= 0) {
                            seasonIDs.add(seasonId);
                        }
                    }
                }
                return seasonIDs;
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Contestant> getContestants(Element element) {
        Elements contestantsElem = element.getElementsByClass("contestants");
        List<Contestant> contestants = new ArrayList<>();
        for(Element contestantElem : contestantsElem) {
            Contestant contestant = new Contestant();
            contestant.setPlayerId(contestantElem.getElementsByTag("a").attr("href").split("=")[1]);
            contestant.setPlayerFullName(contestantElem.getElementsByTag("a").text());
            contestant.setPlayerOccupationAndOrigin(contestantElem.ownText());
            contestants.add(contestant);
        }
        return contestants;
    }

    public List<Clue> getClues(Element round) {
        Elements cluesElem = round.getElementsByClass("clue");
        List<Clue> clues = new ArrayList<>();
        for(Element clueElem : cluesElem) {
            Clue clue = new Clue();
            clue.setCorrectResponse(getCorrectResponse(clueElem));
            clue = getClueBoardPosition(clue, clueElem);
            String dailyDouble =clueElem.getElementsByClass("clue_value_daily_double").text();
            if (!dailyDouble.equals("") && dailyDouble != null) {
                clue.setClueValue(dailyDouble);
            } else {
                clue.setClueValue(clueElem.getElementsByClass("clue_value").text());
            }
            clue.setClueOrderNumber(clueElem.getElementsByClass("clue_order_number").text());
            Element clueText = clueElem.getElementsByClass("clue_text").first();
            if (clueText != null) {
                clue.setClueMultimediaURL(clueText.getElementsByTag("a").attr("href"));
                clue.setClueText(clueText.text());
            }

            clues.add(clue);
        }
        return clues;
    }

    public String getCorrectResponse(Element elem) {
        String onmouseover = elem.getElementsByTag("div").attr("onmouseover");
        String correctResponseKey = "class=\"correct_response\">";
        int indexOfCorrectResponse = onmouseover.indexOf(correctResponseKey) +  correctResponseKey.length();
        String emKey = "</em>";
        int indexOfEm = onmouseover.indexOf(emKey);
        if (indexOfCorrectResponse < 0 || indexOfEm < 0) {
//            Could not find correctResponse
            return "";
        }
        return onmouseover.substring(indexOfCorrectResponse, indexOfEm );
    }

    public Clue getClueBoardPosition(Clue clue,Element elem) {
        int[] result = {-1, -1};
        String onmouseover = elem.getElementsByTag("div").attr("onmouseover");
        String clueKey = "toggle('clue_";
        String endKey = "', 'clue";
        int cluePosition = onmouseover.indexOf(clueKey) + clueKey.length();
        int endPosition = onmouseover.indexOf(endKey);
        if (cluePosition < 0 || endPosition < 0) {
            return clue;
        }
        String clueStr = onmouseover.substring(cluePosition, endPosition);
        String[] clueStrParsed = clueStr.split("_");

        if (clueStrParsed.length == 3) {
            result[0] = Integer.parseInt(clueStrParsed[1]);
            result[1] = Integer.parseInt(clueStrParsed[2]);
        }

        clue.setBoardPositionX(result[0]);
        clue.setBoardPositionY(result[1]);
        return clue;
    }

    public List<Score> getRoundScores(Element round) {
        Elements scorePlayerNicknamesElem = round.getElementsByClass("score_player_nickname");
        Elements scorePositiveElem = round.getElementsByClass("score_positive");
        Elements scoreRemarksElem = round.getElementsByClass("score_remarks");

        List<Score> finalScores = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Score score = new Score();
            if (scorePlayerNicknamesElem.size() >= 3) {
                score.setScorePlayerNickname(scorePlayerNicknamesElem.get(scorePlayerNicknamesElem.size() - 3 + i).text());
            }
            if (scoreRemarksElem.size() >= 3) {
                score.setScoreRemarks(scoreRemarksElem.get(scoreRemarksElem.size() - 3 + i).text());
            }
            if (scorePositiveElem.size() >= 3) {
                score.setScorePositive(scorePositiveElem.get(scorePositiveElem.size() - 3 + i).text());
            }
            finalScores.add(score);
        }
        return finalScores;
    }

    public Round getRoundData(Element roundElem) {
        Elements categoriesElem = roundElem.getElementsByClass("category");
        Round round = new Round();
        round.setTitle(roundElem.getElementsByTag("h2").text());
        List<Category> secondCategories = new ArrayList<>();
        for(Element categoryElem : categoriesElem) {
            Category category = new Category();
            category.setCategoryName(categoryElem.getElementsByClass("category_name").text());
            category.setCategoryComments(categoryElem.getElementsByClass("category_comments").text());
            secondCategories.add(category);
        }

        List<Clue> clues = getClues(roundElem);
        for(int i = 0; i < secondCategories.size(); i++) {
            int boardPositionX = i + 1;
            Category categoryTemp = secondCategories.get(i);
            List<Clue> clueListNew = new ArrayList<>();
            for(Clue clue : clues) {
                if (clue.getBoardPositionX() == boardPositionX) {
                    clueListNew.add(clue);
                }
            }
            categoryTemp.setClues(clueListNew);
            secondCategories.set(i, categoryTemp);
        }

        round.setFinalScores(getRoundScores(roundElem));
        round.setCategories(secondCategories);
        return round;
    }

    public Final getFinalData(Element finalElem) {
        Elements lastCategoryElem = finalElem.getElementsByClass("category");
        Final finalRound = new Final();
        finalRound.setTitle(finalElem.getElementsByTag("h2").text());
        finalRound.setClueText(finalElem.getElementById("clue_FJ").text());
        String tapeDate = finalElem.getElementsByTag("h6").text();
        String tapeDateStr = "Game tape date:";
        int tapeDateIndex = tapeDate.indexOf(tapeDateStr) + tapeDateStr.length();
        finalRound.setTapeDate(tapeDate.substring(tapeDateIndex));
        for(Element categoryElem : lastCategoryElem) {
            finalRound.setCategoryName(categoryElem.getElementsByClass("category_name").text());
            finalRound.setCategoryComments(categoryElem.getElementsByClass("category_comments").text());
            finalRound.setCorrectResponse(getCorrectResponse(categoryElem));
        }
        finalRound.setFinalScores(getRoundScores(finalElem));

        return finalRound;
    }

}
