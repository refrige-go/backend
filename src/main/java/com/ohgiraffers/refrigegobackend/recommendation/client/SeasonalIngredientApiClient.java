package com.ohgiraffers.refrigegobackend.recommendation.client;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class SeasonalIngredientApiClient {

    @Value("${seasonapi.key}")
    private String apiKey;

    private HttpClient client;

    @PostConstruct
    public void init() {
        client = HttpClient.newHttpClient();
    }

    public List<String> getSeasonalIngredients(int month) {
        String url = String.format(
                "http://211.237.50.150:7080/openapi/%s/xml/Grid_20171128000000000572_1/1/1000",
                apiKey
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String xml = response.body();
            return parseProduceFromXml(xml, month);

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private List<String> parseProduceFromXml(String xml, int month) {
        List<String> result = new ArrayList<>();
        String monthStrKor = month + "월"; // ex: "5월"

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList rowList = doc.getElementsByTagName("row");

            for (int i = 0; i < rowList.getLength(); i++) {
                Node row = rowList.item(i);

                if (row.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) row;

                    String prdName = getTagValue("PRDLST_NM", element);
                    String distMonths = getTagValue("M_DISTCTNS", element);

                    if (distMonths != null && distMonths.contains(monthStrKor)) {
                        result.add(prdName);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() == 0) return null;
        return nodeList.item(0).getTextContent();
    }
}
