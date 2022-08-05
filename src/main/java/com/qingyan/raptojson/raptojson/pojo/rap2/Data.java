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
public class Data {

    private int id;
    private String name;
    private String description;
    private String logo;
    private String token;
    private boolean visibility;
    private long ownerId;
    private String organizationId;
    private long creatorId;
    private String lockerId;
    private Date createdAt;
    private Date updatedAt;
    private String deletedAt;
    private Creator creator;
    private Owner owner;
    private String locker;
    private List<String> members;
    private String organization;
    private List<String> collaborators;
    private List<Modules> modules;
    private boolean canUserEdit;
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

    public void setLogo(String logo) {
         this.logo = logo;
     }
     public String getLogo() {
         return logo;
     }

    public void setToken(String token) {
         this.token = token;
     }
     public String getToken() {
         return token;
     }

    public void setVisibility(boolean visibility) {
         this.visibility = visibility;
     }
     public boolean getVisibility() {
         return visibility;
     }

    public void setOwnerId(long ownerId) {
         this.ownerId = ownerId;
     }
     public long getOwnerId() {
         return ownerId;
     }

    public void setOrganizationId(String organizationId) {
         this.organizationId = organizationId;
     }
     public String getOrganizationId() {
         return organizationId;
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

    public void setCreator(Creator creator) {
         this.creator = creator;
     }
     public Creator getCreator() {
         return creator;
     }

    public void setOwner(Owner owner) {
         this.owner = owner;
     }
     public Owner getOwner() {
         return owner;
     }

    public void setLocker(String locker) {
         this.locker = locker;
     }
     public String getLocker() {
         return locker;
     }

    public void setMembers(List<String> members) {
         this.members = members;
     }
     public List<String> getMembers() {
         return members;
     }

    public void setOrganization(String organization) {
         this.organization = organization;
     }
     public String getOrganization() {
         return organization;
     }

    public void setCollaborators(List<String> collaborators) {
         this.collaborators = collaborators;
     }
     public List<String> getCollaborators() {
         return collaborators;
     }

    public void setModules(List<Modules> modules) {
         this.modules = modules;
     }
     public List<Modules> getModules() {
         return modules;
     }

    public void setCanUserEdit(boolean canUserEdit) {
         this.canUserEdit = canUserEdit;
     }
     public boolean getCanUserEdit() {
         return canUserEdit;
     }

}