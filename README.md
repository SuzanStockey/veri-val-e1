# Break the Pieces — Verificação e Validação de Software

[![Integração contínua](https://github.com/SuzanStockey/veri-val-e1/actions/workflows/ci.yml/badge.svg)](https://github.com/SuzanStockey/veri-val-e1/actions/workflows/ci.yml)

Projeto desenvolvido para o exercício de automação de testes com Java, Maven e
JUnit 6. A aplicação resolve o kata **Break the Pieces**, do CodeWars, e possui
uma suíte local de testes automatizados com casos unitários, parametrizados, de
regressão e de erro.

## Enunciado e objetivo

O programa recebe um desenho ASCII formado por `+`, `-`, `|` e espaços. Cada
região fechada do desenho deve ser extraída como uma peça independente e
retornada em um vetor de strings.

Kata utilizado: [Break the Pieces — CodeWars](https://www.codewars.com/kata/527fde8d24b9309d9b000c4e)

Os objetivos do exercício são:

- configurar um projeto Java com Maven e JUnit 6;
- implementar uma classe principal e uma classe auxiliar;
- automatizar os testes da classe auxiliar;
- utilizar recursos como `@DisplayName`, `@BeforeEach` e testes parametrizados;
- reproduzir e ampliar localmente os casos do CodeWars;
- validar a implementação nos testes oficiais e ocultos da plataforma.

## Tecnologias

- Java 17;
- Maven 3.9 ou superior;
- JUnit 6.0.0;
- Maven Compiler Plugin 3.14.0;
- Maven Surefire Plugin 3.5.4.

## Estrutura do projeto

```text
veri-val-e1/
├── .github/workflows/
│   └── ci.yml
├── docs/evidencias/
│   └── testes-intellij.png
├── examples/
│   └── shape.txt
├── src/
│   ├── main/java/
│   │   ├── BreakPieces.java
│   │   └── Main.java
│   └── test/
│       ├── java/
│       │   └── BreakPiecesTest.java
│       └── resources/
│           └── break-pieces-cases.txt
├── pom.xml
└── README.md
```

- `BreakPieces.java`: contém o algoritmo e a API compatível com o CodeWars.
- `Main.java`: lê um desenho de um arquivo TXT e exibe as peças encontradas.
- `shape.txt`: entrada de exemplo para execução local.
- `BreakPiecesTest.java`: suíte de testes JUnit.
- `break-pieces-cases.txt`: fonte externa dos casos parametrizados de regressão.
- `pom.xml`: dependências e configuração de construção do projeto.
- `ci.yml`: automação de integração contínua no GitHub Actions.
- `docs/evidencias`: capturas reais das execuções e validações do projeto.

## Como executar

### Testes automatizados

Na raiz do projeto, execute:

```bash
mvn test
```

Para limpar os artefatos anteriores e executar a verificação completa:

```bash
mvn clean test
```

No IntelliJ IDEA, o `pom.xml` deve ser importado como projeto Maven. Depois da
importação, também é possível executar `BreakPiecesTest` pelo botão verde ao
lado da classe ou dos métodos de teste.

## Integração contínua e evidências

O workflow `.github/workflows/ci.yml` executa automaticamente
`mvn --batch-mode clean test` no GitHub Actions em cada *push* e *pull request*.
O badge no início deste README apresenta o estado da execução mais recente.

Mesmo quando ocorre uma falha, o workflow tenta salvar o conteúdo de
`target/surefire-reports` como um artefato chamado
`relatorios-junit-<numero-da-execucao>`. Esses arquivos registram os testes
executados, duração, falhas e mensagens de erro e ficam disponíveis durante 30
dias na página da execução:

1. acessar a aba **Actions** do repositório;
2. abrir a execução desejada;
3. localizar **Artifacts** no resumo da execução;
4. baixar o arquivo `relatorios-junit-...`.

A captura da execução local no IntelliJ está em
`docs/evidencias/testes-intellij.png`. Depois das demais execuções reais, os
prints do GitHub Actions e do CodeWars podem ser adicionados à mesma pasta em um
commit separado. As evidências não devem ser simuladas.

### Aplicação local

Compile o projeto e informe ao `Main` o caminho de um desenho TXT:

```bash
mvn package
java -cp target/classes Main examples/shape.txt
```

O `Main` é apenas a interface de linha de comando. A leitura do arquivo foi
mantida fora da classe `BreakPieces` para que o algoritmo continue independente
da origem dos dados e possa ser testado diretamente.

## Interface usada pelo CodeWars

O CodeWars não executa o `Main`, o Maven nem a suíte JUnit deste repositório. A
plataforma chama diretamente o seguinte método:

```text
BreakPieces.process(String shape)
```

Para realizar a tentativa na plataforma, deve-se selecionar Java e copiar o
conteúdo de `src/main/java/BreakPieces.java` para o editor do kata. O botão
**Test** executa os exemplos visíveis, enquanto **Attempt** executa também os
testes ocultos oficiais.

## Estratégia da solução

O desenho original é convertido para uma grade com escala três vezes maior.
Essa expansão preserva as conexões entre linhas e impede ambiguidades em cantos
e cruzamentos.

Em seguida, o algoritmo:

1. normaliza as quebras de linha;
2. valida os caracteres da entrada;
3. rasteriza as linhas do desenho na grade ampliada;
4. utiliza *flood fill* para identificar a região externa e cada região fechada;
5. reconstrói a borda de cada região nas dimensões originais;
6. remove margens desnecessárias e retorna as peças encontradas.

Durante a reconstrução, um `+` é convertido para `-` ou `|` quando, na peça
extraída, ele não representa mais um canto ou cruzamento.

## Testes automatizados

A suíte utiliza os seguintes recursos do JUnit:

- `@DisplayName`, para apresentar nomes legíveis;
- `@BeforeEach`, para preparar o desenho básico antes de cada teste;
- `@Test`, para comportamentos individuais;
- `@ParameterizedTest`, para executar a mesma verificação com várias entradas;
- `@ValueSource`, para entradas simples e casos de erro;
- `@MethodSource`, para carregar os cenários externos;
- `assertThrows`, para verificar exceções esperadas;
- `assertDoesNotThrow`, para verificar entradas tratadas de forma segura.

As categorias verificadas incluem:

- retângulo simples;
- peças adjacentes;
- exemplo principal do kata;
- peças aninhadas;
- entrada `null`;
- strings vazias ou formadas apenas por espaços;
- desenhos abertos, sem região fechada;
- quebras de linha do Windows (`\r\n`);
- chamadas sucessivas, verificando ausência de estado residual;
- caracteres fora do contrato;
- comparação das peças sem depender da ordem do vetor retornado.

Uma suíte finita não consegue representar todos os desenhos ASCII possíveis.
Por isso, os testes foram organizados por categorias de comportamento, casos de
borda e regressões relevantes.

## Arquivo de casos parametrizados

Os desenhos maiores ficam em `src/test/resources/break-pieces-cases.txt`, que é
a fonte única desses cenários. Isso permite incluir novos exemplos sem alterar a
classe de teste.

Formato:

```text
nome do cenário
+--+
|  |
+--+
---
+--+
|  |
+--+
===
próximo cenário
...
```

- `---` separa o desenho de entrada das peças esperadas;
- outros separadores `---` indicam peças esperadas adicionais;
- `===` encerra um caso e inicia o seguinte.

## Tratamento de entradas inválidas

- `null`, string vazia ou desenho sem região fechada: retorna `String[0]`;
- desenho válido: retorna todas as regiões fechadas encontradas;
- caractere diferente de `+`, `-`, `|`, espaço ou quebra de linha: lança
  `IllegalArgumentException` com a posição do caractere inválido.

## O que aprendi com a atividade

Antes desta atividade, eu ainda não conhecia vários recursos oferecidos pelo
JUnit para organizar e reaproveitar testes. Eu costumava imaginar que cada caso
precisaria ser escrito em um método separado. Durante o desenvolvimento, aprendi
que é possível tornar a suíte mais legível, reduzir repetições e executar a mesma
regra de teste com diferentes entradas.

### `@DisplayName`

Aprendi que `@DisplayName` permite dar um nome descritivo para uma classe ou um
método de teste. Esse texto aparece no relatório do JUnit e no IntelliJ, no lugar
de exibir somente o nome técnico do método.

Por exemplo:

```java
@Test
@DisplayName("aceita entrada nula sem lançar exceção")
void acceptsNullInput() {
    // ...
}
```

Isso facilita entender o comportamento verificado e identificar rapidamente
qual cenário falhou.

### `@ParameterizedTest`

Um teste parametrizado é um único método de teste executado várias vezes. A cada
execução, o JUnit fornece um valor ou conjunto de valores diferente.

Sem um teste parametrizado, seria necessário criar vários métodos quase iguais:
um para string vazia, outro para espaços e outro para várias linhas vazias. Com
`@ParameterizedTest`, a lógica da verificação é escrita apenas uma vez:

```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "   \n   "})
void ignoresEmptyDrawings(String shape) {
    assertEquals(0, BreakPieces.process(shape).length);
}
```

Nesse exemplo, o método é executado três vezes. Em cada execução, `shape`
recebe uma das strings declaradas no `@ValueSource`.

### `@ValueSource`

`@ValueSource` é uma fonte simples de parâmetros. Ela é adequada quando cada
execução precisa receber apenas um valor básico, como uma string, número ou
booleano.

Neste projeto, ela é utilizada para testar:

- diferentes formas de entrada vazia;
- diferentes desenhos abertos;
- diferentes entradas com caracteres inválidos.

Assim, novos valores simples podem ser acrescentados diretamente à anotação sem
duplicar o método de teste.

### `@MethodSource`

`@MethodSource` é utilizado quando os dados são mais complexos ou precisam ser
montados por um método Java. A anotação indica qual método fornecerá os
argumentos para o teste parametrizado.

Neste projeto:

```java
@ParameterizedTest(name = "arquivo: {0}")
@MethodSource("casesFromTextFile")
void executesCasesFromResourceFile(
        String scenario,
        String shape,
        String[] expected) {
    // ...
}
```

O método `casesFromTextFile()` lê `break-pieces-cases.txt` e produz vários
conjuntos de argumentos. Cada conjunto contém:

1. o nome do cenário;
2. o desenho de entrada;
3. o vetor de peças esperado.

O JUnit chama o teste uma vez para cada conjunto. Dessa forma, os desenhos
maiores ficam em um TXT legível e o código Java conserva apenas a regra de
verificação.

### `@BeforeEach`

Também aprendi que `@BeforeEach` marca um método que deve ser executado antes de
cada teste. Ele é útil para preparar dados que serão reutilizados, garantindo
que cada teste comece com uma configuração conhecida e independente.

No projeto, ele prepara o retângulo básico:

```java
@BeforeEach
void prepararDesenhoBasico() {
    rectangle = "+--+\n|  |\n+--+";
}
```

Além das anotações, a atividade ajudou a compreender a diferença entre o código
da aplicação e o código de teste, a função do Maven na construção do projeto, a
importância de testar casos de borda e de erro e a necessidade de não depender
apenas dos exemplos visíveis fornecidos pelo CodeWars.

## Uso de inteligência artificial

Utilizei o Codex, uma ferramenta de inteligência artificial da OpenAI, como
apoio durante o desenvolvimento. A IA auxiliou nas seguintes atividades:

- interpretação do enunciado e organização inicial do projeto Maven;
- discussão e implementação da estratégia baseada em grade ampliada e
  *flood fill*;
- criação e ampliação dos testes com JUnit 6;
- identificação de casos de borda, entradas inválidas e possíveis regressões;
- externalização dos desenhos de teste para um arquivo TXT;
- revisão da estrutura, da documentação e dos comandos de execução.

As sugestões produzidas pela IA foram discutidas, revisadas e ajustadas durante
o desenvolvimento. Em particular, foram removidos testes duplicados, corrigido
o tratamento das junções `+`, separada a leitura de arquivos da lógica do kata e
definido explicitamente o comportamento para entradas inválidas. A ferramenta
foi utilizada como suporte técnico, e não como substituição da validação: a
solução deve ser confirmada pela suíte Maven e pelos testes oficiais do CodeWars.

## Referências

- [Kata Break the Pieces](https://www.codewars.com/kata/527fde8d24b9309d9b000c4e)
- [Documentação do JUnit](https://docs.junit.org/current/user-guide/)
- [Documentação do Maven](https://maven.apache.org/guides/)
