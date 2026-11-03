package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * The gRPC protocol, enabled by the crate's protocol-grpc feature.
 */
public class EngineFeaturesTests {

	@Test
	void grpcRoundTrip() {
		// gRPC is served on the same port as HTTP and WebSocket, from 3.4.
		final String version;
		try (final Surreal ws = TestServer.connect(TestServer.WS_URL)) {
			version = ws.version();
		}
		Assumptions.assumeTrue(Surreal.atLeast(version, 3, 4), "gRPC needs server 3.4+, got " + version);

		try (final Surreal surreal = TestServer.connect(TestServer.WS_URL.replaceFirst("^ws", "grpc"))) {
			surreal.query("CREATE item:1 SET v = 'grpc'");
			assertEquals("grpc", surreal.query("SELECT VALUE v FROM ONLY item:1").take(0).getString());
			assertTrue(surreal.health());
		}
	}
}
