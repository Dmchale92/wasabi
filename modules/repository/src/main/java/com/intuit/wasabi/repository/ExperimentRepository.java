/*******************************************************************************
 * Copyright 2016 Intuit
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.intuit.wasabi.repository;

import com.google.common.collect.Table;
import com.intuit.wasabi.analyticsobjects.counts.AssignmentCounts;
import com.intuit.wasabi.assignmentobjects.Assignment;
import com.intuit.wasabi.experimentobjects.Bucket.BucketAuditInfo;
import com.intuit.wasabi.experimentobjects.Experiment.ExperimentAuditInfo;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Rows;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mid-level interface for the experiments repository
 *
 * @see Experiment
 * @see NewExperiment
 * @see Application
 * @see Context
 * @see Assignment
 * @see Bucket
 * @see BucketList
 * @see BucketAuditInfo
 * @see AssignmentCounts
 */
public interface ExperimentRepository {

    /**
     * Retrieve the specified experiment from the repository
     *
     * @param experimentID ID of the experience
     * @return experiment Object
     */
    Experiment getExperiment(Experiment.ID experimentID);

    /**
     * Retrieve the specified experiment from the repository using its label
     *
     * @param appName         name of the application
     * @param experimentLabel lable of the experiment
     * @return experiment Object
     */
    Experiment getExperiment(Application.Name appName, Experiment.Label experimentLabel);

    /**
     * Create the new experiment. Please use @link{#createIndicesForNewExperiment} after creating the Experiment
     * in Cassandra to assure that the indices are up to date for this experiment.
     *
     * @param newExperiment experiment to create
     * @return ID of the created experiment
     */
    Experiment.ID createExperiment(NewExperiment newExperiment);

    /**
     * Update the experiment
     *
     * @param experiment current experiment
     * @return updated experiment
     */
    Experiment updateExperiment(Experiment experiment);

    /**
     * Update the experiment state
     *
     * @param experiment current experiment
     * @param state      new state
     * @return updated experiment with the state
     */
    Experiment updateExperimentState(Experiment experiment, Experiment.State state);

    /**
     * Get all non-deleted/archived experiment IDs
     *
     * @return list of experiment ids
     */
    List<Experiment.ID> getExperiments();

    /**
     * Retrieve the experiments or the specified IDs
     *
     * @param experimentIDs list of experiment ids
     * @return experimentlist object
     */
    ExperimentList getExperiments(Collection<Experiment.ID> experimentIDs);

    /**
     * Get the experiments for an Application
     *
     * @param appName application name
     * @return a table contains experiment id, experiment label and the experiment objects
     */
    Table<Experiment.ID, Experiment.Label, Experiment> getExperimentList(Application.Name appName);

    List<Application.Name> getApplicationsList();


    /**
     * Retrieve the specified bucket from the repository
     *
     * @param experimentID The unique experiment ID
     * @param bucketLabel  The unique bucket label within the experiment
     * @return The specified bucket instance, or null if not found
     */
    Bucket getBucket(Experiment.ID experimentID, Bucket.Label bucketLabel);

    /**
     * Return a list of bucket IDs for the specified experiment
     *
     * @param experimentID id of the experiment
     * @return a list of buckets of that experiment
     */
    BucketList getBuckets(Experiment.ID experimentID);


    /**
     * Create a new bucket for the specified experiment
     *
     * @param newBucket bucket to create
     */
    void createBucket(Bucket newBucket);

    /**
     * Update a bucket
     *
     * @param bucket bucket to update
     * @return updated bucket object
     */
    Bucket updateBucket(Bucket bucket);

    /**
     * Update bucket allocation percentage
     *
     * @param bucket                      bucket to update
     * @param desiredAllocationPercentage allocation information
     * @return bucket
     */
    Bucket updateBucketAllocationPercentage(Bucket bucket, Double desiredAllocationPercentage);

    /**
     * update bucket state
     *
     * @param bucket       bucket to update
     * @param desiredState new state
     * @return bucket updated
     */
    Bucket updateBucketState(Bucket bucket, Bucket.State desiredState);

    /**
     * Update bucket batch
     *
     * @param experimentID experiment id
     * @param bucketList   list of buckets
     * @return updated bucket list
     */
    BucketList updateBucketBatch(Experiment.ID experimentID, BucketList bucketList);

    /**
     * Delete a bucket
     *
     * @param experimentID experiment id
     * @param bucketLabel  label for bucket
     */
    void deleteBucket(Experiment.ID experimentID, Bucket.Label bucketLabel);

    /**
     * Log changes made to the bucket metadata
     *
     * @param experimentID experiment id
     * @param bucketLabel  label for bucket
     * @param changeList   list of bucket audit info
     */
    void logBucketChanges(Experiment.ID experimentID, Bucket.Label bucketLabel,
                          List<BucketAuditInfo> changeList);

    /**
     * Log changes made to the experiment metadata
     *
     * @param experimentID experiment id
     * @param changeList   list of bucket audit info
     */
    void logExperimentChanges(Experiment.ID experimentID, List<ExperimentAuditInfo> changeList);

    /**
     * Retrieve the list of experiments that belong to application appName
     *
     * @param appName application name
     * @return list of experiments
     */
    List<Experiment> getExperiments(Application.Name appName);

    void deleteExperiment(NewExperiment newExperiment);

    /**
     * This is an cassandra specific method, that has to be used to update the Indices for a new Experiment.
     *
     * @param newExperiment new experiment
     */
    void createIndicesForNewExperiment(NewExperiment newExperiment);

    /**
     * Get the summary of assignments delivered for each experiment
     *
     * @param experimentID experiment id
     * @param context      current context
     * @return assignment counts
     */
    AssignmentCounts getAssignmentCounts(Experiment.ID experimentID, Context context);

    /**
     * Get a bucket list for a list of Experiments in a single cassandra call
     *
     * @param experimentIDCollection collection of experiment ids
     * @return map of Id to BucketList objects
     */
    Map<Experiment.ID, BucketList> getBucketList(Collection<Experiment.ID> experimentIDCollection);

    /**
     * Get the list of buckets for an experiment
     *
     * @param experimentID experiment id
     * @return List of buckets
     */
    BucketList getBucketList(Experiment.ID experimentID);

    /**
     * Update state index
     *
     * @param batch      MutationBatch object
     * @param experiment Experiment Object
     * @throws ConnectionException exception
     */
    void updateStateIndex(MutationBatch batch, Experiment experiment)
            throws ConnectionException;

    /**
     * Get experiment rows
     *
     * @param appName Application name
     * @return experiment rows
     */
    Rows<Experiment.ID, String> getExperimentRows(Application.Name appName);

    /**
     * Create an application at top level
     *
     * @param applicationName Application name
     */
    void createApplication(Application.Name applicationName);

}
