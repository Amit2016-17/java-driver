/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.metadata.diagnostic.ring;

import static com.datastax.oss.driver.api.core.ConsistencyLevel.LOCAL_QUORUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metadata.NodeState;
import com.datastax.oss.driver.api.core.metadata.TokenMap;
import com.datastax.oss.driver.api.core.metadata.diagnostic.TokenRingDiagnostic;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.token.TokenRange;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3Token;
import com.datastax.oss.driver.internal.core.metadata.token.Murmur3TokenRange;
import com.datastax.oss.driver.internal.core.metadata.token.ReplicationFactor;
import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalTokenRingDiagnosticGeneratorTest {

  @Mock Metadata metadata;
  @Mock TokenMap tokenMap;

  @Mock(name = "ks1")
  KeyspaceMetadata ks;

  @Mock(name = "node1")
  Node node1;

  @Mock(name = "node2")
  Node node2;

  @Mock(name = "node3")
  Node node3;

  @Mock(name = "node4")
  Node node4;

  @Mock(name = "node5")
  Node node5;

  @Mock(name = "node6")
  Node node6;

  TokenRange tr1 = new Murmur3TokenRange(new Murmur3Token(1), new Murmur3Token(2));
  TokenRange tr2 = new Murmur3TokenRange(new Murmur3Token(3), new Murmur3Token(4));

  @Before
  public void setUp() {
    given(ks.getName()).willReturn(CqlIdentifier.fromInternal("ks1"));
    given(metadata.getTokenMap()).willReturn(Optional.of(tokenMap));
    given(tokenMap.getTokenRanges()).willReturn(ImmutableSet.of(tr1, tr2));
    given(tokenMap.getReplicas(ks.getName(), tr1))
        .willReturn(ImmutableSet.of(node1, node2, node3, node4, node5, node6));
    given(tokenMap.getReplicas(ks.getName(), tr2))
        .willReturn(ImmutableSet.of(node1, node2, node3, node4, node5, node6));
    // dc1: quorum achievable
    given(node1.getState()).willReturn(NodeState.UP);
    given(node2.getState()).willReturn(NodeState.UP);
    given(node3.getState()).willReturn(NodeState.DOWN);
    // dc2: quorum not achievable
    given(node4.getState()).willReturn(NodeState.UNKNOWN);
    given(node5.getState()).willReturn(NodeState.DOWN);
    given(node6.getState()).willReturn(NodeState.DOWN);
  }

  @Test
  public void should_generate_diagnostic_for_local_CL() {
    // given
    LocalTokenRingDiagnosticGenerator generator =
        new LocalTokenRingDiagnosticGenerator(
            metadata, ks, LOCAL_QUORUM, "dc1", new ReplicationFactor(3));
    // when
    TokenRingDiagnostic tokenRingDiagnostic = generator.generate();
    // then
    assertThat(tokenRingDiagnostic).isExactlyInstanceOf(DefaultLocalTokenRingDiagnostic.class);
    assertThat(tokenRingDiagnostic)
        .isEqualTo(
            new DefaultLocalTokenRingDiagnostic(
                ks,
                LOCAL_QUORUM,
                "dc1",
                ImmutableSet.of(
                    new SimpleTokenRangeDiagnostic(tr1, ks, LOCAL_QUORUM, 2, 2),
                    new SimpleTokenRangeDiagnostic(tr2, ks, LOCAL_QUORUM, 2, 2))));
  }
}