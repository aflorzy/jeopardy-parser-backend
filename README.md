# Jeopardy-Backend

API for fetching and parsing Jeopardy! game data from J! Archive.

Parses HTML using Jsoup and constructs POJOs with the extracted data.

GET endpoints are available to get seasons (random, by ID), games (random, by ID), and categories (random).

See `src/main/java/com/aflorzy/jeopardy/domain` to view object relationships.

## To Use

Ensure Java 11 is installed on your system. Clone this repository by running `git clone https://github.com/aflorzy/jeopardy-parser-backend.git` in a terminal/command prompt. Open the directory in an IDE and run the Java application. The application will run on port 8081 by default. Adjust the port in application.propoerties.
Endpoints are located at `http://localhost:8081/api` by default.
Each call to J! Archive is prefixed with a 250ms delay to space out http requests and reduce stress on their servers.

## License
This software is released under the MIT License. See the LICENSE.md file for more information.

## Disclaimer

This software is not affiliated with J! Archive or Jeopardy Productions, Inc. and is intended solely to provide JSON data of J! Archive data to be used for further Javascript manipulation. An example of this manipulation is my interactive Jeopardy! board replica in [this repository](https://github.com/aflorzy/jeopardy-ui).
