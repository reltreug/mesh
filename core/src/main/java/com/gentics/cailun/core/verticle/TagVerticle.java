package com.gentics.cailun.core.verticle;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import org.jacpfx.vertx.spring.SpringVerticle;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gentics.cailun.core.AbstractCailunRestVerticle;
import com.gentics.cailun.core.repository.GenericContentRepository;
import com.gentics.cailun.core.repository.TagRepository;

/**
 * The tag verticle provides rest endpoints which allow manipulation and handling of tag related objects.
 * 
 * @author johannes2
 *
 */
@Component
@Scope("singleton")
@SpringVerticle
public class TagVerticle extends AbstractCailunRestVerticle {

	private static Logger log = LoggerFactory.getLogger(TagVerticle.class);

	@Autowired
	private GenericContentRepository pageRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	GraphDatabaseService graphDb;

	public TagVerticle() {
		super("tags");
	}

	@Override
	public void start() throws Exception {
		super.start();

		// addAddTagStructureHandler();
	}

	// private void addAddTagStructureHandler() {
	// route("/add/:tagPath").method(PUT).consumes(APPLICATION_JSON).handler(rc -> {
	//
	// PageCreateRequest request = null;
	// // fromJson(rc, PageCreateRequest.class);
	// String tagPath = rc.request().params().get("tagPath");
	// // final @PathParam("tagPath") String tagPath
	// ExecutionEngine engine = new ExecutionEngine(graphDb);
	//
	// String query = transformPathToCypher(tagPath);
	// System.out.println(query);
	// // WITH tag,page MERGE (tag)-[r:TAGGED]->(page) RETURN r
	// try (Transaction tx = graphDb.beginTx()) {
	// ExecutionResult result = engine.execute(query);
	// }
	// });
	// }

	// private String transformPathToCypher(String tagPath) {
	// String parts[] = tagPath.split("/");
	// StringBuilder builder = new StringBuilder();
	// List<String> tagNames = new ArrayList<>();
	// int n = 1;
	// for (String part : parts) {
	// String tagName = "tag" + n;
	// tagNames.add(tagName);
	// builder.append("MERGE (tag" + n + ":Tag { name:'" + part + "'}) ");
	// n++;
	// }
	//
	// int rels = 0;
	// for (int i = 0; i < tagNames.size(); i++) {
	// if (i == tagNames.size() - 1) {
	// builder.append("(" + tagNames.get(i) + ")");
	// continue;
	// } else {
	// builder.append("(" + tagNames.get(i) + ")-[r" + i + ":TAGGED]->");
	// rels++;
	// }
	// }
	// builder.append(" RETURN ");
	// for (int i = 0; i < rels; i++) {
	// builder.append("r" + i);
	// if (i < rels - 1) {
	// builder.append(",");
	// }
	// }
	// return builder.toString();
	// }

}
