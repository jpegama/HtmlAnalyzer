import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public class HtmlAnalyzer {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing URL argument.");
            return;
        }
        String url = args[0];
        String html = getHtml(url);
        if (html == null) {
            System.err.println("URL connection error.");
            return;
        }
        String result = findFirstDeepestText(html);
        if (result == null) {
            System.out.println("malformed HTML");
        } else {
            System.out.println(result);
        }
    }

    private static String getHtml(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/html");
            InputStream is = conn.getInputStream();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                StringBuilder htmlBuilder = new StringBuilder();
                while ((bytesRead = is.read(buffer)) != -1) {
                    htmlBuilder.append(new String(buffer, 0, bytesRead));
                }
                return htmlBuilder.toString();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static String findFirstDeepestText(String html) {
        Deque<String> tags = new ArrayDeque<>();
        int maxDepth = -1;
        String deepestText = null;
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '<') {
                // tag start
                int endIdx = html.indexOf('>', i + 1);
                if (endIdx == -1) {
                    return null; // malformed HTML
                }
                String tag = html.substring(i + 1, endIdx).toLowerCase();
                if (tag.startsWith("/")) {
                    // closing tag
                    if (!tags.isEmpty() && tags.peek().equals(tag.substring(1))) {
                        tags.pop();
                        depth--;
                        sb.setLength(0);
                    }
                } else {
                    // opening tag
                    tags.push(tag);
                    depth++;
                }
                i = endIdx;
            } else if (c == '>') {
                // tag end
                // ignore
            } else {
                // text
                if (!tags.isEmpty() && depth > maxDepth) {
                    sb.append(c);
                    if (html.charAt(i + 1) == '<' || i == html.length() - 1) {
                        maxDepth = depth;
                        deepestText = sb.toString().trim();
                    }
                }
            }
        }
        return deepestText;
    }

}
