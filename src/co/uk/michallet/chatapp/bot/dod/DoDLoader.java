package co.uk.michallet.chatapp.bot.dod;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DoDLoader {
    public Game run() {
        UI.motd();

        try {
            var map = getAndLoadMapFile();

            var players = new ConcurrentLinkedQueue<PlayerBase>();
            var botPos = MapUtil.getLegalStartingPoint(map);
            players.add(new BotPlayer(botPos, map));

            var game = new Game(map, players);

            return game;
        }
        catch (Exception mapEx) {
            UI.error(mapEx);
        }

        return null;
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
