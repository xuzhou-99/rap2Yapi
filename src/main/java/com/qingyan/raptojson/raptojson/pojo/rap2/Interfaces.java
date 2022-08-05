/**
  * Copyright 2022 json.cn 
  */
package com.qingyan.raptojson.raptojson.pojo.rap2;
import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2022-08-05 14:42:0
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Interfaces {

    private int id;
    private String name;
    private String url;
    private String method;
    private String bodyOption;
    private String description;
    private int priority;
    private int status;
    private long creatorId;
    private String lockerId;
    private int moduleId;
    private int repositoryId;
    private Date createdAt;
    private Date updatedAt;
    private String deletedAt;
    private String locker;
    private List<Properties> properties;
    public void setId(int id) {
         this.id = id;
     }
     public int getId() {
         return id;
     }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setUrl(String url) {
         this.url = url;
     }
     public String getUrl() {
         return url;
     }

    public void setMethod(String method) {
         this.method = method;
     }
     public String getMethod() {
         return method;
     }

    public void setBodyOption(String bodyOption) {
         this.bodyOption = bodyOption;
     }
     public String getBodyOption() {
         return bodyOption;
     }

    public void setDescription(String description) {
         this.description = description;
     }
     public String getDescription() {
         return description;
     }

    public void setPriority(int priority) {
         this.priority = priority;
     }
     public int getPriority() {
         return priority;
     }

    public void setStatus(int status) {
         this.status = status;
     }
     public int getStatus() {
         return status;
     }

    public void setCreatorId(long creatorId) {
         this.creatorId = creatorId;
     }
     public long getCreatorId() {
         return creatorId;
     }

    public void setLockerId(String lockerId) {
         this.lockerId = lockerId;
     }
     public String getLockerId() {
         return lockerId;
     }

    public void setModuleId(int moduleId) {
         this.moduleId = moduleId;
     }
     public int getModuleId() {
         return moduleId;
     }

    public void setRepositoryId(int repositoryId) {
         this.repositoryId = repositoryId;
     }
     public int getRepositoryId() {
         return repositoryId;
     }

    public void setCreatedAt(Date createdAt) {
         this.createdAt = createdAt;
     }
     public Date getCreatedAt() {
         return createdAt;
     }

    public void setUpdatedAt(Date updatedAt) {
         this.updatedAt = updatedAt;
     }
     public Date getUpdatedAt() {
         return updatedAt;
     }

    public void setDeletedAt(String deletedAt) {
         this.deletedAt = deletedAt;
     }
     public String getDeletedAt() {
         return deletedAt;
     }

    public void setLocker(String locker) {
         this.locker = locker;
     }
     public String getLocker() {
         return locker;
     }

    public void setProperties(List<Properties> properties) {
         this.properties = properties;
     }
     public List<Properties> getProperties() {
         return properties;
     }

}