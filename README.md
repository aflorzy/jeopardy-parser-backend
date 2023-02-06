# Jeopardy-Backend

API for fetching and parsing Jeopardy! game data from J! Archive.

Parses HTML using Jsoup and constructs POJOs with the extracted data.

GET endpoints are available to get seasons (random, by ID), games (random, by ID), and categories (random).

See `src/main/java/com/aflorzy/jeopardy/domain` to view object relationships.

## To Use

Ensure Java 11 is installed on your system. Clone this repository by running `git clone https://github.com/aflorzy/jeopardy-parser-backend.git` in a terminal/command prompt. Open the directory in an IDE and run the Java application. The application will run on port 8081 by default. Adjust the port in application.propoerties.
Endpoints are located at `http://localhost:8081/api` by default.