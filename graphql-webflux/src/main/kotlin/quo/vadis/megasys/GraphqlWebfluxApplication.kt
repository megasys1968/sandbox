package quo.vadis.megasys

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import com.coxautodev.graphql.tools.SchemaParserBuilder
import graphql.GraphQL
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@SpringBootApplication
class GraphqlWebfluxApplication(val userRepo: UserRepository) : CommandLineRunner {
  override fun run(vararg args: String?) {
    userRepo.saveAll(listOf(User(name = "ユウキ"), User(name = "カーシャ"), User(name = "ギジェ")))
  }

  @Bean
  fun graphql(appContext: ApplicationContext, resolvers: List<GraphQLResolver<*>>): GraphQL {
    val builder = SchemaParserBuilder()

    val schemas: List<String> = appContext
      .getResources("classpath*:/graphql/*.graphqls")
      .map { StreamUtils.copyToString(it.inputStream, StandardCharsets.UTF_8) }
      .onEach { builder.schemaString(it) }
      .toList()

    val schemaParser = builder.resolvers(resolvers).build();

    val graphQLSchema = schemaParser.makeExecutableSchema();

    return GraphQL.newGraphQL(graphQLSchema).build()
  }
}

fun main(args: Array<String>) {
  runApplication<GraphqlWebfluxApplication>(*args)
}


@Entity
data class User(
  @field:Id
  @field:GeneratedValue
  var id: Int = -1,
  var name: String = StringUtils.EMPTY)

interface UserRepository : JpaRepository<User, Int>


@Component
class QueryResolver(val userRepo: UserRepository) : GraphQLQueryResolver {
  fun users(): List<User> {
    return userRepo.findAll()
  }
}

@Component
class MutationResolver(val userRepo: UserRepository) : GraphQLMutationResolver {
  fun createUser(name: String): Boolean {
    try {
      userRepo.save(User(name = name))
      return true
    } catch (e: Exception) {
      return false
    }

  }
}
