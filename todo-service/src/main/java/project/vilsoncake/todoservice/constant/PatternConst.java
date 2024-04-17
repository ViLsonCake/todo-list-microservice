package project.vilsoncake.todoservice.constant;

public class PatternConst {
    public static final String TITLE_LIKE_PATTERN_POSTFIX = " (";
    public static final String TITLE_POSTFIX = " (%s)";
    public static final String REGEX_TODO_TITLE_DUPLICATE_PATTERN = " \\((\\d+)\\)";
    public static final String REGEX_TITLE_VALIDATION_PATTERN = "^[a-zA-Z\\s]+$";
    public static final String REGEX_CATEGORY_VALIDATION_PATTERN = "^[a-zA-Z\\s]+$";
}
