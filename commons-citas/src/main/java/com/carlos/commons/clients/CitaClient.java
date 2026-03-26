package com.carlos.commons.clients;
import org.springframework.cloud.openfeign.FeignClient;
import com.carlos.commons.configuration.FeighClientConfig;

@FeignClient(name = "medicos-msv", configuration = FeighClientConfig.class)
public interface CitaClient {
	


}
