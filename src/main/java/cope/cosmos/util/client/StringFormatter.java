package cope.cosmos.util.client;

public class StringFormatter {

    public static String formatEnum(Enum<?> enumIn) {
        String enumName = enumIn.name();
        if (!enumName.contains("_")) {
            char firstChar = enumName.charAt(0);
            String suffixChars = enumName.split(String.valueOf(firstChar), 2)[1];
            return String.valueOf(firstChar).toUpperCase() + suffixChars.toLowerCase();
        }

        String[] names = enumName.split("_");
        StringBuilder nameToReturn = new StringBuilder();

        for (String s : names) {
            char firstChar = s.charAt(0);
            String suffixChars = s.split(String.valueOf(firstChar), 2)[1];
            nameToReturn.append(String.valueOf(firstChar).toUpperCase()).append(suffixChars.toLowerCase());
        }

        return nameToReturn.toString();
    }
}
