/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLinkType;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public class DeleteIdentityLinkCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  protected String userId;
  
  protected String groupId;
  
  protected String type;
  
  public DeleteIdentityLinkCmd(String taskId, String userId, String groupId, String type) {
    super(taskId);
    validateParams(userId, groupId, type, taskId);
    this.taskId = taskId;
    this.userId = userId;
    this.groupId = groupId;
    this.type = type;
  }
  
  protected void validateParams(String userId, String groupId, String type, String taskId) {
    if(taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    
    if (type == null) {
      throw new ActivitiException("type is required when adding a new task identity link");
    }
    
    // Special treatment for assignee and owner: group cannot be used and userId may be null
    if (IdentityLinkType.ASSIGNEE.equals(type) || IdentityLinkType.OWNER.equals(type)) {
      if (groupId != null) {
        throw new ActivitiException("Incompatible usage: cannot use type '" + type
                + "' together with a groupId");
      }
    } else {
      if (userId == null && groupId == null) {
        throw new ActivitiException("userId and groupId cannot both be null");
      }
    }
  }
  
  protected Void execute(CommandContext commandContext, TaskEntity task) {

    if (IdentityLinkType.ASSIGNEE.equals(type)) {
      task.setAssignee(null);
    } else if (IdentityLinkType.OWNER.equals(type)) {
        task.setOwner(null);
    } else {
      task.deleteIdentityLink(userId, groupId, type);
    }
    
    commandContext.getHistoryManager()
      .createIdentityLinkComment(taskId, userId, groupId, type, false);
    
    return null;  
  }
  
}
