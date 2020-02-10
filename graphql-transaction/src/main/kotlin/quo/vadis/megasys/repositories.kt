package quo.vadis.megasys

import org.apache.commons.lang3.StringUtils
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import javax.persistence.*

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
