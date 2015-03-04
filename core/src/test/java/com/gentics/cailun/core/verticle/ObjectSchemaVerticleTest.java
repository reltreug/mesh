package com.gentics.cailun.core.verticle;

import static org.junit.Assert.fail;
import io.vertx.core.http.HttpMethod;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.cailun.core.AbstractRestVerticle;
import com.gentics.cailun.core.data.model.ObjectSchema;
import com.gentics.cailun.test.AbstractRestVerticleTest;
import com.gentics.cailun.test.TestDataProvider;
import com.gentics.cailun.test.UserInfo;

public class ObjectSchemaVerticleTest extends AbstractRestVerticleTest {

	@Autowired
	private ObjectSchemaVerticle objectSchemaVerticle;

	@Override
	public AbstractRestVerticle getVerticle() {
		return objectSchemaVerticle;
	}

	@Test
	public void testReadSchemaByName() throws Exception {
		UserInfo info = data().getUserInfo();
		String json = "{\"uuid\":\"uuid-value\",\"name\":\"content\",\"description\":\"Default schema for contents\",\"propertyTypeSchemas\":[{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"content\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"filename\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"name\",\"desciption\":null}]}";
		String response = request(info, HttpMethod.GET, "/api/v1/" + TestDataProvider.PROJECT_NAME + "/types/content", 200, "OK");
		assertEqualsSanitizedJson("The response json did not match the expected one.", json, response);
	}

	@Test
	public void testReadAllSchemasForProject() throws Exception {
		UserInfo info = data().getUserInfo();
		String json = "{\"custom-content\":{\"uuid\":\"uuid-value\",\"name\":\"custom-content\",\"description\":\"Custom schema for contents\",\"propertyTypeSchemas\":[{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"content\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"filename\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"name\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"string\",\"key\":\"secret\",\"desciption\":null}]},\"content\":{\"uuid\":\"uuid-value\",\"name\":\"content\",\"description\":\"Default schema for contents\",\"propertyTypeSchemas\":[{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"content\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"filename\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"name\",\"desciption\":null}]}}";
		String response = request(info, HttpMethod.GET, "/api/v1/" + TestDataProvider.PROJECT_NAME + "/types/", 200, "OK");
		assertEqualsSanitizedJson("The response json did not match the expected one.", json, response);
	}

	@Test
	public void testReadSchemaByUUID() throws Exception {
		UserInfo info = data().getUserInfo();
		String json = "{\"uuid\":\"uuid-value\",\"name\":\"content\",\"description\":\"Default schema for contents\",\"propertyTypeSchemas\":[{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"content\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"filename\",\"desciption\":null},{\"uuid\":\"uuid-value\",\"type\":\"i18n-string\",\"key\":\"name\",\"desciption\":null}]}";
		ObjectSchema schema = data().getContentSchema();
		String response = request(info, HttpMethod.GET, "/api/v1/" + TestDataProvider.PROJECT_NAME + "/types/" + schema.getUuid(), 200,
				"OK");
		assertEqualsSanitizedJson("The response json did not match the expected one.", json, response);
	}

	@Test
	public void testReadSchemaByInvalidName() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteSchemaByUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteSchemaByName() {
		fail("Not yet implemented");
	}

	public void testDeleteSchemaWithMissingPermission() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateSimpleSchema() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateExtendedSchema() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateSchemaWithInvalidJson() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateSchemaWithMissingAttributeInJson() {
		fail("Not yet implemented");
	}

}
