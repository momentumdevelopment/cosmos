package cope.cosmos.client.manager.managers;

import cope.cosmos.client.manager.Manager;
import cope.cosmos.util.Wrapper;

public class ThreadManager extends Manager {

    // thread used by all modules
    ModuleService moduleService = new ModuleService();

    public ThreadManager() {
        super("ThreadManager", "Manages the main client service thread", 15);

        moduleService.setDaemon(true);
        moduleService.start();
    }

    public static class ModuleService extends Thread implements Wrapper {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!nullCheck()) {
                        yield();
                        continue;
                    }

                    ModuleManager.getModules(module -> module.isEnabled()).forEach(module -> {
                        try {
                            module.onThread();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });

                } catch (Exception ignored) {

                }
            }
        }
    }

    public ModuleService getService() {
        return moduleService;
    }
}
