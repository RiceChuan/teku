/*
 * Copyright Consensys Software Inc., 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.validator.remote.typedef.handlers;

import static tech.pegasys.teku.infrastructure.http.RestApiConstants.ATTESTATION_DATA_ROOT;
import static tech.pegasys.teku.infrastructure.http.RestApiConstants.SLOT;
import static tech.pegasys.teku.validator.remote.apiclient.ValidatorApiMethod.GET_AGGREGATE;

import java.util.Map;
import java.util.Optional;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.json.types.DeserializableTypeDefinition;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.operations.Attestation;
import tech.pegasys.teku.spec.datastructures.operations.AttestationSchema;
import tech.pegasys.teku.spec.schemas.SchemaDefinitionCache;
import tech.pegasys.teku.validator.remote.typedef.ResponseHandler;

public class CreateAggregateAttestationRequest extends AbstractTypeDefRequest {
  private final SchemaDefinitionCache schemaDefinitionCache;

  public CreateAggregateAttestationRequest(
      final HttpUrl baseEndpoint,
      final OkHttpClient okHttpClient,
      final SchemaDefinitionCache schemaDefinitionCache) {
    super(baseEndpoint, okHttpClient);
    this.schemaDefinitionCache = schemaDefinitionCache;
  }

  public Optional<Attestation> createAggregate(
      final UInt64 slot, final Bytes32 attestationHashTreeRoot) {

    final AttestationSchema<Attestation> attestationSchema =
        schemaDefinitionCache.atSlot(slot).getAttestationSchema().castTypeToAttestationSchema();
    final DeserializableTypeDefinition<GetAggregateAttestationResponse>
        getAggregateAttestationTypeDef =
            DeserializableTypeDefinition.object(GetAggregateAttestationResponse.class)
                .initializer(GetAggregateAttestationResponse::new)
                .withField(
                    "data",
                    attestationSchema.getJsonTypeDefinition(),
                    GetAggregateAttestationResponse::getData,
                    GetAggregateAttestationResponse::setData)
                .build();
    final ResponseHandler<GetAggregateAttestationResponse> responseHandler =
        new ResponseHandler<>(getAggregateAttestationTypeDef);

    final Map<String, String> queryParams =
        Map.of(SLOT, slot.toString(), ATTESTATION_DATA_ROOT, attestationHashTreeRoot.toString());
    return get(GET_AGGREGATE, queryParams, responseHandler)
        .map(GetAggregateAttestationResponse::getData);
  }

  public static class GetAggregateAttestationResponse {

    private Attestation data;

    public GetAggregateAttestationResponse() {}

    public GetAggregateAttestationResponse(final Attestation data) {
      this.data = data;
    }

    public Attestation getData() {
      return data;
    }

    public void setData(final Attestation data) {
      this.data = data;
    }
  }
}