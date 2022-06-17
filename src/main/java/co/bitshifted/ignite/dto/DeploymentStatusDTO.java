package co.bitshifted.ignite.dto;

import lombok.Data;

@Data
public class DeploymentStatusDTO {
    private String status;
    private RequiredResourcesDTO requiredResources;
}
