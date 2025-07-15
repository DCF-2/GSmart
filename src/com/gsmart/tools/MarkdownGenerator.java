package com.gsmart.tools;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
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

/**
 * Ferramenta utilitária para gerar documentação em formato Markdown a partir dos comentários Javadoc do código-fonte.
 *
 * Esta classe utiliza a biblioteca {@link com.github.javaparser.JavaParser} para analisar
 * os ficheiros .java do projeto, extrair os comentários Javadoc das classes e métodos,
 * e convertê-los em ficheiros .md formatados, prontos para serem usados por geradores
 * de sites estáticos como o MkDocs.
 */
public class MarkdownGenerator {

    /**
     * Ponto de entrada principal para o gerador de documentação.
     *
     * Varre o diretório 'src', analisa todos os ficheiros .java, e gera a
     * documentação em Markdown no diretório de saída especificado.
     *
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        String sourcePath = "src";
        String outputPath = "gsmart-docs/docs/api";

        System.out.println("Iniciando geração da documentação em Markdown...");
        cleanOutputDirectory(outputPath);

        try (Stream<Path> paths = Files.walk(Paths.get(sourcePath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(path);
                            // ALTERAÇÃO: Agora encontra todas as declarações, incluindo privadas e protegidas.
                            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> {
                                generateMarkdownForClass(c, outputPath);
                            });
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

    /**
     * Gera um ficheiro Markdown para uma única classe ou interface Java.
     *
     * Extrai o pacote, o nome da classe, e os Javadocs da classe e dos seus métodos,
     * e formata tudo num ficheiro .md dentro da estrutura de diretórios do pacote correspondente.
     * A própria classe MarkdownGenerator é ignorada.
     *
     * @param classOrInterface O objeto de declaração da classe/interface a ser processado.
     * @param outputDir O diretório raiz onde os ficheiros .md serão guardados.
     */
    private static void generateMarkdownForClass(ClassOrInterfaceDeclaration classOrInterface, String outputDir) {
        // ALTERAÇÃO: A verificação se a classe tem Javadoc foi removida.
        // if (!classOrInterface.getJavadoc().isPresent()) return;

        // Ignora a si mesmo e classes anónimas
        if (classOrInterface.getNameAsString().isEmpty() || classOrInterface.getNameAsString().equals("MarkdownGenerator")) {
            return;
        }

        System.out.println("Processando classe: " + classOrInterface.getFullyQualifiedName().orElse(classOrInterface.getNameAsString()));

        String packageName = classOrInterface.findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(pd -> pd.getName().asString())
                .orElse("");

        String fileName = classOrInterface.getNameAsString() + ".md";
        Path packageDir = Paths.get(outputDir, packageName.replace('.', File.separatorChar));

        try {
            Files.createDirectories(packageDir);
            Path outputFile = packageDir.resolve(fileName);

            StringBuilder mdContent = new StringBuilder();
            mdContent.append("# Classe: `").append(classOrInterface.getNameAsString()).append("`\n\n");
            mdContent.append("**Pacote:** `").append(packageName).append("`\n\n");

            mdContent.append("## Descrição Geral\n\n");
            classOrInterface.getJavadoc().ifPresentOrElse(
                    javadoc -> mdContent.append(javadocToMarkdown(javadoc)).append("\n\n"),
                    () -> mdContent.append("*Nenhuma documentação de classe fornecida.*\n\n")
            );

            mdContent.append("## Métodos da Classe\n\n");
            // ALTERAÇÃO: Agora processa todos os métodos, independentemente do Javadoc.
            classOrInterface.getMethods().forEach(method -> {
                mdContent.append("---\n\n");
                // Mostra a declaração completa, incluindo o modificador (public, private, etc.)
                mdContent.append("### `").append(method.getDeclarationAsString(true, true, true)).append("`\n\n");

                method.getJavadoc().ifPresentOrElse(
                        javadoc -> mdContent.append(javadocToMarkdown(javadoc)).append("\n\n"),
                        () -> mdContent.append("*Nenhuma documentação de método fornecida.*\n\n")
                );
            });

            try (FileWriter writer = new FileWriter(outputFile.toFile())) {
                writer.write(mdContent.toString());
                System.out.println(" -> SUCESSO: Gerado " + outputFile.getFileName());
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar ficheiro Markdown para: " + classOrInterface.getNameAsString(), e);
        }
    }

    /**
     * Converte um objeto Javadoc completo (descrição e tags de bloco) para uma string em Markdown.
     *
     * @param javadoc O objeto Javadoc a ser convertido.
     * @return Uma string formatada em Markdown.
     */
    private static String javadocToMarkdown(Javadoc javadoc) {
        StringBuilder sb = new StringBuilder();
        sb.append(javadocDescriptionToMarkdown(javadoc.getDescription()));

        if (!javadoc.getBlockTags().isEmpty()) {
            sb.append("\n\n");
            for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                String tagName = blockTag.getTagName();
                String content = javadocDescriptionToMarkdown(blockTag.getContent());

                switch(tagName) {
                    case "param":
                        sb.append("- **Parâmetro:** `")
                                .append(blockTag.getName().orElse(""))
                                .append("` - ")
                                .append(content)
                                .append("\n");
                        break;
                    case "return":
                        sb.append("- **Retorna:** ").append(content).append("\n");
                        break;
                    case "see":
                        sb.append("- **Ver Também:** ").append(content).append("\n");
                        break;
                    default:
                        sb.append("- **`@").append(tagName).append("`**: ").append(content).append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Converte a descrição de um Javadoc (o texto principal) para uma string em Markdown.
     *
     * Este método lida especificamente com as tags inline como {@code} e {@link}.
     *
     * @param description O objeto JavadocDescription a ser convertido.
     * @return Uma string formatada em Markdown.
     */
    private static String javadocDescriptionToMarkdown(JavadocDescription description) {
        StringBuilder sb = new StringBuilder();
        description.getElements().forEach(element -> {
            if (element instanceof JavadocSnippet) {
                sb.append(element.toText());
            } else if (element instanceof JavadocInlineTag) {
                JavadocInlineTag inlineTag = (JavadocInlineTag) element;
                String content = inlineTag.getContent().trim();

                switch (inlineTag.getName()) {
                    case "code":
                        sb.append("`").append(content).append("`");
                        break;
                    case "link":
                        sb.append("`").append(content).append("`");
                        break;
                    default:
                        sb.append(content);
                }
            }
        });
        return sb.toString().replace("\n", "  \n");
    }

    /**
     * Limpa (apaga) o diretório de saída antes de uma nova geração de documentação.
     * Garante que não fiquem ficheiros antigos ou órfãos na nova versão da documentação.
     *
     * @param path O caminho para o diretório a ser limpo.
     */
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