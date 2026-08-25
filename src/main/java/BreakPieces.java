import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Solução do kata CodeWars "Break the pieces". */
public final class BreakPieces {
    private static final int SCALE = 3;
    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, -1, 1};

    private BreakPieces() {
    }

    /**
     * Decompõe um desenho ASCII nas regiões fechadas mínimas que o formam.
     *
     * @param shape desenho composto por '+', '-', '|', espaços e quebras de linha
     * @return uma peça por região fechada; nunca {@code null}
     */
    public static String[] process(String shape) {
        if (shape == null || shape.isEmpty()) {
            return new String[0];
        }

        String normalizedShape = shape.replace("\r", "");
        validateCharacters(normalizedShape);
        String[] lines = normalizedShape.split("\n", -1);
        int rows = lines.length;
        int columns = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        if (columns == 0) {
            return new String[0];
        }

        char[][] source = rectangularCopy(lines, rows, columns);
        boolean[][] wall = rasterize(source);
        int[][] regions = labelEmptyRegions(wall);

        List<String> pieces = new ArrayList<>();
        int regionCount = Arrays.stream(regions)
                .flatMapToInt(Arrays::stream)
                .max()
                .orElse(0);

        for (int region = 1; region <= regionCount; region++) {
            String piece = renderBoundary(source, regions, region);
            if (!piece.isEmpty()) {
                pieces.add(piece);
            }
        }
        return pieces.toArray(String[]::new);
    }

    private static void validateCharacters(String shape) {
        for (int index = 0; index < shape.length(); index++) {
            char symbol = shape.charAt(index);
            if (symbol != '+' && symbol != '-' && symbol != '|'
                    && symbol != ' ' && symbol != '\n') {
                throw new IllegalArgumentException(
                        "Caractere inválido na posição " + index + ": '" + symbol + "'");
            }
        }
    }

    private static char[][] rectangularCopy(String[] lines, int rows, int columns) {
        char[][] source = new char[rows][columns];
        for (char[] row : source) {
            Arrays.fill(row, ' ');
        }
        for (int row = 0; row < rows; row++) {
            lines[row].getChars(0, lines[row].length(), source[row], 0);
        }
        return source;
    }

    private static boolean[][] rasterize(char[][] source) {
        int height = source.length * SCALE + 2;
        int width = source[0].length * SCALE + 2;
        boolean[][] wall = new boolean[height][width];

        for (int row = 0; row < source.length; row++) {
            for (int column = 0; column < source[row].length; column++) {
                int y = row * SCALE + 2;
                int x = column * SCALE + 2;
                char symbol = source[row][column];
                if (symbol != ' ') {
                    wall[y][x] = true;
                }
                if (symbol == '-') {
                    wall[y][x - 1] = wall[y][x + 1] = true;
                } else if (symbol == '|') {
                    wall[y - 1][x] = wall[y + 1][x] = true;
                } else if (symbol == '+') {
                    wall[y][x - 1] = connectsHorizontally(source, row, column - 1);
                    wall[y][x + 1] = connectsHorizontally(source, row, column + 1);
                    wall[y - 1][x] = connectsVertically(source, row - 1, column);
                    wall[y + 1][x] = connectsVertically(source, row + 1, column);
                }
            }
        }
        return wall;
    }

    private static int[][] labelEmptyRegions(boolean[][] wall) {
        int[][] regions = new int[wall.length][wall[0].length];
        boolean[][] visited = new boolean[wall.length][wall[0].length];
        floodFill(wall, visited, regions, 0, 0, -1); // região externa

        int label = 0;
        for (int row = 0; row < wall.length; row++) {
            for (int column = 0; column < wall[row].length; column++) {
                if (!wall[row][column] && !visited[row][column]) {
                    floodFill(wall, visited, regions, row, column, ++label);
                }
            }
        }
        return regions;
    }

    private static void floodFill(boolean[][] wall, boolean[][] visited, int[][] regions,
                                  int startRow, int startColumn, int label) {
        Deque<int[]> pending = new ArrayDeque<>();
        pending.add(new int[]{startRow, startColumn});
        visited[startRow][startColumn] = true;
        regions[startRow][startColumn] = label;

        while (!pending.isEmpty()) {
            int[] point = pending.removeFirst();
            for (int direction = 0; direction < DR.length; direction++) {
                int row = point[0] + DR[direction];
                int column = point[1] + DC[direction];
                if (row >= 0 && row < wall.length && column >= 0 && column < wall[0].length
                        && !wall[row][column] && !visited[row][column]) {
                    visited[row][column] = true;
                    regions[row][column] = label;
                    pending.addLast(new int[]{row, column});
                }
            }
        }
    }

    private static String renderBoundary(char[][] source, int[][] regions, int region) {
        char[][] output = new char[source.length][source[0].length];
        for (char[] row : output) {
            Arrays.fill(row, ' ');
        }

        int minRow = source.length;
        int maxRow = -1;
        int minColumn = source[0].length;
        int maxColumn = -1;

        for (int row = 0; row < source.length; row++) {
            for (int column = 0; column < source[row].length; column++) {
                int y = row * SCALE + 2;
                int x = column * SCALE + 2;
                boolean horizontal = hasHorizontalBoundary(source, row, column, regions, region, y, x);
                boolean vertical = hasVerticalBoundary(source, row, column, regions, region, y, x);

                if (horizontal || vertical) {
                    output[row][column] = horizontal && vertical ? '+' : horizontal ? '-' : '|';
                    minRow = Math.min(minRow, row);
                    maxRow = Math.max(maxRow, row);
                    minColumn = Math.min(minColumn, column);
                    maxColumn = Math.max(maxColumn, column);
                }
            }
        }

        if (maxRow < 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int row = minRow; row <= maxRow; row++) {
            if (row > minRow) {
                result.append('\n');
            }
            result.append(output[row], minColumn, maxColumn - minColumn + 1);
            while (result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
                result.setLength(result.length() - 1);
            }
        }
        return result.toString();
    }

    private static boolean hasHorizontalBoundary(char[][] source, int row, int column,
                                                 int[][] regions, int region, int y, int x) {
        char symbol = source[row][column];
        if (symbol != '-' && symbol != '+') {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            if (symbol == '+' && (dx == 0
                    || dx < 0 && !connectsHorizontally(source, row, column - 1)
                    || dx > 0 && !connectsHorizontally(source, row, column + 1))) {
                continue;
            }
            if (regions[y - 1][x + dx] == region || regions[y + 1][x + dx] == region) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasVerticalBoundary(char[][] source, int row, int column,
                                               int[][] regions, int region, int y, int x) {
        char symbol = source[row][column];
        if (symbol != '|' && symbol != '+') {
            return false;
        }
        for (int dy = -1; dy <= 1; dy++) {
            if (symbol == '+' && (dy == 0
                    || dy < 0 && !connectsVertically(source, row - 1, column)
                    || dy > 0 && !connectsVertically(source, row + 1, column))) {
                continue;
            }
            if (regions[y + dy][x - 1] == region || regions[y + dy][x + 1] == region) {
                return true;
            }
        }
        return false;
    }

    private static boolean connectsHorizontally(char[][] source, int row, int column) {
        return row >= 0 && row < source.length && column >= 0 && column < source[0].length
                && (source[row][column] == '-' || source[row][column] == '+');
    }

    private static boolean connectsVertically(char[][] source, int row, int column) {
        return row >= 0 && row < source.length && column >= 0 && column < source[0].length
                && (source[row][column] == '|' || source[row][column] == '+');
    }
}
