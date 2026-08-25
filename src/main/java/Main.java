import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso: java Main <caminho-do-desenho.txt>");
            return;
        }

        Path input = Path.of(args[0]);
        String shape = Files.readString(input, StandardCharsets.UTF_8);

        Arrays.stream(BreakPieces.process(shape))
                .forEach(piece -> System.out.println(piece + "\n"));
    }
}
