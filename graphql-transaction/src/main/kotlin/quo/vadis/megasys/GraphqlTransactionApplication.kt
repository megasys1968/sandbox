package quo.vadis.megasys

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.SimpleInstrumentationContext
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters
import graphql.kickstart.execution.context.GraphQLContext
import graphql.language.OperationDefinition
import graphql.schema.DataFetchingEnvironment
import graphql.servlet.context.DefaultGraphQLServletContext
import graphql.servlet.context.DefaultGraphQLServletContextBuilder
import graphql.servlet.context.DefaultGraphQLWebSocketContext
import org.apache.commons.lang3.StringUtils
import org.dataloader.DataLoader
import org.dataloader.DataLoaderOptions
import org.dataloader.DataLoaderRegistry
import org.dataloader.MappedBatchLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.io.Serializable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.websocket.Session
import javax.websocket.server.HandshakeRequest

@Entity
data class UserModel(
  // ユーザID
  @Id
  @GeneratedValue
  var userId: Int = -1,
  // ログインID
  var userName: String = StringUtils.EMPTY
)

@Entity
data class GroupModel(
  // グループID
  @Id
  @GeneratedValue
  var groupId: Int = -1,
  // グループ名
  var groupName: String = StringUtils.EMPTY
)


@Embeddable
data class UserGroupKey(
  // ユーザID
  var userId: Int = -1,

  // グループID
  var groupId: Int = -1
): Serializable

@Entity
data class UserGroupModel(
  @EmbeddedId
  var key: UserGroupKey = UserGroupKey()
)

interface UserRepository: JpaRepository<UserModel, Int>
interface GroupRepository: JpaRepository<GroupModel, Int>
interface UserGroupRepository: JpaRepository<UserGroupModel, UserGroupKey> {
  fun findByKeyUserIdIn(userId: Collection<Int>): List<UserGroupModel>
}

@SpringBootApplication
class GraphqlTransactionApplication : GraphQLQueryResolver, ApplicationRunner {
  @Autowired
  lateinit var userRepo: UserRepository
  @Autowired
  lateinit var groupRepo: GroupRepository
  @Autowired
  lateinit var userGroupRepo: UserGroupRepository

  @Autowired
  lateinit var transactionTemplate: TransactionTemplate

  val counter = AtomicInteger()

  fun users(): List<UserModel> {
    val flag = counter.incrementAndGet() % 2
    Thread.sleep(if (1 == flag) 10000 else 3000)
    return userRepo.findAll()
  }

  override fun run(args: ApplicationArguments?) {
    transactionTemplate.execute {
      val user1 = userRepo.save(UserModel(userName = "ベス"))
      val user2 = userRepo.save(UserModel(userName = "カララ"))
      val user3 = userRepo.save(UserModel(userName = "コスモ"))
      val user4 = userRepo.save(UserModel(userName = "カーシャ"))

      val group1 = groupRepo.save(GroupModel(groupName = "ソロシップ"))
      val group2 = groupRepo.save(GroupModel(groupName = "バッフ・クラン"))

      userGroupRepo.save(UserGroupModel(UserGroupKey(user1.userId, group1.groupId)))
      userGroupRepo.save(UserGroupModel(UserGroupKey(user2.userId, group1.groupId)))
      userGroupRepo.save(UserGroupModel(UserGroupKey(user2.userId, group2.groupId)))
      userGroupRepo.save(UserGroupModel(UserGroupKey(user3.userId, group1.groupId)))
      userGroupRepo.save(UserGroupModel(UserGroupKey(user4.userId, group1.groupId)))
    }
  }
}

fun main(args: Array<String>) {
  runApplication<GraphqlTransactionApplication>(*args)
}


@Component
class GraphqlTransactionInstrumentation(private val transactionManager: PlatformTransactionManager) : SimpleInstrumentation() {

  override fun beginExecuteOperation(parameters: InstrumentationExecuteOperationParameters): InstrumentationContext<ExecutionResult> {
    val tx = TransactionTemplate(this.transactionManager)
    val operation = parameters.executionContext.operationDefinition.operation
    if (OperationDefinition.Operation.MUTATION != operation) {
      tx.isReadOnly = true
    }

    val status = this.transactionManager.getTransaction(tx)

//    return SimpleInstrumentationContext.whenDispatched { codeToRun ->
//      codeToRun.join()
//      if (codeToRun.isCompletedExceptionally) {
//        this.transactionManager.rollback(status)
//      } else {
//        this.transactionManager.commit(status)
//      }
//    }

    return SimpleInstrumentationContext.whenCompleted { _, e ->
      if (e != null) {
        this.transactionManager.rollback(status)
      } else {
        this.transactionManager.commit(status)
      }
    }

  }
}

@Component
class UserModelGraphQLResolver(): GraphQLResolver<UserModel> {
  fun groups(userModel: UserModel, env: DataFetchingEnvironment): CompletableFuture<List<GroupModel>> {
    val context = env.getContext<GraphQLContext>()
    val registry = context.dataLoaderRegistry.get()
    val dataLoader = registry.getDataLoader<Int, List<GroupModel>>(UserGroupDataLoader::class.simpleName)
    return dataLoader.load(userModel.userId)
  }
}

@Component
class UserGroupDataLoader(val userGroupRepo: UserGroupRepository, val groupRepo: GroupRepository) : MappedBatchLoader<Int, List<GroupModel>> {
  override fun load(keys: MutableSet<Int>): CompletionStage<MutableMap<Int, List<GroupModel>>> {
    val userGroups = userGroupRepo.findByKeyUserIdIn(keys)

    val groups = groupRepo.findAllById(userGroups.map { it.key.groupId }.toSet()).map { it.groupId to it }.toMap()

    val result = userGroups
      .groupBy { it.key.userId }
      .map { entry -> entry.key to entry.value.map { groups[it.key.groupId]!! } }
      .toMap().toMutableMap()

    return CompletableFuture.completedStage(result)
  }
}

@Component
class GraphQLContextBuilder(val mappedBatchLoaders: List<MappedBatchLoader<*, *>>): DefaultGraphQLServletContextBuilder() {

  val dataLoaderRegistry: DataLoaderRegistry = dataLoaderRegistry()

  override fun build(request: HttpServletRequest, response: HttpServletResponse): GraphQLContext {
    return DefaultGraphQLServletContext.createServletContext().with(request).with(response)
      .with(dataLoaderRegistry).build()
  }

  override fun build(session: Session, handshakeRequest: HandshakeRequest): GraphQLContext {
    return DefaultGraphQLWebSocketContext.createWebSocketContext().with(session).with(handshakeRequest)
      .with(dataLoaderRegistry).build()
  }

  final fun dataLoaderRegistry(): DataLoaderRegistry {
    val registry = DataLoaderRegistry()

    mappedBatchLoaders
      .forEach {
        registry.register(it::class.simpleName, DataLoader.newMappedDataLoader(it, DataLoaderOptions.newOptions().setCachingEnabled(false)))
      }
    return registry
  }
}
