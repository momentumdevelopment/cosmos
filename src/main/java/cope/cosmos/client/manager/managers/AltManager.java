package cope.cosmos.client.manager.managers;

import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import cope.cosmos.asm.mixins.accessor.IMinecraft;
import cope.cosmos.client.alts.AltEntry;
import cope.cosmos.client.manager.Manager;
import net.minecraft.util.Session;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bon55
 * @since 05/06/2021
 */
public class AltManager extends Manager {

	// list of all alternate accounts
	private static final List<AltEntry> alts = new ArrayList<>();

	public AltManager() {
		super("AltManager", "Manages alternate accounts for easy login");
	}

	/**
	 * Logs into a given Minecraft account
	 * @param email Email to the Minecraft account
	 * @param password Password to the Minecraft account
	 * @param setSession If to attempt to set the current session as the new login
	 * @return The authentication state of the Minecraft account
	 */
	public static YggdrasilUserAuthentication logIn(String email, String password, boolean setSession) {
		// authentication to mc servers
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);

		// update auth info
		auth.setUsername(email);
		auth.setPassword(password);

		new Thread(() -> {
			try {
				// attempt to login
				auth.logIn();

				// attempt to set the session
				if (setSession) {
					Session session = new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");

					// session null?
					if (session != null) {
						((IMinecraft) mc).setSession(session);
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}).start();

		// return authentication
		return auth;
	}

	/**
	 * Gets a list of all alt accounts
	 * @return List of all alt accounts
	 */
	public static List<AltEntry> getAlts() {
		return alts;
	}
}
