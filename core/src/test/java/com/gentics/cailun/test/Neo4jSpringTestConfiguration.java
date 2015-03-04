package com.gentics.cailun.test;

import io.vertx.core.Vertx;
import io.vertx.ext.graph.neo4j.Neo4jGraphVerticle;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.gentics.cailun.etc.neo4j.UUIDTransactionEventHandler;

@Configuration
@EnableNeo4jRepositories("com.gentics.cailun")
@EnableTransactionManagement
@ComponentScan(basePackages = { "com.gentics.cailun" })
public class Neo4jSpringTestConfiguration extends Neo4jConfiguration {

	public Neo4jSpringTestConfiguration() {
		setBasePackage("com.gentics.cailun");
	}

	@Bean
	public GraphDatabaseService graphDatabaseService() {
		GraphDatabaseService service = new TestGraphDatabaseFactory().newImpermanentDatabase();
		service.registerTransactionEventHandler(new UUIDTransactionEventHandler(service));
		Neo4jGraphVerticle.setDatabaseService(service);
		return service;
	}

	@Bean
	public Vertx vertx() {
		return Vertx.vertx();
	}

}
