package co.bitshifted.ignite.http;

import co.bitshifted.ignite.dto.DeploymentDTO;
import co.bitshifted.ignite.dto.DeploymentStatusDTO;
import co.bitshifted.ignite.exception.CommunicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.Optional;

public final class IgniteHttpClient {

    private static final int HTTP_STATUS_ACCEPTED = 202;
    private static final int HTTP_STATUS_OK = 200;
    private static final int WAIT_TIMEOUT_SEC = 10;
    private static final int MAX_RETRIES = 100;
    private static final String DEPLOYMENTT_STATUS_HEADER = "X-Deployment-Status";
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

    public String submitDeployment(DeploymentDTO deploymentDTO) throws CommunicationException {
        try {
            String text = objectMapper.writeValueAsString(deploymentDTO);
            RequestBody body = RequestBody.create(text, MediaType.parse("application/json"));
            Request request = new Request.Builder().url(serverBaseUrl + DEPLOYMENT_SUBMIT_ENDPOINT).post(body).build();

            Call call = client.newCall(request);
            Response response = call.execute();

            int status = response.code();
            if (status != HTTP_STATUS_ACCEPTED) {
                String message = response.body().string();
                logger.error("Unexpected status from server");
                throw new CommunicationException("Unexpected HTTP status: " + status + ", message: " + message);
            }
            String headerValue = response.header(DEPLOYMENTT_STATUS_HEADER);
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
                    if ("STAGE_ONE_COMPLETED".equals(dto.getStatus())) {
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
}
