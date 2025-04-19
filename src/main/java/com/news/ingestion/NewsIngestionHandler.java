package com.news.ingestion;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;

/**
 * Fetches HTML content from a public news website (currently using https://www.npr.org/sections/news/ as an example).
 * Generates a unique key for each article using the current timestamp.
 * Uploads the raw HTML content to a specified S3 bucket.
 *
 */
public class NewsIngestionHandler implements RequestHandler<Object,String>
{
    /*
             S3Client: SDK client to interact with AWS S3.

             Context: AWS Lambda provides a Context object with info like remaining time, memory, logs, etc.

             Scanner + URL: Used to fetch and read the HTML content from a web page.
    */

    private final S3Client s3 = S3Client.create();

    @Override
    public String handleRequest(Object o, Context context) {
        try {

            // Fetch HTML from NPR News
            Document doc = Jsoup.connect("https://news.ycombinator.com/")
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            // Get the entire HTML
            String content = doc.outerHtml();

            // Store in S3
            String key = "articles/article_" + Instant.now().toEpochMilli() + ".html";
            s3.putObject(PutObjectRequest.builder()
                            .bucket("news-article-storage-mjnewsgen")
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(content.getBytes()));

            return "Stored: " + key;
        } catch (Exception e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
