/*
 * Copyright 2017-2019 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.pipeline.entity.cluster;

import com.epam.pipeline.entity.AbstractSecuredEntity;
import com.epam.pipeline.entity.pipeline.PipelineRun;
import com.epam.pipeline.entity.security.acl.AclClass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.kubernetes.api.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeInstance extends AbstractSecuredEntity {

    public static final String RUN_ID_LABEL = "runid";

    private UUID uid;
    private String name;
    private String creationTimestamp;
    private List<NodeInstanceAddress> addresses;

    private String clusterName;

    private Map<String, String> labels;
    private Map<String, String> allocatable;
    private Map<String, String> capacity;

    private List<PodInstance> pods;

    private NodeInstanceSystemInfo systemInfo;

    private String runId;
    private PipelineRun pipelineRun;

    public NodeInstance() {
        this.pods = new ArrayList<>();
        this.allocatable = new HashMap<>();
        this.capacity = new HashMap<>();
    }

    public NodeInstance(Node node) {
        this();
        ObjectMeta metadata = node.getMetadata();
        if (metadata != null) {
            this.setUid(UUID.fromString(metadata.getUid()));
            this.setName(metadata.getName());
            Map<String, String> labels = metadata.getLabels();
            this.setLabels(labels);
            if (labels != null) {
                this.setRunId(labels.get(RUN_ID_LABEL));
            }
            this.setCreationTimestamp(metadata.getCreationTimestamp());
            this.setClusterName(metadata.getClusterName());
        }
        NodeStatus status = node.getStatus();
        if (status != null) {
            this.setAddresses(NodeInstanceAddress.convertToInstances(status.getAddresses()));
            NodeSystemInfo info = status.getNodeInfo();
            if (info != null) {
                this.setSystemInfo(new NodeInstanceSystemInfo(info));
            }
            this.setAllocatable(QuantitiesConverter.convertQuantityMap(status.getAllocatable()));
            this.setCapacity(QuantitiesConverter.convertQuantityMap(status.getCapacity()));
        }
    }


    @Override
    @JsonIgnore
    public AbstractSecuredEntity getParent() {
        return pipelineRun == null ? null : pipelineRun.getParent();
    }

    @Override
    @JsonIgnore
    public AclClass getAclClass() {
        return AclClass.PIPELINE;
    }

    @Override
    public Long getId() {
        return 0L;
    }

    @Override
    public String getOwner() {
        return pipelineRun == null ? null : pipelineRun.getOwner();
    }
}
