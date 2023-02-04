package com.aflorzy.jeopardy.service;

import com.aflorzy.jeopardy.domain.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {

    public Set<Contestant> getContestants(Element element) {
        Elements contestantsElem = element.getElementsByClass("contestants");
        Set<Contestant> contestants = new HashSet<>();
        for(Element contestantElem : contestantsElem) {
            Contestant contestant = new Contestant();
            contestant.setPlayerId(contestantElem.getElementsByTag("a").attr("href").split("=")[1]);
            contestant.setPlayerFullName(contestantElem.getElementsByTag("a").text());
            contestant.setPlayerOccupationAndOrigin(contestantElem.ownText());
            contestants.add(contestant);
        }
        return contestants;
    }

    public Set<Clue> getClues(Element round) {
        Elements cluesElem = round.getElementsByClass("clue");
        Set<Clue> clues = new HashSet<>();
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
            clue.setClueText(clueElem.getElementsByClass("clue_text").text());

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
//            Could not find clueStr
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

    public Set<Score> getRoundScores(Element round) {
        Elements scorePlayerNicknamesElem = round.getElementsByClass("score_player_nickname");
        Elements scorePositiveElem = round.getElementsByClass("score_positive");
        Elements scoreRemarksElem = round.getElementsByClass("score_remarks");

        Set<Score> finalScores = new HashSet<>();
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
        round.setCategories(secondCategories);
        round.setFinalScores(getRoundScores(roundElem));
        round.setClues(getClues(roundElem));
        return round;
    }

    public Final getFinalData(Element finalElem) {
        Elements lastCategoryElem = finalElem.getElementsByClass("category");
        Final finalRound = new Final();
        finalRound.setTitle(finalElem.getElementsByTag("h2").text());
        finalRound.setClueText(finalElem.getElementById("clue_FJ").text());
        for(Element categoryElem : lastCategoryElem) {
            finalRound.setCategoryName(categoryElem.getElementsByClass("category_name").text());
            finalRound.setCategoryComments(categoryElem.getElementsByClass("category_comments").text());
            finalRound.setCorrectResponse(getCorrectResponse(categoryElem));
        }
        finalRound.setFinalScores(getRoundScores(finalElem));

        return finalRound;
    }

}
