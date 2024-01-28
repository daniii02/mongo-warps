package warps.mongo.manager;

import org.bukkit.Bukkit;
import warps.mongo.MongoWarps;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public final class TaskManager {
    /**
     * Se utiliza un pool distinto, para no utilizar el mismo que Bukkit.
     * Sirve para evitar el sistema de ticks y asi tener mayor precisión al calcular temporizadores.
     */
    private final ThreadPoolExecutor cachedPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /**
     * Ejecuta una tarea en el hilo principal.
     * Si la tarea ya se está ejecutando en el hilo principal, se ejecuta directamente.
     * Para forzarlo, utilizar {@link #syncLater(Runnable, long)} con <tt>delayTicks = 0</tt>.
     * @param runnable tarea a ejecutar
     */
    public void sync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(MongoWarps.get(), runnable);
        }
    }

    /**
     * Ejecuta una tarea en el hilo principal después de un delay.
     * @param runnable tarea a ejecutar
     * @param delayTicks ticks de espera
     */
    public void syncLater(Runnable runnable, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(MongoWarps.get(), runnable, delayTicks);
    }

    /**
     * Ejecuta una tarea de forma periódica en el hilo principal.
     * @param runnable tarea a ejecutar
     * @param delayTicks ticks de espera
     * @param periodTicks ticks entre cada ejecución
     */
    public void syncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimer(MongoWarps.get(), runnable, delayTicks, periodTicks);
    }

    /**
     * Ejecuta una tarea de forma asíncrona.
     * Si la tarea ya es asíncrona, imprime un error en la consola.
     * @param runnable tarea a ejecutar
     */
    public void async(Runnable runnable) {
        this.async(runnable, true);
    }

    /**
     * Ejecuta una tarea de forma asíncrona.
     * @param runnable tarea a ejecutar
     * @param checkAsync si es <tt>true</tt>, imprime un error en la consola si la tarea ya es asíncrona
     */
    public void async(Runnable runnable, boolean checkAsync) {
        if (checkAsync && !Bukkit.isPrimaryThread()) {
            MongoWarps.get().error("La tarea ya es asíncrona.", new Exception());
        }

        this.cachedPool.execute(runnable);
    }

    /**
     * @see CompletableFuture#runAsync(Runnable, java.util.concurrent.Executor)
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, this.cachedPool);
    }

    /**
     * @see CompletableFuture#supplyAsync(Supplier, java.util.concurrent.Executor)
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.cachedPool);
    }

    /**
     * Apaga el pool de tareas.
     * Uso interno
     */
    public void stop() {
        this.cachedPool.shutdownNow();
    }
}