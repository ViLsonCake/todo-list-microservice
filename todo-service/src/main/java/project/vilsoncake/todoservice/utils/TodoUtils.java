package project.vilsoncake.todoservice.utils;

import org.springframework.stereotype.Component;
import project.vilsoncake.todoservice.document.TodoDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static project.vilsoncake.todoservice.constant.PatternConst.REGEX_TODO_TITLE_PATTERN;

@Component
public class TodoUtils {

    public int getDuplicateCount(List<TodoDocument> todoDocuments) {
        Pattern pattern = Pattern.compile(REGEX_TODO_TITLE_PATTERN);

        List<Integer> numbers = new ArrayList<>();

        for (var todoDocument : todoDocuments) {
            Matcher matcher = pattern.matcher(todoDocument.getTitle());

            while (matcher.find()) {
                int number = Integer.parseInt(matcher.group(1));
                numbers.add(number);
            }
        }

        return Collections.max(numbers);
    }
}
