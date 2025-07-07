package com.gsmart.tools;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class MarkdownGenerator {

    public static void main(String[] args) {
        StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        String sourcePath = "src";
        String outputPath = "gsmart-docs/docs/api";

        System.out.println("Iniciando geração da documentação em Markdown (vFinal)...");
        cleanOutputDirectory(outputPath);

        try (Stream<Path> paths = Files.walk(Paths.get(sourcePath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(path);
                            cu.findAll(ClassOrInterfaceDeclaration.class)
                                    .forEach(c -> generateMarkdownForClass(c, outputPath));
                        } catch (Exception e) {
                            System.err.println("Erro Crítico ao processar o ficheiro: " + path);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Documentação gerada com sucesso.");
    }

    private static void generateMarkdownForClass(ClassOrInterfaceDeclaration classOrInterface, String outputDir) {
        if (!classOrInterface.getJavadoc().isPresent()) return;

        String className = classOrInterface.getNameAsString();
        if (className.equals("MarkdownGenerator")) return;

        System.out.println("Processando classe: " + className + "...");

        String packageName = classOrInterface.findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(pd -> pd.getName().asString())
                .orElse("");

        String fileName = classOrInterface.isNestedType()
                ? classOrInterface.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() + "." + className + ".md"
                : className + ".md";

        Path packageDir = Paths.get(outputDir, packageName.replace('.', '/'));

        try {
            Files.createDirectories(packageDir);
            Path outputFile = packageDir.resolve(fileName);

            StringBuilder mdContent = new StringBuilder();
            mdContent.append("# Classe: ").append(className).append("\n\n");
            mdContent.append("**Pacote:** `").append(packageName).append("`\n\n");

            classOrInterface.getJavadoc().ifPresent(javadoc -> {
                mdContent.append("## Descrição Geral\n\n");
                mdContent.append(javadocToMarkdown(javadoc)).append("\n\n");
            });

            // ✅ CORRIGIDO PARA MOSTRAR TODOS OS MÉTODOS COM JAVADOC
            mdContent.append("## Métodos da Classe\n\n");
            classOrInterface.getMethods().stream()
                    .filter(method -> method.getJavadoc().isPresent()) // Filtra apenas se tem Javadoc
                    .forEach(method -> {
                        mdContent.append("---\n\n");
                        // Mostra a declaração completa, incluindo private/public
                        mdContent.append("### `").append(method.getDeclarationAsString(true, true, true)).append("`\n\n");
                        method.getJavadoc().ifPresent(javadoc -> {
                            mdContent.append(javadocToMarkdown(javadoc)).append("\n\n");
                        });
                    });

            try (FileWriter writer = new FileWriter(outputFile.toFile())) {
                writer.write(mdContent.toString());
                System.out.println(" -> SUCESSO: Gerado " + outputFile.getFileName());
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar ficheiro Markdown para: " + className, e);
        }
    }

    // O resto da classe (javadocToMarkdown, etc.) permanece igual...
    private static String javadocToMarkdown(Javadoc javadoc) {
        StringBuilder sb = new StringBuilder();
        sb.append(javadocDescriptionToMarkdown(javadoc.getDescription()));
        if (!javadoc.getBlockTags().isEmpty()) {
            sb.append("\n\n");
            for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                String tagName = blockTag.getTagName();
                String content = javadocDescriptionToMarkdown(blockTag.getContent());
                if ("param".equals(tagName)) {
                    sb.append("- **Parâmetro:** `")
                            .append(blockTag.getName().orElse(""))
                            .append("` - ")
                            .append(content)
                            .append("\n");
                } else {
                    sb.append("- **`@").append(tagName).append("`**: ").append(content).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private static String javadocDescriptionToMarkdown(JavadocDescription description) {
        StringBuilder sb = new StringBuilder();
        description.getElements().forEach(element -> {
            if (element instanceof JavadocSnippet) {
                sb.append(element.toText());
            } else if (element instanceof JavadocInlineTag) {
                JavadocInlineTag inlineTag = (JavadocInlineTag) element;
                if ("code".equals(inlineTag.getName())) {
                    sb.append("`").append(inlineTag.getContent().trim()).append("`");
                } else {
                    sb.append(inlineTag.getContent().trim());
                }
            }
        });
        return sb.toString().replace("\n", "  \n");
    }

    private static void cleanOutputDirectory(String path) {
        try {
            Path dir = Paths.get(path);
            if (Files.exists(dir)) {
                Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } catch (IOException e) {
            System.err.println("Aviso: Não foi possível limpar o diretório de saída. " + e.getMessage());
        }
    }
}