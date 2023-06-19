package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Trivia type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Trivias", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
@Index(name = "byUser", fields = {"userID","createdAt"})
public final class Trivia implements Model {
  public static final QueryField ID = field("Trivia", "id");
  public static final QueryField USER_ID = field("Trivia", "userID");
  public static final QueryField TRIVIA = field("Trivia", "trivia");
  public static final QueryField CREATED_AT = field("Trivia", "createdAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="ID", isRequired = true) String userID;
  private final @ModelField(targetType="String", isRequired = true) String trivia;
  private final @ModelField(targetType="AWSDateTime", isRequired = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String resolveIdentifier() {
    return id;
  }
  
  public String getId() {
      return id;
  }
  
  public String getUserId() {
      return userID;
  }
  
  public String getTrivia() {
      return trivia;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Trivia(String id, String userID, String trivia, Temporal.DateTime createdAt) {
    this.id = id;
    this.userID = userID;
    this.trivia = trivia;
    this.createdAt = createdAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Trivia trivia = (Trivia) obj;
      return ObjectsCompat.equals(getId(), trivia.getId()) &&
              ObjectsCompat.equals(getUserId(), trivia.getUserId()) &&
              ObjectsCompat.equals(getTrivia(), trivia.getTrivia()) &&
              ObjectsCompat.equals(getCreatedAt(), trivia.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), trivia.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUserId())
      .append(getTrivia())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Trivia {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("userID=" + String.valueOf(getUserId()) + ", ")
      .append("trivia=" + String.valueOf(getTrivia()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static UserIdStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Trivia justId(String id) {
    return new Trivia(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      userID,
      trivia,
      createdAt);
  }
  public interface UserIdStep {
    TriviaStep userId(String userId);
  }
  

  public interface TriviaStep {
    CreatedAtStep trivia(String trivia);
  }
  

  public interface CreatedAtStep {
    BuildStep createdAt(Temporal.DateTime createdAt);
  }
  

  public interface BuildStep {
    Trivia build();
    BuildStep id(String id);
  }
  

  public static class Builder implements UserIdStep, TriviaStep, CreatedAtStep, BuildStep {
    private String id;
    private String userID;
    private String trivia;
    private Temporal.DateTime createdAt;
    @Override
     public Trivia build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Trivia(
          id,
          userID,
          trivia,
          createdAt);
    }
    
    @Override
     public TriviaStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userID = userId;
        return this;
    }
    
    @Override
     public CreatedAtStep trivia(String trivia) {
        Objects.requireNonNull(trivia);
        this.trivia = trivia;
        return this;
    }
    
    @Override
     public BuildStep createdAt(Temporal.DateTime createdAt) {
        Objects.requireNonNull(createdAt);
        this.createdAt = createdAt;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String userId, String trivia, Temporal.DateTime createdAt) {
      super.id(id);
      super.userId(userId)
        .trivia(trivia)
        .createdAt(createdAt);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder trivia(String trivia) {
      return (CopyOfBuilder) super.trivia(trivia);
    }
    
    @Override
     public CopyOfBuilder createdAt(Temporal.DateTime createdAt) {
      return (CopyOfBuilder) super.createdAt(createdAt);
    }
  }
  
}
