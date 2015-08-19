package com.gentics.mesh.core.field.node;

import static com.gentics.mesh.util.MeshAssert.latchFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.gentics.mesh.core.data.NodeFieldContainer;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.field.AbstractGraphFieldNodeVerticleTest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.field.NodeField;
import com.gentics.mesh.core.rest.node.field.impl.NodeFieldImpl;
import com.gentics.mesh.core.rest.schema.NodeFieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.impl.NodeFieldSchemaImpl;
import com.gentics.mesh.demo.DemoDataProvider;

import io.vertx.core.Future;
public class NodeGraphFieldNodeVerticleTest extends AbstractGraphFieldNodeVerticleTest {

	@Before
	public void updateSchema() throws Exception {
		Schema schema = schemaContainer("folder").getSchema();
		NodeFieldSchema nodeFieldSchema = new NodeFieldSchemaImpl();
		nodeFieldSchema.setName("nodeField");
		nodeFieldSchema.setLabel("Some label");
		nodeFieldSchema.setAllowedSchemas("folder");
		schema.addField(nodeFieldSchema);
		schemaContainer("folder").setSchema(schema);
	}

	@Test
	@Override
	public void testUpdateNodeFieldWithField() {
		Node node = folder("news");
		Node node2 = folder("deals");

		// Update the field to point to node
		NodeResponse response = updateNode("nodeField", new NodeFieldImpl().setUuid(node.getUuid()));
		NodeResponse field = response.getField("nodeField");
		assertEquals(node.getUuid(), field.getUuid());

		// Update the field to point to node2
		response = updateNode("nodeField", new NodeFieldImpl().setUuid(node2.getUuid()));
		field = response.getField("nodeField");
		assertEquals(node2.getUuid(), field.getUuid());
	}

	@Test
	public void testUpdateNodeFieldWithNodeResponseJson() {
		Node node = folder("news");
		Node node2 = folder("deals");

		// Load the node so that we can use it to prepare the update request
		Future<NodeResponse> future = getClient().findNodeByUuid(DemoDataProvider.PROJECT_NAME, node.getUuid());
		latchFor(future);
		NodeResponse loadedNode = future.result();

		// Update the field to point to node
		NodeResponse response = updateNode("nodeField", loadedNode);
		NodeResponse field = response.getField("nodeField");
		assertEquals(node.getUuid(), field.getUuid());

		// Update the field to point to node2
		response = updateNode("nodeField", new NodeFieldImpl().setUuid(node2.getUuid()));
		field = response.getField("nodeField");
		assertEquals(node2.getUuid(), field.getUuid());
	}

	@Test
	@Override
	public void testCreateNodeWithField() {
		NodeResponse response = createNode("nodeField", new NodeFieldImpl().setUuid(folder("news").getUuid()));
		NodeResponse field = response.getField("nodeField");
		assertEquals(folder("news").getUuid(), field.getUuid());
	}

	@Test
	@Override
	public void testReadNodeWithExitingField() throws IOException {
		Node newsNode = folder("news");
		Node node = folder("2015");

		NodeFieldContainer container = node.getFieldContainer(english());
		container.createNode("nodeField", newsNode);

		NodeResponse response = readNode(node);
		NodeField deserializedNodeField = response.getField("nodeField", NodeFieldImpl.class);
		assertNotNull(deserializedNodeField);
		assertEquals(newsNode.getUuid(), deserializedNodeField.getUuid());
	}

	@Test
	public void testReadExpandedNodeWithExitingField() throws IOException {
		resetClientSchemaStorage();
		Node newsNode = folder("news");
		Node node = folder("2015");

		// Create test field
		NodeFieldContainer container = node.getFieldContainer(english());
		container.createNode("nodeField", newsNode);

		// 1. Read node with collapsed fields and check that the collapsed node field can be read
		NodeResponse responseCollapsed = readNode(node);
		NodeField deserializedNodeField = responseCollapsed.getField("nodeField", NodeFieldImpl.class);
		assertNotNull(deserializedNodeField);
		assertEquals(newsNode.getUuid(), deserializedNodeField.getUuid());

		// Check whether it is possible to read the field in an expanded form.
		NodeResponse deserializedExpandedNodeField = responseCollapsed.getField("nodeField", NodeResponse.class);
		assertNotNull(deserializedExpandedNodeField);

		// 2. Read node with expanded fields
		NodeResponse responseExpanded = readNode(node, "nodeField", "bogus");

		// Check collapsed node field
		deserializedNodeField = responseExpanded.getField("nodeField", NodeFieldImpl.class);
		assertNotNull(deserializedNodeField);
		assertEquals(newsNode.getUuid(), deserializedNodeField.getUuid());

		// Check expanded node field
		deserializedExpandedNodeField = responseExpanded.getField("nodeField", NodeResponse.class);
		if (deserializedExpandedNodeField instanceof NodeResponse) {
			NodeResponse expandedField = (NodeResponse) deserializedExpandedNodeField;
			assertNotNull(expandedField);
			assertEquals(newsNode.getUuid(), expandedField.getUuid());
			assertNotNull(expandedField.getCreator());
		} else {
			fail("The returned object should be a NodeResponse object");
		}
	}

}
