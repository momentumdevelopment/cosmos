package cope.cosmos.util.string;

import cope.cosmos.util.render.FontUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linustouchtips
 * @since 12/06/2021
 */
public class StringFormatter {

    /**
     * Capitalises a given string
     * @param in The string to capitalise
     * @return The string with the first letter capitalised
     */
    public static String capitalise(String in) {
        if (in.length() != 0) {
            return Character.toTitleCase(in.charAt(0)) + in.substring(1);
        }

        return "";
    }

    /**
     * Formats an enum to a String
     * @param in The enum to format
     * @return The formatted enum
     */
    public static String formatEnum(Enum<?> in) {
        // enum to string
        String enumName = in.name();

        // spaced
        if (!enumName.contains("_")) {
            char firstChar = enumName.charAt(0);
            String suffixChars = enumName.split(String.valueOf(firstChar), 2)[1];
            return String.valueOf(firstChar).toUpperCase() + suffixChars.toLowerCase();
        }

        // split by spaces
        String[] names = enumName.split("_");
        StringBuilder nameToReturn = new StringBuilder();

        // combine spaces
        for (String name : names) {
            char firstChar = name.charAt(0);
            String suffixChars = name.split(String.valueOf(firstChar), 2)[1];
            nameToReturn.append(String.valueOf(firstChar).toUpperCase()).append(suffixChars.toLowerCase());
        }

        return nameToReturn.toString();
    }

    /**
     * Wraps text to not overlap a certain width
     * @param text The text to wrap
     * @param width The width boundary
     * @return The wrapped text
     */
    public static List<String> wrapText(String text, double width) {
        ArrayList<String> finalWords = new ArrayList<>();

        if (FontUtil.getStringWidth(text) > width) {
            String[] words = text.split(" ");
            StringBuilder currentWord = new StringBuilder();
            char lastColorCode = 65535;

            for (String word : words) {
                for (int innerIndex = 0; innerIndex < word.toCharArray().length; innerIndex++) {
                    char c = word.toCharArray()[innerIndex];

                    if (c == '\u00a7' && innerIndex < word.toCharArray().length - 1) {
                        lastColorCode = word.toCharArray()[innerIndex + 1];
                    }
                }

                if (FontUtil.getStringWidth(currentWord + word + " ") < width) {
                    currentWord.append(word).append(" ");
                }

                else {
                    finalWords.add(currentWord.toString());
                    currentWord = new StringBuilder("\u00a7" + lastColorCode + word + " ");
                }
            }

            if (currentWord.length() > 0) {
                if (FontUtil.getStringWidth(currentWord.toString()) < width) {
                    finalWords.add("\u00a7" + lastColorCode + currentWord + " ");
                    currentWord = new StringBuilder();
                }

                else {
                    finalWords.addAll(formatString(currentWord.toString(), width));
                }
            }
        }

        else {
            finalWords.add(text);
        }

        return finalWords;
    }

    /**
     * Formats a string
     * @param string The string to format
     * @param width The width boundary
     * @return The formatted string
     */
    public static List<String> formatString(String string, double width) {
        ArrayList<String> finalWords = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        char lastColorCode = 65535;
        char[] chars = string.toCharArray();

        for (int index = 0; index < chars.length; index++) {
            char c = chars[index];

            if (c == '\u00a7' && index < chars.length - 1) {
                lastColorCode = chars[index + 1];
            }

            if (FontUtil.getStringWidth(currentWord.toString() + c) < width) {
                currentWord.append(c);
            }

            else {
                finalWords.add(currentWord.toString());
                currentWord = new StringBuilder("\u00a7" + lastColorCode + c);
            }
        }

        if (currentWord.length() > 0) {
            finalWords.add(currentWord.toString());
        }

        return finalWords;
    }
}
