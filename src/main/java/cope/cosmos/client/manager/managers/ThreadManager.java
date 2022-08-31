package cope.cosmos.client.manager.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.features.modules.ServiceModule;
import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class ThreadManager extends Manager {

    // thread used by all modules
    private final ClientService clientService = new ClientService();

    // processes
    private static final Queue<Runnable> clientProcesses = new ArrayDeque<>();

    public ThreadManager() {
        super("ThreadManager", "Manages the main client service thread");

        // start the client thread
        clientService.setName("cosmos-client-thread");
        clientService.setDaemon(true);
        clientService.start();
    }

    public static class ClientService extends Thread implements Wrapper {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {

                    // check if the mc world is running
                    if (nullCheck()) {

                        // run and remove the latest service
                        if (!clientProcesses.isEmpty()) {
                            clientProcesses.poll().run();
                        }

                        // module onThread
                        for (Module module : getCosmos().getModuleManager().getAllModules()) {

                            // check if the module is safe to run
                            if (nullCheck() || getCosmos().getNullSafeFeatures().contains(module)) {

                                // check if module should run
                                if (module.isEnabled() || module instanceof ServiceModule) {

                                    // run
                                    try {
                                        module.onThread();
                                    } catch (Exception exception) {

                                        // print stacktrace if in dev environment
                                        if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                                            exception.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }

                        // manager onThread
                        for (Manager manager : getCosmos().getAllManagers()) {

                            // check if the manager is safe to run
                            if (nullCheck() || getCosmos().getNullSafeFeatures().contains(manager)) {

                                // run
                                try {
                                    manager.onThread();
                                } catch (Exception exception) {

                                    // print stacktrace if in dev environment
                                    if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                                        exception.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    // give up thread resources
                    else {
                        Thread.yield();
                    }

                } catch(Exception exception) {

                    // print stacktrace if in dev environment
                    if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Submit a new runnable to the thread
     * @param in The runnable
     */
    public void submit(Runnable in) {
        clientProcesses.add(in);
    }

    /**
     * Gets the main client thread
     * @return The main client thread
     */
    public ClientService getService() {
        return clientService;
    }
}
