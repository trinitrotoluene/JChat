package co.uk.michallet.chatapp.bot.dod;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * A singleton class responsible for discovering and loading map files.
 */
public final class MapLoader {
    private static MapLoader _instance = new MapLoader();

    public static MapLoader getInstance() {
        return _instance;
    }

    private MapLoader() {
    }

    /**
     * Get all map files in a given path relative to the one the program was run from.
     * @param path The relative path to search in.
     * @return All valid readable text files.
     * @throws NullPointerException When no valid files were found.
     */
    public File[] getMapFilesIn(String path) throws NullPointerException {
        var folder = new File("./" + path);
        // Filter for all top-level text files in the path.
        var files = folder.listFiles(f -> {
            var index = f.getName().lastIndexOf(".");
            // Account for files with no extension
            if (index == -1)
                return false;
            // Return if it's a readable .txt file.
            return f.isFile()
                    && f.canRead()
                    && f.getName().substring(index + 1)
                        .equalsIgnoreCase("txt");
        });

        return files;
    }

    /**
     * Loads and parses a map from file.
     * @param path Path of the file to load the map data from.
     * @throws MapParseException Thrown when the format of the map file is invalid.
     * @return A valid map initialised from the provided file data.
     */
    public GameMap loadMap(File path) throws MapParseException {
        var builder = new GameMapBuilder();

        try {
            var file = new RandomAccessFile(path, "r");

            String line;
            var lineList = new ArrayList<String[]>();
            while ((line = file.readLine()) != null)
                lineList.add(line.split(" "));

            // Height is the number of lines minus the two metadata lines. (From sample map format)
            int mapY = lineList.size() - 2;
            // Filter out all lines with multiple "words" in them to drop the metadata lines, leaving only map lines.
            // Since they're the same length, width is just one of their lengths.
            int mapX = lineList.stream()
                    .filter(s -> s.length == 1)
                    .findFirst()
                    .orElse(new String[0])[0]
                    .length();
            // Check for bad dimensions or something else going horribly wrong.
            if (mapY == 0 || mapX == 0) throw new MapParseException(String.format("Failed to parse map dimensions. Got values %s x %s",
                    mapX, mapY));

            var mapArray = new Tile[mapX][mapY];
            // Here we do the heavy lifting on parsing this thing :)
            fillTiles(builder, lineList, mapArray);

            file.close();

            builder.withTiles(mapArray);
            return builder.build();
        }
        catch (IOException ioex) {
            throw new MapParseException(String.format("There was an exception thrown while attempting to open the target map file.\r\n%s",
                    ioex.getMessage()));
        }
        catch (NumberFormatException numex) {
            throw new MapParseException("Could not parse an integer from the provided value for \"win\".");
        }
    }

    private void fillTiles(GameMapBuilder builder, ArrayList<String[]> lineList, Tile[][] mapArray) throws MapParseException {
        int mapY = 0;
        for (var tokens : lineList) {
            switch (tokens[0]) {
                case "name":
                    if (tokens.length < 2) throw new MapParseException("Bad format for map file value \"name\".");
                    var mapName = Arrays.stream(tokens).skip(1).collect(Collectors.joining(" "));
                    builder.withName(mapName);
                    break;
                case "win":
                    if (tokens.length != 2) throw new MapParseException("Bad format for map file value \"win\".");
                    var mapWinGold = Integer.parseInt(tokens[1]);
                    builder.withWinGold(mapWinGold);
                    break;
                default:
                    if (tokens.length != 1) throw new MapParseException("Bad format for map layout line: Illegal whitespace!");
                    parseMapLine(mapY++, tokens[0].toCharArray(), mapArray);
                    break;
            }
        }
    }

    private void parseMapLine(int yIndex, char[] line, Tile[][] map) {
        for (int x = 0; x < line.length; x++) {
            var tile = getTile(line[x]);
            map[x][yIndex] = tile;
        }
    }

    /**
     * Switch over the character and set the appropriate flags for the tile.
     * @param c The character to select a tile from.
     * @return The tile this character represents.
     */
    private Tile getTile(char c) {
        var tile = new Tile(EnumSet.noneOf(TileFlags.class));
        switch (c) {
            case 'P':
                tile.setFlag(TileFlags.PLAYER);
                break;
            case 'B':
                tile.setFlag(TileFlags.BOT);
                break;
            case '.':
                tile.setFlag(TileFlags.EMPTY);
                break;
            case 'G':
                tile.setFlag(TileFlags.EMPTY);
                tile.setFlag(TileFlags.GOLD);
                break;
            case 'E':
                tile.setFlag(TileFlags.EXIT);
                tile.setFlag(TileFlags.EMPTY);
                break;
            case '#':
                tile.setFlag(TileFlags.WALL);
                break;
        }

        return tile;
    }
}
