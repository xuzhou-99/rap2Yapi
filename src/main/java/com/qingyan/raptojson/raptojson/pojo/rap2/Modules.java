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
public class Modules {

    private int id;
    private String name;
    private String description;
    private int priority;
    private long creatorId;
    private int repositoryId;
    private Date createdAt;
    private Date updatedAt;
    private String deletedAt;
    private List<Interfaces> interfaces;
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

    public void setCreatorId(long creatorId) {
         this.creatorId = creatorId;
     }
     public long getCreatorId() {
         return creatorId;
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

    public void setInterfaces(List<Interfaces> interfaces) {
         this.interfaces = interfaces;
     }
     public List<Interfaces> getInterfaces() {
         return interfaces;
     }

}