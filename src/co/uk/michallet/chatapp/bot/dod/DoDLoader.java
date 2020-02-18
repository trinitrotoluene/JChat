package co.uk.michallet.chatapp.bot.dod;

import java.io.File;

public class DoDLoader {
    public void run(String[] args) {
        UI.motd();

        try {
            var map = getAndLoadMapFile();

            var players = new PlayerBase[2];
            // Get valid random starting positions for two players
            var playerPos = MapUtil.getLegalStartingPoint(map);
            players[0] = new Player(playerPos);
            var botPos = MapUtil.getLegalStartingPoint(map, playerPos);
            players[1] = new BotPlayer(botPos, map);

            var game = new Game(map, players);

            game.run();

            if (game.isWon()) {
                UI.write("WIN");
                UI.win();
            }
            else {
                UI.write("LOSE");
                UI.lose();
            }
        }
        catch (Exception mapEx) {
            UI.error(mapEx);
        }
    }

    private GameMap getAndLoadMapFile() throws MapParseException {
        var mapFiles = MapLoader.getInstance().getMapFilesIn("maps");

        printMapFiles(mapFiles);

        System.out.print("Map number: ");
        int selectedId;
        while (true) {
            selectedId = UI.getScanner().nextInt();
            UI.getScanner().nextLine();

            if (selectedId <= mapFiles.length && selectedId > 0) break;

            UI.cls();
            UI.motd();
            printMapFiles(mapFiles);
            System.out.println("Please select a valid number.");
            System.out.print("Map number: ");
        }

        return MapLoader.getInstance().loadMap(mapFiles[selectedId - 1]);
    }

    private void printMapFiles(File[] mapFiles) {
        System.out.println("Please select a map file to load from (./maps): ");

        for (int i = 0; i < mapFiles.length; i++) {
            System.out.println(String.format("%s) %s", i + 1, mapFiles[i].getName()));
        }
    }


}
