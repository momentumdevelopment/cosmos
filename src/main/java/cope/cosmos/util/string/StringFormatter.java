package cope.cosmos.util.string;

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
}
