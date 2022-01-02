package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linustouchtips
 * @since 11/25/2021
 */
public class ChangelogManager extends Manager {

    // the changelog as a list of strings
    private final List<String> changelog = new ArrayList<>();

    public ChangelogManager() {
        super("ChangelogManager", "Manages the client changelog");

        try {
            // the changelog from the git
            URL url = new URL("https://raw.githubusercontent.com/momentumdevelopment/cosmos/main/resources/changelog.json");
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(url.openStream()));

            // add all changelog lines
            changelog.addAll(inputStream.lines().collect(Collectors.toList()));

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Gets the changelog
     * @return The changelog as a list of Strings
     */
    public List<String> getChangelog() {
        return changelog;
    }
}
