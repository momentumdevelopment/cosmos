package cope.cosmos.client.managment;

@FunctionalInterface
public interface IManager<M extends Manager> {

    M registerManager();
}
