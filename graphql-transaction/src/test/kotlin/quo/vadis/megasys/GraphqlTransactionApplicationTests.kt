package quo.vadis.megasys

import com.graphql.spring.boot.test.GraphQLTestTemplate
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GraphqlTransactionApplicationTests {
  @Autowired
  lateinit var graphQLTestTemplate: GraphQLTestTemplate

	@Test
	fun test() {
    val response = graphQLTestTemplate.postForResource("graphql/users.document.graphql")
	}

}
