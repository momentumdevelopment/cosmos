package cope.cosmos.client.ui.altmanager;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import net.minecraft.util.Session;
import java.net.Proxy;
import java.util.UUID;

/**
 * @author Wolfsurge
 */
public class Alt {

    // The type of alt
    private final AltType altType;
    // The email or username of the alt. Called login because it looks better than emailUsername etc
    private final String login;
    // The password of the alt
    private final String password;
    // The alt session, for quick login
    private final Session altSession;

    // Creates a new alt
    public Alt(String altLogin, String altPassword, AltType altType) {
        this.login = altLogin;
        this.password = altPassword;
        this.altType = altType;
        // Create a new session when, and only when, the alt is created
        this.altSession = createSession();
    }

    /**
     * Creates a new Minecraft session.
     * @return A new Minecraft session, if we were able to create one
     */
    private Session createSession() {
        switch (getAltType()) {
            case MICROSOFT:
                // Create new authenticator
                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                try {
                    // Create new result
                    MicrosoftAuthResult result = authenticator.loginWithCredentials(login, password);

                    // Return created session
                    return new Session(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), "legacy");
                } catch (MicrosoftAuthenticationException e) { 
                    e.printStackTrace();
                }
                
                break;
                
            case MOJANG:
                // Create auth variables
                YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
                YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication)service.createUserAuthentication(Agent.MINECRAFT);

                // Set email and password
                auth.setUsername(getLogin());
                auth.setPassword(getPassword());

                // Attempt login
                try {
                    auth.logIn();

                    // Return created session
                    return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
                } catch (AuthenticationException localAuthenticationException) {
                    localAuthenticationException.printStackTrace();
                }
                
                break;
                
            case CRACKED:
                return new Session(getLogin(), UUID.randomUUID().toString(), "", "legacy");
        }

        return null;
    }

    /**
     * Type of alt
     */
    public enum AltType {
        /**
         * Premium Microsoft Account
         */
        MICROSOFT,
        /**
         * Premium Mojang Account
         */
        MOJANG,
        /**
         * Unregistered account
         */
        CRACKED
    }

    /**
     * Gets the alt type
     * @return The alt type
     */
    public AltType getAltType() {
        return altType;
    }

    /**
     * Gets the email of the alt
     * @return The email of the alt
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the password of the alt
     * @return The password of the alt
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the alt's session
     * @return The alt's session
     */
    public Session getAltSession() {
        return altSession;
    }
}
