package com.trendpulse.text_enricher;

import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexUtils {

    static final Pattern HASHTAG = Pattern.compile("#(\\w+)");
    static final Pattern MENTION = Pattern.compile("@(\\w+)");

    static Set<String> extract(String text, Pattern p){
        Matcher matcher = p.matcher(text);

        return matcher.results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
    }

}
