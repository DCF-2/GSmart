package com.gsmart.tools;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.javadoc.Javadoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Ferramenta interna para gerar documentação em Markdown a partir do código-fonte Java.
 */
public class MarkdownGenerator {

    public static void main(String[] args) {
        // ✅ Configura o JavaParser para suportar "record"
        StaticJavaParser.getConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        String sourcePath = "src"; // Diretório do código-fonte
        String outputPath = "gsmart-docs/docs/api"; // Diretório de saída para MkDocs

        System.out.println("Iniciando geração da documentação em Markdown...");

        // Limpa a pasta de saída, se existir
        try {
            Path outputDir = Paths.get(outputPath);
            if (Files.exists(outputDir)) {
                Files.walk(outputDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            System.err.println("Erro ao limpar o diretório de saída:");
            e.printStackTrace();
            return;
        }

        // Processa os arquivos .java
        try (Stream<Path> paths = Files.walk(Paths.get(sourcePath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            generateMarkdownForFile(path, outputPath);
                        } catch (IOException e) {
                            System.err.println("Erro ao processar o arquivo: " + path);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivos do diretório fonte:");
            e.printStackTrace();
        }

        System.out.println("Documentação gerada com sucesso em: " + outputPath);
    }

    private static void generateMarkdownForFile(Path javaFile, String outputDir) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(javaFile);

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrInterface -> {
            if (classOrInterface.getNameAsString().equals("MarkdownGenerator")) {
                return; // Ignora a própria classe geradora
            }

            String className = classOrInterface.getNameAsString();
            String packageName = cu.getPackageDeclaration()
                    .map(pd -> pd.getName().asString())
                    .orElse("default");

            StringBuilder mdContent = new StringBuilder();
            mdContent.append("# Classe: ").append(className).append("\n\n")
                    .append("**Pacote:** `").append(packageName).append("`\n\n");

            // Descrição da classe
            classOrInterface.getJavadoc().ifPresent(javadoc ->
                    mdContent.append("## Descrição Geral\n\n")
                            .append(javadoc.getDescription().toText()).append("\n\n")
            );

            // Métodos
            mdContent.append("## Métodos\n\n");
            classOrInterface.getMethods().forEach(method ->
                    method.getJavadoc().ifPresent(javadoc ->
                            mdContent.append("---\n\n")
                                    .append("### `").append(method.getDeclarationAsString(true, true, true)).append("`\n\n")
                                    .append(javadoc.getDescription().toText()).append("\n\n")
                    )
            );

            // Cria os diretórios e salva o arquivo Markdown
            Path packageDir = Paths.get(outputDir, packageName.replace('.', '/'));
            try {
                Files.createDirectories(packageDir);
                Path outputFile = packageDir.resolve(className + ".md");
                try (FileWriter writer = new FileWriter(outputFile.toFile())) {
                    writer.write(mdContent.toString());
                    System.out.println("Gerado: " + outputFile);
                }
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar arquivo Markdown: " + className, e);
            }
        });
    }
}
