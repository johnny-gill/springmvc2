package hello.thymeleaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
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
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
class ThymeleafBasicApplicationTests {

	static class CustomSyndEntryImpl extends SyndEntryImpl {

		private MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
		@Override
		public void setForeignMarkup(List<Element> foreignMarkup) {
			for (Element element : foreignMarkup) {
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
		}

		public MultiValueMap<String, Object> getForeignMarkupMap() {
			return multiValueMap;
		}
	}

	@Test
	void test() throws IOException, FeedException {

		List<MultiValueMap<String, Object>> list = new ArrayList<>();

//		String url = "https://trends.google.com/trends/trendingsearches/daily/rss?geo=JP";
//		SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));

		URL url = getClass().getClassLoader().getResource("static/test.rss");
		SyndFeed feed = new SyndFeedInput().build(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

		for (SyndEntry entry : feed.getEntries()) {
			CustomSyndEntryImpl customEntry = new CustomSyndEntryImpl();
			customEntry.copyFrom(entry);

			MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
			multiValueMap.add("title", customEntry.getTitle());
			multiValueMap.add("description", customEntry.getDescription().getValue());
			multiValueMap.add("link", customEntry.getLink());
			multiValueMap.add("pubDate", customEntry.getPublishedDate().toString());

			customEntry.setForeignMarkup(entry.getForeignMarkup());
			multiValueMap.addAll(customEntry.getForeignMarkupMap());
			list.add(multiValueMap);
		}

		list.sort((Comparator.comparing(o -> o.get("pubDate").toString())));
		System.out.println(new ObjectMapper().writeValueAsString(list));
	}
}
