package quo.vadis.megasys

import com.graphql.spring.boot.test.GraphQLTestTemplate
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphqlTransactionApplicationTests {
  @Autowired
  lateinit var graphQLTestTemplate: GraphQLTestTemplate

  @Test
  fun test() {
    val tester = MultithreadedStressTester(1, 10)
    tester.stress (Runnable {
      val response = graphQLTestTemplate.postForResource("graphql/users.document.graphql")
      println(response.rawResponse.body)
    })

    tester.shutdown()
  }


  class MultithreadedStressTester(val iterationCount: Int, val threadCount: Int = DEFAULT_THREAD_COUNT, val executor: ExecutorService = Executors.newCachedThreadPool()) {
    companion object {
      const val DEFAULT_THREAD_COUNT: Int = 2
    }

    fun totalActionCount(): Int {
      return threadCount * iterationCount
    }
    fun stress(action: Runnable) {
      spawnThreads(action).await()
    }

    fun blitz(timeoutMs: Long, action: Runnable?) {
      if (!spawnThreads(action!!).await(timeoutMs, TimeUnit.MILLISECONDS)) {
        throw TimeoutException("timed out waiting for blitzed actions to complete successfully")
      }
    }

    private fun spawnThreads(action: Runnable): CountDownLatch {
      val finished = CountDownLatch(threadCount)
      for (i in 0 until threadCount) {
        executor.execute(Runnable {
          try {
            repeat(action)
          } finally {
            finished.countDown()
          }
        })
      }
      return finished
    }

    private fun repeat(action: Runnable) {
      for (i in 0 until iterationCount) {
        action.run()
      }
    }

    fun shutdown() {
      executor.shutdown()
    }
  }

}

