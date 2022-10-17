package hello.thymeleaf;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@SpringBootTest
class ThymeleafBasicApplicationTests {

	@Test
	void test() throws IOException, FeedException {

		List<MultiValueMap<String, Object>> list = new ArrayList<>();
		String url = "https://trends.google.com/trends/trendingsearches/daily/rss?geo=KR";

		SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
		for (SyndEntry entry : feed.getEntries()) {
			MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
			multiValueMap.add("title", entry.getTitle());
			multiValueMap.add("description", entry.getDescription().getValue());
			multiValueMap.add("link", entry.getLink());
			multiValueMap.add("publishedDate", entry.getPublishedDate());

			for (Element element : entry.getForeignMarkup()) {
				String name = element.getName();

				if ("news_item".equals(name)) {
					Map<String, String> childMap = new HashMap<>();
					for (Element child : element.getChildren()) {
						childMap.put(child.getName(), child.getValue());
					}
					multiValueMap.add(name, childMap);
				} else {
					String value = element.getValue();
					multiValueMap.add(name, value);
				}
			}
			list.add(multiValueMap);
		}

		list.sort((Comparator.comparing(o -> o.get("publishedDate").toString())));
		for (MultiValueMap<String, Object> multiValueMap : list) {
			System.out.println(multiValueMap);
		}
	}
}
