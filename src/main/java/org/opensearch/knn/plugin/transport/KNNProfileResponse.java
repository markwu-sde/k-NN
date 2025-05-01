/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.knn.plugin.transport;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.opensearch.action.support.broadcast.BroadcastResponse;
import org.opensearch.action.support.broadcast.BroadcastShardResponse;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.core.action.ActionResponse;
import org.opensearch.core.action.support.DefaultShardOperationFailedException;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.knn.profiler.SegmentProfilerState;

import java.io.IOException;
import java.util.List;

public class KNNProfileResponse extends BroadcastResponse implements ToXContentObject {

    //List of all the shard profile results
    List<KNNIndexShardProfileResult> shardProfileResults;

    public KNNProfileResponse() {}

    public KNNProfileResponse(StreamInput in) throws IOException {
        super(in);
    }

    public KNNProfileResponse(List<KNNIndexShardProfileResult> shardProfileResults,
            int totalShards,
            int successfulShards,
            int failedShards,
            List<DefaultShardOperationFailedException> shardFailures
    ) {
        super(totalShards, successfulShards, failedShards, shardFailures);

        this.shardProfileResults = shardProfileResults;
    }

    @Override
    public void writeTo(StreamOutput streamOutput) throws IOException {
        throw new UnsupportedOperationException("This method is not available");
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, Params params) throws IOException {

        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
        //Grab all the index shard profile results
        //TODO: Clean up the XContentBuilder format - this is a bit hacky
        for (KNNIndexShardProfileResult shardProfileResult : shardProfileResults) {
            //TODO:Currently we haven't aggregated any of the segments in the shard.
            builder.startObject(shardProfileResult.shardId);
            for (SegmentProfilerState state : shardProfileResult.segmentProfilerStateList) {
                //TODO: get the segment ID here instead of a hard coded value
                builder.field("Segment ID:", 1);

                for (SummaryStatistics summaryStatistics : state.getStatistics()) {
                    builder.startObject(Integer.toString(state.getDimension()));
                    builder.field("Result:", summaryStatistics.toString());
                    builder.endObject();
                }
                builder.endObject();
            }
        }

        builder.endObject();

        //TODO: modify this XContentBuilder to print correctly
        xContentBuilder.value(builder.humanReadable(true).toString());
        return xContentBuilder;
    }
}
