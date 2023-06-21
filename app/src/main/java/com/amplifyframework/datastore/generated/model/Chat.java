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

/** This is an auto generated class representing the Chat type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Chats", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
@Index(name = "byUser", fields = {"userID","createdAt"})
@Index(name = "byTrivia", fields = {"triviaId","createdAt"})
public final class Chat implements Model {
  public static final QueryField ID = field("Chat", "id");
  public static final QueryField USER_ID = field("Chat", "userID");
  public static final QueryField TRIVIA_ID = field("Chat", "triviaId");
  public static final QueryField MESSAGES = field("Chat", "messages");
  public static final QueryField CREATED_AT = field("Chat", "createdAt");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="ID", isRequired = true) String userID;
  private final @ModelField(targetType="ID", isRequired = true) String triviaId;
  private final @ModelField(targetType="AWSJSON") List<String> messages;
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
  
  public String getTriviaId() {
      return triviaId;
  }
  
  public List<String> getMessages() {
      return messages;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Chat(String id, String userID, String triviaId, List<String> messages, Temporal.DateTime createdAt) {
    this.id = id;
    this.userID = userID;
    this.triviaId = triviaId;
    this.messages = messages;
    this.createdAt = createdAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Chat chat = (Chat) obj;
      return ObjectsCompat.equals(getId(), chat.getId()) &&
              ObjectsCompat.equals(getUserId(), chat.getUserId()) &&
              ObjectsCompat.equals(getTriviaId(), chat.getTriviaId()) &&
              ObjectsCompat.equals(getMessages(), chat.getMessages()) &&
              ObjectsCompat.equals(getCreatedAt(), chat.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), chat.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUserId())
      .append(getTriviaId())
      .append(getMessages())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Chat {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("userID=" + String.valueOf(getUserId()) + ", ")
      .append("triviaId=" + String.valueOf(getTriviaId()) + ", ")
      .append("messages=" + String.valueOf(getMessages()) + ", ")
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
  public static Chat justId(String id) {
    return new Chat(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      userID,
      triviaId,
      messages,
      createdAt);
  }
  public interface UserIdStep {
    TriviaIdStep userId(String userId);
  }
  

  public interface TriviaIdStep {
    CreatedAtStep triviaId(String triviaId);
  }
  

  public interface CreatedAtStep {
    BuildStep createdAt(Temporal.DateTime createdAt);
  }
  

  public interface BuildStep {
    Chat build();
    BuildStep id(String id);
    BuildStep messages(List<String> messages);
  }
  

  public static class Builder implements UserIdStep, TriviaIdStep, CreatedAtStep, BuildStep {
    private String id;
    private String userID;
    private String triviaId;
    private Temporal.DateTime createdAt;
    private List<String> messages;
    @Override
     public Chat build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Chat(
          id,
          userID,
          triviaId,
          messages,
          createdAt);
    }
    
    @Override
     public TriviaIdStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userID = userId;
        return this;
    }
    
    @Override
     public CreatedAtStep triviaId(String triviaId) {
        Objects.requireNonNull(triviaId);
        this.triviaId = triviaId;
        return this;
    }
    
    @Override
     public BuildStep createdAt(Temporal.DateTime createdAt) {
        Objects.requireNonNull(createdAt);
        this.createdAt = createdAt;
        return this;
    }
    
    @Override
     public BuildStep messages(List<String> messages) {
        this.messages = messages;
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
    private CopyOfBuilder(String id, String userId, String triviaId, List<String> messages, Temporal.DateTime createdAt) {
      super.id(id);
      super.userId(userId)
        .triviaId(triviaId)
        .createdAt(createdAt)
        .messages(messages);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder triviaId(String triviaId) {
      return (CopyOfBuilder) super.triviaId(triviaId);
    }
    
    @Override
     public CopyOfBuilder createdAt(Temporal.DateTime createdAt) {
      return (CopyOfBuilder) super.createdAt(createdAt);
    }
    
    @Override
     public CopyOfBuilder messages(List<String> messages) {
      return (CopyOfBuilder) super.messages(messages);
    }
  }
  
}
