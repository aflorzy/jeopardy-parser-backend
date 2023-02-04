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

	public static void main(String[] args) throws IOException {
		SpringApplication.run(JeopardyApplication.class, args);
	}
}
