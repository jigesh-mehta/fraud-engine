package com.fraudintel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class FraudEngineApplicationTests {

	@MockBean
	private EmbeddingModel fraudEmbeddingModel;

	@BeforeEach
	void stubEmbeddingModel() {
		when(fraudEmbeddingModel.embed(anyString()))
				.thenAnswer(invocation -> IntStream.range(0, 384).mapToObj(i -> 0.0).toList());
		when(fraudEmbeddingModel.dimensions()).thenReturn(384);
	}

	@Test
	void contextLoads() {
	}

}
