package cope.cosmos.client.manager.managers;

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
                        getCosmos().getModuleManager().getAllModules().forEach(module -> {
                            try {
                                // run and remove the latest service
                                if (!clientProcesses.isEmpty()) {
                                    clientProcesses.poll().run();
                                }

                                if (module.isEnabled()) {
                                    module.onThread();
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });

                        getCosmos().getManagers().forEach(manager -> {
                            try {
                                // run and remove the latest service
                                if (!clientProcesses.isEmpty()) {
                                    clientProcesses.poll().run();
                                }

                                manager.onThread();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });
                    }

                    // give up thread resources
                    else {
                        Thread.yield();
                    }
                } catch(Exception ignored) {

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
