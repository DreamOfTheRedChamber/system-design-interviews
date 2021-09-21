# Future

* [Future](future.md#future)
  * [Future interface](future.md#future-interface)
  * [FutureTask](future.md#futuretask)
  * [CompletableFuture](future.md#completablefuture)
  * [CompletionService](future.md#completionservice)

## Future

### Future interface

* ThreadPoolExecutor interfaces

```text
// submit a Runnable task
Future<?> submit(Runnable task);

// submit a Callable task
<T> Future<T> submit(Callable<T> task);

// submit a Runnable task and its result 
<T> Future<T> submit(Runnable task, T result);
```

* Future interface methods

```text
// Cancel task
boolean cancel(boolean mayInterruptIfRunning);

// Judge whether task is cancelled
boolean isCancelled();

// Judge whether task is finished
boolean isDone();

// Get task execution result
get();

// Get task execution result with timeout support
get(long timeout, TimeUnit unit);
```

### FutureTask

* FutureTask implements Runnable and Future interface. 

```text
FutureTask(Callable<V> callable);
FutureTask(Runnable runnable, V result);
```

```java
// Create FutureTask
FutureTask<Integer> futureTask = new FutureTask<>(()-> 1+2);

// Create thread pool
ExecutorService es = Executors.newCachedThreadPool();

// Submit FutureTask 
es.submit(futureTask);

// Get computation result
Integer result = futureTask.get();
```

### CompletableFuture

* Benefits:
  * No need to manually create / maintain threads
  * More clear semantics on thread relationships with completionStage such as f3 = f1.thenCombine\(f2, \(\)-&gt;{}\)
* Constructor methods

```java
// Use the default threadpool
static CompletableFuture<Void> runAsync(Runnable runnable)
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)

// Use specific threadpool
static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

* CompletionStage: Could be used to specify the relationship between fn, consumer or action
  * fn means Function, support both parameters T and return value R.
  * consumer means Consumer supports only parameters T.
  * action means Runnable, does not support T or R. 

```java
CompletionStage<R> thenApply(fn);
CompletionStage<R> thenApplyAsync(fn);
CompletionStage<Void> thenAccept(consumer);
CompletionStage<Void> thenAcceptAsync(consumer);
CompletionStage<Void> thenRun(action);
CompletionStage<Void> thenRunAsync(action);
CompletionStage<R> thenCompose(fn);
CompletionStage<R> thenComposeAsync(fn);
```

### CompletionService

* Def: Implements a blockingQueue inside. When the future result is available, it will be put future task inside the blocking queue. 
* It has the following two constructors:
  * ExecutorCompletionService\(Executor executor\)
  * ExecutorCompletionService\(Executor executor, BlockingQueue&gt; completionQueue
* It provides the following interface methods

```java
Future<V> submit(Callable<V> task);
Future<V> submit(Runnable task, V result);
Future<V> take() throws InterruptedException;
Future<V> poll();
Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
```

