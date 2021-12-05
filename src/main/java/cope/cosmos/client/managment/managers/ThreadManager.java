package cope.cosmos.client.managment.managers;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.features.modules.Module;
import cope.cosmos.client.managment.Manager;
import cope.cosmos.utility.IUtility;

import java.util.ArrayDeque;
import java.util.Queue;

public class ThreadManager extends Manager {

    // thread used by all modules
    private final ClientService clientService = new ClientService();

    // processes
    private static final Queue<Runnable> clientProcesses = new ArrayDeque<>();

    public ThreadManager() {
        super("ThreadManager", "Manages the main client service thread");

        clientService.setName("panels-client-thread");
        clientService.setDaemon(true);
        clientService.start();
    }

    public static class ClientService extends Thread implements IUtility {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!nullCheck()) {
                        Thread.yield();
                        continue;
                    }

                    ModuleManager.getModules(Module::isEnabled).forEach(module -> {
                        try {
                            // run and remove the latest service
                            if (clientProcesses.size() > 0) {
                                clientProcesses.poll().run();
                                clientProcesses.remove();
                            }

                            module.onThread();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });

                    Cosmos.INSTANCE.getManagers().forEach(manager -> {
                        if (nullCheck()) {
                            try {
                                // run and remove the latest service
                                if (clientProcesses.size() > 0) {
                                    clientProcesses.poll().run();
                                    clientProcesses.remove();
                                }

                                manager.onThread();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    });

                } catch (Exception ignored) {

                }
            }
        }
    }

    public void submit(Runnable in) {
        clientProcesses.add(in);
    }

    public ClientService getService() {
        return clientService;
    }
}
