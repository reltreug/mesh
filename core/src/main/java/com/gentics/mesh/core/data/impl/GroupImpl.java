package com.gentics.mesh.core.data.impl;

import static com.gentics.mesh.core.data.relationship.GraphRelationships.ASSIGNED_TO_ROLE;
import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_ROLE;
import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_USER;
import static com.gentics.mesh.core.data.search.SearchQueueEntryAction.DELETE_ACTION;
import static com.gentics.mesh.core.data.search.SearchQueueEntryAction.STORE_ACTION;
import static com.gentics.mesh.core.rest.error.Errors.conflict;
import static com.gentics.mesh.core.rest.error.Errors.error;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.Set;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.cache.PermissionStore;
import com.gentics.mesh.core.data.Group;
import com.gentics.mesh.core.data.MeshAuthUser;
import com.gentics.mesh.core.data.Role;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.data.generic.AbstractMeshCoreVertex;
import com.gentics.mesh.core.data.generic.MeshVertexImpl;
import com.gentics.mesh.core.data.page.impl.PageImpl;
import com.gentics.mesh.core.data.relationship.GraphPermission;
import com.gentics.mesh.core.data.search.SearchQueueBatch;
import com.gentics.mesh.core.data.search.SearchQueueEntryAction;
import com.gentics.mesh.core.rest.group.GroupReference;
import com.gentics.mesh.core.rest.group.GroupResponse;
import com.gentics.mesh.core.rest.group.GroupUpdateRequest;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.parameter.impl.PagingParameters;
import com.gentics.mesh.util.ETag;
import com.gentics.mesh.util.InvalidArgumentException;
import com.gentics.mesh.util.TraversalHelper;
import com.syncleus.ferma.traversals.VertexTraversal;

/**
 * @see Group
 */
public class GroupImpl extends AbstractMeshCoreVertex<GroupResponse, Group> implements Group {

	public static void init(Database database) {
		database.addVertexType(GroupImpl.class, MeshVertexImpl.class);
		database.addVertexIndex(GroupImpl.class, true, "name");
	}

	@Override
	public GroupReference transformToReference() {
		return new GroupReference().setName(getName()).setUuid(getUuid());
	}

	@Override
	public String getType() {
		return Group.TYPE;
	}

	public String getName() {
		return getProperty("name");
	}

	public void setName(String name) {
		setProperty("name", name);
	}

	public List<? extends User> getUsers() {
		return in(HAS_USER).toListExplicit(UserImpl.class);
	}

	public void addUser(User user) {
		setUniqueLinkInTo(user.getImpl(), HAS_USER);

		// Add shortcut edge from user to roles of this group
		for (Role role : getRoles()) {
			user.getImpl().setUniqueLinkOutTo(role.getImpl(), ASSIGNED_TO_ROLE);
		}
	}

	public void removeUser(User user) {
		unlinkIn(user.getImpl(), HAS_USER);

		// Remove shortcut edge from user to roles of this group
		for (Role role : getRoles()) {
			user.getImpl().unlinkOut(role.getImpl(), ASSIGNED_TO_ROLE);
		}
		PermissionStore.invalidate();
	}

	public List<? extends Role> getRoles() {
		return in(HAS_ROLE).toListExplicit(RoleImpl.class);
	}

	public void addRole(Role role) {
		setUniqueLinkInTo(role.getImpl(), HAS_ROLE);

		// Add shortcut edges from role to users of this group
		for (User user : getUsers()) {
			user.getImpl().setUniqueLinkOutTo(role.getImpl(), ASSIGNED_TO_ROLE);
		}

	}

	public void removeRole(Role role) {
		unlinkIn(role.getImpl(), HAS_ROLE);

		// Remove shortcut edges from role to users of this group
		for (User user : getUsers()) {
			user.getImpl().unlinkOut(role.getImpl(), ASSIGNED_TO_ROLE);
		}
		PermissionStore.invalidate();
	}

	// TODO add java handler
	public boolean hasRole(Role role) {
		return in(HAS_ROLE).retain(role.getImpl()).hasNext();
	}

	public boolean hasUser(User user) {
		return in(HAS_USER).retain(user.getImpl()).hasNext();
	}

	/**
	 * Get all users within this group that are visible for the given user.
	 */
	public PageImpl<? extends User> getVisibleUsers(MeshAuthUser requestUser, PagingParameters pagingInfo) throws InvalidArgumentException {

		VertexTraversal<?, ?, ?> traversal = in(HAS_USER).mark().in(GraphPermission.READ_PERM.label()).out(HAS_ROLE).in(HAS_USER)
				.retain(requestUser.getImpl()).back().has(UserImpl.class);
		return TraversalHelper.getPagedResult(traversal, pagingInfo, UserImpl.class);
	}

	@Override
	public PageImpl<? extends Role> getRoles(MeshAuthUser requestUser, PagingParameters pagingInfo) throws InvalidArgumentException {
		VertexTraversal<?, ?, ?> traversal = in(HAS_ROLE);
		PageImpl<? extends Role> page = TraversalHelper.getPagedResult(traversal, pagingInfo, RoleImpl.class);
		return page;
	}

	@Override
	public GroupResponse transformToRestSync(InternalActionContext ac, int level, String... languageTags) {
		GroupResponse restGroup = new GroupResponse();
		restGroup.setName(getName());

		setRoles(ac, restGroup);
		fillCommonRestFields(ac, restGroup);
		setRolePermissions(ac, restGroup);

		return restGroup;
	}

	private void setRoles(InternalActionContext ac, GroupResponse restGroup) {
		for (Role role : getRoles()) {
			String name = role.getName();
			if (name != null) {
				restGroup.getRoles().add(role.transformToReference());
			}
		}
	}

	@Override
	public void delete(SearchQueueBatch batch) {
		// TODO don't allow deletion of the admin group
		batch.addEntry(this, DELETE_ACTION);
		addRelatedEntries(batch, DELETE_ACTION);
		getElement().remove();
		PermissionStore.invalidate();
	}

	@Override
	public Group update(InternalActionContext ac, SearchQueueBatch batch) {
		BootstrapInitializer boot = MeshInternal.get().boot();
		GroupUpdateRequest requestModel = ac.fromJson(GroupUpdateRequest.class);

		if (isEmpty(requestModel.getName())) {
			throw error(BAD_REQUEST, "error_name_must_be_set");
		}

		if (shouldUpdate(requestModel.getName(), getName())) {
			Group groupWithSameName = boot.groupRoot().findByName(requestModel.getName());
			if (groupWithSameName != null && !groupWithSameName.getUuid().equals(getUuid())) {
				throw conflict(groupWithSameName.getUuid(), requestModel.getName(), "group_conflicting_name", requestModel.getName());
			}

			setName(requestModel.getName());
			addIndexBatchEntry(batch, STORE_ACTION);
		}
		return this;
	}

	@Override
	public void applyPermissions(Role role, boolean recursive, Set<GraphPermission> permissionsToGrant, Set<GraphPermission> permissionsToRevoke) {
		if (recursive) {
			for (User user : getUsers()) {
				user.applyPermissions(role, false, permissionsToGrant, permissionsToRevoke);
			}
		}
		super.applyPermissions(role, recursive, permissionsToGrant, permissionsToRevoke);
	}

	@Override
	public void addRelatedEntries(SearchQueueBatch batch, SearchQueueEntryAction action) {
		for (User user : getUsers()) {
			//We need to store users as well since users list their groups - See {@link UserTransformator#toDocument(User)}
			batch.addEntry(user, STORE_ACTION);
		}
	}

	@Override
	public String getETag(InternalActionContext ac) {
		return ETag.hash(getUuid() + "-" + getLastEditedTimestamp());
	}

	@Override
	public String getAPIPath(InternalActionContext ac) {
		return "/api/v1/groups/" + getUuid();
	}

}
