package quo.vadis.megasys;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class GraphQLException extends RuntimeException implements GraphQLError {
  private Map<String, Object> extensions;

  public GraphQLException(String message) {
    super(message);
  }

  public GraphQLException(String message, Throwable cause) {
    super(message, cause);
  }

  public GraphQLException(Throwable cause) {
    super(cause);
  }

  public GraphQLException(String message, Map<String, Object> extensions) {
    super(message);
    this.extensions = extensions;
  }

  public GraphQLException(Throwable cause, Map<String, Object> extensions) {
    super(cause);
    this.extensions = extensions;
  }

  public GraphQLException(String message, Throwable cause, Map<String, Object> extensions) {
    super(message, cause);
    this.extensions = extensions;
  }

  @Override
  public List<SourceLocation> getLocations() {
    return null;
  }

  @Override
  public ErrorClassification getErrorType() {
    return null;
  }

  @Override
  public List<Object> getPath() {
    return null;
  }
}
