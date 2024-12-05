package com.zero.rag.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.zero.rag.common.constant.RagConstants;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import lombok.SneakyThrows;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DocumentConfiguration {

    @Bean
    public List<Document> documents() {
        return RagConstants.SPRING_BOOT_RESOURCES_LIST.stream()
                .map(url -> {
                    try {

                        //1. HTML 文档解析器 (HtmlDocumentParser)
                        //功能：解析 HTML 格式的文档。
                        //可能用到的库：JSoup 或其他 HTML 解析工具。
                        if (url.endsWith(".html")) {
                            HtmlDocumentParser parser = new HtmlDocumentParser();
                            return UrlDocumentLoader.load(url, parser);
                        }

                        //2. JSON 文档解析器 (JsonDocumentParser)
                        //功能：解析 JSON 格式的文档。
                        //可能用到的库：Jackson 或 Gson。
                        if (url.endsWith(".json")) {
                            JsonDocumentParser parser = new JsonDocumentParser();
                            return UrlDocumentLoader.load(url, parser);
                        }

                        //3. XML 文档解析器 (XmlDocumentParser)
                        //功能：解析 XML 格式的文档。
                        //可能用到的库：DOM Parser 或 SAX Parser。
                        if (url.endsWith(".xml")) {
                            XmlDocumentParser parser = new XmlDocumentParser();
                            return UrlDocumentLoader.load(url, parser);
                        }

                        //4. 文本文档解析器 (PlainTextDocumentParser)
                        //功能：直接处理纯文本内容，无需复杂解析。
                        if (url.endsWith(".txt")) {
                            PlainTextDocumentParser  parser = new PlainTextDocumentParser ();
                            return UrlDocumentLoader.load(url, parser);
                        }

                        //5. PDF 文档解析器 (PdfDocumentParser)
                        //功能：解析 PDF 文件内容。
                        //可能用到的库：Apache PDFBox。
                        if (url.endsWith(".pdf")) {
                            PdfDocumentParser parser = new PdfDocumentParser();
                            return UrlDocumentLoader.load(url, parser);
                        }

                        //6. 自定义逻辑解析器 (CustomLogicDocumentParser)
                        //功能：用户根据特定需求自定义解析逻辑，例如根据特定的格式提取表格数据或元信息。
                        if (url.endsWith(".custom")) {
                            CustomLogicDocumentParser parser = new CustomLogicDocumentParser();
                            return UrlDocumentLoader.load(url, parser);
                        }

                        TextDocumentParser parser = new TextDocumentParser();
                        return UrlDocumentLoader.load(url, parser);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load document from " + url, e);
                    }
                })
                .toList();
    }

    private static class HtmlDocumentParser implements DocumentParser {
        // 实现 HTML 文档解析逻辑
        @Override
        @SneakyThrows
        public Document parse(InputStream inputStream) {
            try {
                // 输入流转string
                String content = convertInputstreamToString(inputStream);
                // 使用 Jsoup 解析 HTML 内容
                String result = Jsoup.parse(content).text();
                // 返回解析后的文档
                return new Document(result);
            } catch (Exception e) {
                throw new ParseException("Failed to parse HTML content", 1);
            }
        }
    }

    private static class JsonDocumentParser implements DocumentParser {
        // 实现 JSON 文档解析逻辑
        @Override
        @SneakyThrows
        public Document parse(InputStream inputStream) {
            try {
                // 输入流转string
                String content = convertInputstreamToString(inputStream);
                // 使用 Jackson 解析 JSON 内容
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> jsonMap = mapper.readValue(content, Map.class);
                return new Document(jsonMap.toString());
            } catch (Exception e) {
                throw new ParseException("Failed to parse JSON content", 1);
            }
        }
    }

    private static class XmlDocumentParser implements DocumentParser {
        // 实现 XML 文档解析逻辑
        @Override
        @SneakyThrows
        public Document parse(InputStream inputStream) {
            try {
                // 输入流转string
                String content = convertInputstreamToString(inputStream);
                // 使用 DOM 解析 XML 内容
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                org.w3c.dom.Document xmlDoc = builder.parse(new ByteArrayInputStream(content.getBytes()));

                // 返回解析后的文档
                String result = xmlDoc.getDocumentElement().getTextContent();
                return new Document(result);
            } catch (Exception e) {
                throw new ParseException("Failed to parse XML content", 1);
            }
        }
    }

    private static class PlainTextDocumentParser implements DocumentParser {
        // 实现纯文本文档解析逻辑
        @Override
        @SneakyThrows
        public Document parse(InputStream inputStream) {
            try {
                String result = convertInputstreamToString(inputStream);
                return new Document(result);
            } catch (Exception e) {
                throw new ParseException("Failed to parse plain text content", 1);
            }
        }
    }

    private static class PdfDocumentParser implements DocumentParser {
        // 实现 PDF 文档解析逻辑
        @Override
        @SneakyThrows
        public Document parse(InputStream inputStream) {
            try {
//                // 输入流转string
//                String content = convertInputstreamToString(inputStream);
//                // 如果是string
//                PDDocument pdfDoc = PDDocument.load(new ByteArrayInputStream(content.getBytes()));

                // 使用 PDFBox 解析 PDF 内容
                PDDocument document = PDDocument.load(inputStream);
                PDFTextStripper stripper = new PDFTextStripper();
                String result = stripper.getText(document);

                return new Document(result);
            } catch (Exception e) {
                throw new ParseException("Failed to parse PDF content", 1);
            }
        }
    }

    private static class CustomLogicDocumentParser implements DocumentParser {
        // 实现自定义逻辑解析逻辑
        @Override
        @SneakyThrows
        public Document parse(InputStream inputStream) {
            try {
                // 输入流转string
                String content = convertInputstreamToString(inputStream);

                // 使用自定义逻辑解析内容 这部分内容可自定义实现
                String result = content.replaceAll("\\s+", " ");

                return new Document(result);
            } catch (Exception e) {
                throw new ParseException("Failed to parse custom logic content", 1);
            }
        }
    }

    private static String convertInputstreamToString(InputStream inputStream) throws IOException {
        //1. 使用 Apache Commons IO 库中的 IOUtils 工具类
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        //2. 使用 Guava 中的 CharStreams 工具类
        result = CharStreams.toString(new InputStreamReader(inputStream, Charsets.toCharset(StandardCharsets.UTF_8)));

        //3. 使用Scanner（JDK）
        java.util.Scanner scanner = new java.util.Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A");
        result = scanner.hasNext() ? scanner.next() : "";

        //4. 使用 Java 8 Stream API
        result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));

        //5. 使用parallel
        result = new BufferedReader(new InputStreamReader(inputStream)).lines().parallel().collect(Collectors.joining("\n"));

        //6. 使用InputStreamReader和StringBuilder
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        result = sb.toString();

        //7. 使用StringWriter和IOUtils.copy
        java.io.StringWriter writer = new java.io.StringWriter();
        IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);

        //8. 常用 ByteArrayOutputStream()
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream);

        //9. 常用 使用BufferedReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        result = builder.toString();

        //10. BufferedInputStream和ByteArrayOutputStream
        java.io.BufferedInputStream bis = new java.io.BufferedInputStream(inputStream);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int ch;
        while ((ch = bis.read()) != -1) {
            baos.write(ch);
        }
        result = baos.toString(StandardCharsets.UTF_8);

        //11. 使用inputStream.read()和StringBuilder
        StringBuilder sb2 = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != -1) {
            sb2.append((char) c);
        }
        result = sb2.toString();

        //12. 使用inputStream.readAllBytes()和new String()
        String result2 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        //使用 Jsoup 库解析 HTML 内容，并提取纯文本
        return Jsoup.parse(result).text();
    }

}
