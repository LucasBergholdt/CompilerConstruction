import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class Main {
    public static void main(String[] args) {
        String inputByteString;
        String inputFile  =  "/home/lasse/Datalogi/5. semester/Compilerkonstruktion/Assignment_1/vvpl-interpreter/vvpl-template/src/test/resources/sample-input-our-version.in";
        // String expectedFile  = "src/test/resources/sample-input-scan.out";
        try {
            inputByteString = new String(Files.readAllBytes(Paths.get(inputFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scanner lexer = new Scanner(inputByteString);
        List<Token> tokens = lexer.scanTokens();


        // C-E: Her bygger Scanner sin egen string
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token + System.lineSeparator());
            if (token.lexeme.equals(";")) builder.append(System.lineSeparator());
        }

        String fileActual = builder.toString();
        List<String> fileActualLines = Arrays.asList(fileActual.split("\\R"));

        for (String line: fileActualLines) {
            System.out.println(line);
        }
    }
}
