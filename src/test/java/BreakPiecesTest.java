import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Break the pieces — testes do kata CodeWars")
class BreakPiecesTest {
    private String rectangle;

    @BeforeEach
    void prepararDesenhoBasico() {
        rectangle = "+--+\n|  |\n+--+";
    }

    @Test
    @DisplayName("extrai um retângulo simples")
    void extractsOneRectangle() {
        assertArrayEquals(new String[]{rectangle}, BreakPieces.process(rectangle));
    }

    @ParameterizedTest(name = "entrada vazia #{index}: [{0}]")
    @ValueSource(strings = {"", " ", "   \n   "})
    @DisplayName("não cria peças sem regiões fechadas")
    void ignoresEmptyDrawings(String shape) {
        assertEquals(0, BreakPieces.process(shape).length);
    }

    @Test
    @DisplayName("aceita entrada nula sem lançar exceção")
    void acceptsNullInput() {
        String[] result = assertDoesNotThrow(() -> BreakPieces.process(null));
        assertArrayEquals(new String[0], result);
    }

    @ParameterizedTest(name = "desenho aberto #{index}")
    @ValueSource(strings = {"+--+", "|\n|", "+--+\n|", "+  +\n|  |\n+--+"})
    @DisplayName("ignora desenhos que não formam região fechada")
    void ignoresOpenDrawings(String shape) {
        assertEquals(0, BreakPieces.process(shape).length);
    }

    @Test
    @DisplayName("normaliza quebra de linha do Windows")
    void acceptsWindowsLineEndings() {
        assertArrayEquals(new String[]{rectangle}, BreakPieces.process(rectangle.replace("\n", "\r\n")));
    }

    @Test
    @DisplayName("não mantém estado entre execuções")
    void doesNotLeakStateBetweenCalls() {
        assertArrayEquals(new String[]{rectangle}, BreakPieces.process(rectangle));
        assertArrayEquals(new String[0], BreakPieces.process("+--+"));
        assertArrayEquals(new String[]{rectangle}, BreakPieces.process(rectangle));
    }

    @ParameterizedTest(name = "entrada inválida #{index}: {0}")
    @ValueSource(strings = {"abc", "+--+\n|x |\n+--+", "\t"})
    @DisplayName("rejeita caracteres fora do contrato")
    void rejectsUnsupportedCharacters(String shape) {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> BreakPieces.process(shape));

        assertTrue(error.getMessage().contains("Caractere inválido"));
    }

    @ParameterizedTest(name = "arquivo: {0}")
    @MethodSource("casesFromTextFile")
    @DisplayName("executa os cenários mantidos no arquivo de recursos")
    void executesCasesFromResourceFile(String scenario, String shape, String[] expected) {
        assertArrayEquals(sorted(expected), sorted(BreakPieces.process(shape)), scenario);
    }

    static Stream<Arguments> casesFromTextFile() {
        try (var input = BreakPiecesTest.class.getResourceAsStream("/break-pieces-cases.txt")) {
            if (input == null) {
                throw new IllegalStateException("Recurso break-pieces-cases.txt não encontrado");
            }
            String content = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")
                    .strip();
            return Arrays.stream(content.split("\\n===\\n"))
                    .map(BreakPiecesTest::parseCase)
                    .toList()
                    .stream();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Arguments parseCase(String block) {
        String[] sections = block.split("\\n---\\n");
        if (sections.length < 2) {
            throw new IllegalArgumentException("Caso malformado no arquivo de testes: " + block);
        }
        int firstLine = sections[0].indexOf('\n');
        if (firstLine < 0) {
            throw new IllegalArgumentException("Caso sem nome ou desenho: " + block);
        }
        String scenario = sections[0].substring(0, firstLine);
        String shape = sections[0].substring(firstLine + 1);
        String[] expected = Arrays.copyOfRange(sections, 1, sections.length);
        return Arguments.of(scenario, shape, expected);
    }

    private static String[] sorted(String[] values) {
        return Arrays.stream(values).sorted().toArray(String[]::new);
    }
}
