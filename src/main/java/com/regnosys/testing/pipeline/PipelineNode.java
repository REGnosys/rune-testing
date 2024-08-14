package com.regnosys.testing.pipeline;

/*-
 * ===============
 * Rune Testing
 * ===============
 * Copyright (C) 2022 - 2024 REGnosys
 * ===============
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============
 */

import com.regnosys.rosetta.common.transform.TransformType;
import com.rosetta.model.lib.functions.RosettaFunction;

import java.util.List;
import java.util.stream.Collectors;

public class PipelineNode {

    private final FunctionNameHelper functionNameHelper;
    private final TransformType transformType;
    private Class<? extends RosettaFunction> function;
    private PipelineNode upstream;

    public PipelineNode(FunctionNameHelper functionNameHelper, TransformType transformType) {
        this.functionNameHelper = functionNameHelper;
        this.transformType = transformType;
    }

    PipelineNode(FunctionNameHelper functionNameHelper, TransformType transformType, Class<? extends RosettaFunction> function, PipelineNode upstream) {
        this.functionNameHelper = functionNameHelper;
        this.transformType = transformType;
        this.function = function;
        this.upstream = upstream;
    }

    public String getInputPath(boolean strictUniqueIds) {
        if (upstream != null) {
            return String.format("%s/output/%s", upstream.getTransformType().getResourcePath(), upstream.idSuffix(strictUniqueIds, "/"));
        }
        return String.format("%s/input", transformType.getResourcePath());
    }

    public String getOutputPath(boolean strictUniqueIds) {
        return String.format("%s/output/%s", transformType.getResourcePath(), idSuffix(strictUniqueIds, "/"));
    }

    public List<PipelineNode> withFunctions(List<Class<? extends RosettaFunction>> function) {
        return function.stream()
                .map(f -> new PipelineNode(functionNameHelper, transformType, f, upstream))
                .collect(Collectors.toList());
    }

    public PipelineNode withFunction(Class<? extends RosettaFunction> function) {
        this.function = function;
        return this;
    }

    public PipelineNode linkWithUpstream(PipelineNode upstreamPipelineNode) {
        this.upstream = upstreamPipelineNode;
        return this;
    }

    public Class<? extends RosettaFunction> getFunction() {
        return function;
    }

    public TransformType getTransformType() {
        return transformType;
    }

    public String id(boolean strictUniqueIds) {
        return String.format("pipeline-%s-%s", getTransformType().name().toLowerCase(), idSuffix(strictUniqueIds, "-"));
    }

    public String upstreamId(boolean strictUniqueIds) {
        if (strictUniqueIds) {
            return (upstream == null) ? null :
                    String.format("pipeline-%s-%s", upstream.getTransformType().name().toLowerCase(), upstream.upstreamIdSuffix(strictUniqueIds, "-"));
        }
        return (upstream == null) ? null :
                String.format("pipeline-%s-%s", upstream.getTransformType().name().toLowerCase(), upstream.idSuffix(strictUniqueIds, "-"));
    }

    private String upstreamIdSuffix(boolean strictUniqueIds, String separator) {
        return (upstream == null) ? idSuffix(strictUniqueIds, separator) :
                String.format("%s%s%s", upstream.upstreamIdSuffix(strictUniqueIds, separator), separator, functionNameHelper.readableId(getFunction()));
    }

    public String idSuffix(boolean strictUniqueIds, String separator) {
        if (strictUniqueIds) {
            return (upstream == null) ? functionNameHelper.readableId(getFunction()) :
                    String.format("%s%s%s", upstream.idSuffix(true, separator), separator, functionNameHelper.readableId(getFunction()));
        }
        return functionNameHelper.readableId(getFunction());
    }

    @Override
    public String toString() {
        return id(true);
    }

    public PipelineNode getUpstream() {
        return upstream;
    }
}