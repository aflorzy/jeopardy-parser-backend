package com.aflorzy.jeopardy;

import com.aflorzy.jeopardy.domain.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class JeopardyApplication {

	private static final Logger logger = LoggerFactory.getLogger(JeopardyApplication.class);


	public static void main(String[] args) throws IOException {
		SpringApplication.run(JeopardyApplication.class, args);

		Document doc = Jsoup.connect("https://j-archive.com/showgame.php?game_id=7689").get();
		log(doc.title());
		Game game = new Game();

		Element content = doc.getElementById("content");
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

		log(game.toString());

	}

	public static Set<Contestant> getContestants(Element element) {
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

	public static Set<Clue> getClues(Element round) {
		Elements cluesElem = round.getElementsByClass("clue");
		Set<Clue> clues = new HashSet<>();
		for(Element clueElem : cluesElem) {
			Clue clue = new Clue();
			clue.setCorrectResponse(getCorrectResponse(clueElem));
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

	public static String getCorrectResponse(Element elem) {
		String onmouseover = elem.getElementsByTag("div").attr("onmouseover");
		String correctResponseKey = "class=\"correct_response\">";
		int indexOfCorrectResponse = onmouseover.indexOf(correctResponseKey) +  correctResponseKey.length();
		String emKey = "</em>";
		int indexOfEm = onmouseover.indexOf(emKey);
		return onmouseover.substring(indexOfCorrectResponse, indexOfEm );
	}

	public static Set<Score> getRoundScores(Element round) {
		Elements scorePlayerNicknamesElem = round.getElementsByClass("score_player_nickname");
		Elements scorePositiveElem = round.getElementsByClass("score_positive");
		Elements scoreRemarksElem = round.getElementsByClass("score_remarks");

		Set<Score> finalScores = new HashSet<>();
		if (scorePlayerNicknamesElem.size() >= 3 && scorePositiveElem.size() >= 3 && scoreRemarksElem.size() >= 3) {
			for (int i = 0; i < 3; i++) {
				Score score = new Score();
				score.setScorePlayerNickname(scorePlayerNicknamesElem.get(i).text());
				score.setScorePositive(scorePositiveElem.get(i).text());
				score.setScoreRemarks(scoreRemarksElem.get(i).text());
				finalScores.add(score);
			}
		}
		return finalScores;
	}

	public static Round getRoundData(Element roundElem) {
		Elements categoriesElem = roundElem.getElementsByClass("category");
		Round round = new Round();
		round.setTitle(roundElem.getElementsByTag("h2").text());
		Set<Category> secondCategories = new HashSet<>();
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

	public static Final getFinalData(Element finalElem) {
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

	public static void log(String message) {
//		logger.info(message);
		System.out.println(message);
	}

}
