/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.http;

import co.bitshifted.ignite.common.dto.DeploymentDTO;
import co.bitshifted.ignite.common.dto.DeploymentStatusDTO;
import co.bitshifted.ignite.common.model.DeploymentStatus;
import co.bitshifted.ignite.exception.CommunicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.maven.plugin.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static co.bitshifted.ignite.IgniteConstants.JSON_MIME_TYPE;
import static co.bitshifted.ignite.IgniteConstants.ZIP_MIME_TYPE;

public final class IgniteHttpClient {

    private static final int HTTP_STATUS_ACCEPTED = 202;
    private static final int HTTP_STATUS_OK = 200;
    private static final int WAIT_TIMEOUT_SEC = 5;
    private static final int MAX_RETRIES = 720;
    private static final String DEPLOYMENT_STATUS_HEADER = "X-Deployment-Status";
    private static final String DEPLOYMENT_SUBMIT_ENDPOINT = "/v1/deployments";


    private final String serverBaseUrl;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final Log logger;

    public IgniteHttpClient(String serverBaseUrl, Log logger) {
        client = new OkHttpClient.Builder().build();
        this.serverBaseUrl = serverBaseUrl;
        this.objectMapper = new ObjectMapper();
        this.logger = logger;
    }

    public IgniteHttpClient(Log logger) {
        this(null, logger);
    }

    public String submitDeployment(DeploymentDTO deploymentDTO) throws CommunicationException {
        try {
            String text = objectMapper.writeValueAsString(deploymentDTO);
            RequestBody body = RequestBody.create(text, MediaType.parse(JSON_MIME_TYPE));
            Request request = new Request.Builder().url(serverBaseUrl + DEPLOYMENT_SUBMIT_ENDPOINT).post(body).build();

            Call call = client.newCall(request);
            Response response = call.execute();

            int status = response.code();
            if (status != HTTP_STATUS_ACCEPTED) {
                String message = response.body().string();
                logger.error("Unexpected status from server");
                throw new CommunicationException("Unexpected HTTP status: " + status + ", message: " + message);
            }
            String headerValue = response.header(DEPLOYMENT_STATUS_HEADER);
            if (headerValue == null || headerValue.length() == 0) {
                throw new CommunicationException("Empty status header received");
            }
            return headerValue;
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    public Optional<DeploymentStatusDTO> waitForStageOneCompleted(String url) {
        logger.info("Waiting for status STAGE_ONE_COMPLETED for deployment URL " + url);
        Request request = new Request.Builder().url(url).get().build();
        for (int i = 0;i < MAX_RETRIES;i++) {
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if(response.code() == HTTP_STATUS_OK) {
                    DeploymentStatusDTO dto = objectMapper.readValue(response.body().string(), DeploymentStatusDTO.class);
                    if (dto.getStatus() == DeploymentStatus.STAGE_ONE_COMPLETED) {
                        logger.info("Stage one completed successfully!!");
                        return Optional.of(dto);
                    } else {
                        logger.info("Current deployment status: " + dto.getStatus());
                    }
                } else {
                    logger.info("Unexpected HTTP status from server: " + response.code());
                }
                logger.info("Waiting for next retry...");
                Thread.sleep(WAIT_TIMEOUT_SEC * 1000);
            } catch(IOException | InterruptedException ex) {
                logger.error("Failed to get status", ex);
                continue;
            }

        }
        return Optional.empty();
    }

    public String submitDeploymentArchive(String url, Path archive) throws CommunicationException {
        logger.info("Submitting deployment archive...");
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Files.copy(archive, bout);
            bout.close();
            RequestBody body = RequestBody.create(bout.toByteArray(), MediaType.parse(ZIP_MIME_TYPE));
            Request request = new Request.Builder().put(body).url(url).build();
            Call call = client.newCall(request);
            Response response = call.execute();
            logger.info("Received response status: " + response.code());

            int status = response.code();
            if (status != HTTP_STATUS_ACCEPTED) {
                String message = response.body().string();
                logger.error("Unexpected status from server");
                throw new CommunicationException("Unexpected HTTP status: " + status + ", message: " + message);
            }
            String headerValue = response.header(DEPLOYMENT_STATUS_HEADER);
            if (headerValue == null || headerValue.length() == 0) {
                throw new CommunicationException("Empty status header received");
            }
            return headerValue;
        } catch(IOException ex) {
            throw new CommunicationException(ex);
        }

    }

    public Optional<DeploymentStatusDTO> waitForStageTwoCompleted(String url) {
        logger.info("Waiting for stage two to complete");
        Request request = new Request.Builder().url(url).get().build();
        for (int i = 0;i < MAX_RETRIES;i++) {
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if(response.code() == HTTP_STATUS_OK) {
                    DeploymentStatusDTO dto = objectMapper.readValue(response.body().string(), DeploymentStatusDTO.class);
                    if (dto.getStatus() == DeploymentStatus.SUCCESS || dto.getStatus() == DeploymentStatus.FAILED) {
                        logger.info("Stage two completed!!");
                        return Optional.of(dto);
                    } else {
                        logger.info("Current deployment status: " + dto.getStatus());
                    }
                } else {
                    logger.info("Unexpected HTTP status from server: " + response.code());
                }
                logger.info("Waiting for next retry...");
                Thread.sleep(WAIT_TIMEOUT_SEC * 1000);
            } catch(IOException | InterruptedException ex) {
                logger.error("Failed to get status", ex);
                continue;
            }

        }
        return Optional.empty();
    }
}
