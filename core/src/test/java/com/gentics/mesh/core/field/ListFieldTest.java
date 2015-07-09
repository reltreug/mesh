package com.gentics.mesh.core.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;

import com.gentics.mesh.core.data.NodeFieldContainer;
import com.gentics.mesh.core.data.impl.NodeFieldContainerImpl;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.field.basic.BooleanField;
import com.gentics.mesh.core.data.node.field.basic.NumberField;
import com.gentics.mesh.core.data.node.field.basic.StringField;
import com.gentics.mesh.core.data.node.field.impl.basic.BooleanFieldImpl;
import com.gentics.mesh.core.data.node.field.impl.basic.StringFieldImpl;
import com.gentics.mesh.core.data.node.field.impl.nesting.NodeFieldImpl;
import com.gentics.mesh.core.data.node.field.nesting.ListField;
import com.gentics.mesh.core.data.node.field.nesting.NodeField;
import com.gentics.mesh.core.data.node.impl.NodeImpl;
import com.gentics.mesh.test.AbstractDBTest;

public class ListFieldTest extends AbstractDBTest {

	@Test
	public void testStringList() {
		NodeFieldContainer container = fg.addFramedVertex(NodeFieldContainerImpl.class);
		ListField<StringField> list = container.createList("dummyList");
		list.setListType(StringFieldImpl.class);

		list.createString("1").setString("Some string 1");
		assertEquals("dummyList", list.getFieldKey());
		assertNotNull(list.getList());

		assertEquals(1, list.getList().size());
		list.createString("2").setString("Some string 2");
		assertEquals(2, list.getList().size());

		ListField<StringField> loadedList = container.getList("dummyList");
		assertNotNull(loadedList);
		assertEquals(2, loadedList.getList().size());
	}

	@Test
	public void testNodeList() {
		Node node = fg.addFramedVertex(NodeImpl.class);
		NodeFieldContainer container = fg.addFramedVertex(NodeFieldContainerImpl.class);
		ListField<NodeField> list = container.createList("dummyList");
		list.setListType(NodeFieldImpl.class);
		assertEquals(0, list.getList().size());
		list.createNode("1", node);
		assertEquals(1, list.getList().size());

		NodeField foundNodeField = list.getList().get(0);
		assertNotNull(foundNodeField.getNode());
		assertEquals(node.getUuid(), foundNodeField.getNode().getUuid());
	}

	@Test
	public void testNumberList() {
		NodeFieldContainer container = fg.addFramedVertex(NodeFieldContainerImpl.class);
		ListField<NumberField> list = container.createList("dummyList");
		list.createNumber("1");
		assertEquals(1, list.getList().size());
	}

	@Test
	@Ignore("Not yet implemented")
	public void testMixedList() {

	}

	@Test
	public void testBooleanList() {
		NodeFieldContainer container = fg.addFramedVertex(NodeFieldContainerImpl.class);
		ListField<BooleanField> list = container.createList("dummyList");
		list.setListType(BooleanFieldImpl.class);
		list.createBoolean("A");
		list.createBoolean("B");
		list.createBoolean("C");
		assertEquals(3, list.getList().size());
	}

}
