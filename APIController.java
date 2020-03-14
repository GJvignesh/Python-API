package com.mugu.codes;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import okhttp3.*;


@RestController
public class APIController {
		
	
    //API Implementation for New Deployment
	@RequestMapping(value="/newdeploy",method=RequestMethod.POST)
	public JSONObject doDeploy(@RequestHeader Map<String,String> reqHeaders,@RequestBody MultipartFile file) throws IOException
	{
		
		OkHttpClient httpClient = new OkHttpClient();
		System.out.println("Headers :"+reqHeaders);
		
		//Storing Header values
		//keys are being converted to lower case because of springboot
		String envID = reqHeaders.get("x-anypnt-env-id");
		String orgID = reqHeaders.get("x-anypnt-org-id");
		String artifactName = reqHeaders.get("artifactname");
		String targetId = reqHeaders.get("targetid");
		
		File convFile = new File( file.getOriginalFilename() );
		
		okhttp3.RequestBody formBody = new FormBody.Builder()
                .add("username", "pams_apiuser_dev")
                .add("password", "Muletest1234")
                .build();

        Request request = new Request.Builder()
                .url("https://anypoint.mulesoft.com/accounts/login")
                .post(formBody)
                .build();
        
		//Calling URL to get bearer token
        Response response =  httpClient.newCall(request).execute();
        
        JSONParser parser = new JSONParser();
        try 
        {
			JSONObject json = (JSONObject) parser.parse(response.body().string());
			
			//Extracting token from response
			String bearer = (String) json.get("access_token");
			
			System.out.println("Bearer Token :"+bearer);
					
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.add("Authorization", "bearer "+bearer);
			headers.add("X-ANYPNT-ENV-ID", envID);
			headers.add("X-ANYPNT-ORG-ID", orgID);
			
			//storing the multi part file in a temp file
	        FileOutputStream fos = new FileOutputStream( convFile );
	        fos.write( file.getBytes() );
			
			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", new FileSystemResource(convFile));
			body.add("targetId",targetId);
			body.add("artifactName", artifactName);
			
			
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			 
			//Calling Deployment URL 
			RestTemplate restTemplate = new RestTemplate();
			
                        //Getting response
                        String response2 =  restTemplate.postForObject("https://anypoint.mulesoft.com/hybrid/api/v1/applications",requestEntity,String.class);
			
                        //Converting to json - we can remove this as by default rest controller returns json as resp
			JSONObject json2 = (JSONObject) parser.parse(response2);
			
			//Closing stream and deleting temp file
			fos.close();
			if(convFile.delete())
			{
			System.out.println("Temp file deleted");
			}
        	
			//Returning response 
			return json2;
			
		}
        catch (ParseException e)
        {
			e.printStackTrace();
		}
		
		return null;		
		
	   }
	
	
	
	//API Implementation for Redeployment
	@RequestMapping(value="/redeploy",method=RequestMethod.POST)
	public JSONObject doRedeploy(@RequestHeader Map<String,String> reqHeaders,@RequestBody MultipartFile file) throws IOException
	{
		
		OkHttpClient httpClient = new OkHttpClient();
		System.out.println("Headers :"+reqHeaders);
		
		//Storing Header values
		//keys are being converted to lower case because of springboot
		String envID = reqHeaders.get("x-anypnt-env-id");
		String orgID = reqHeaders.get("x-anypnt-org-id");
		String appID = reqHeaders.get("id");
		
		
		File convFile = new File( file.getOriginalFilename() );
		
		okhttp3.RequestBody formBody = new FormBody.Builder()
                .add("username", "pams_apiuser_dev")
                .add("password", "Muletest1234")
                .build();

        Request request = new Request.Builder()
                .url("https://anypoint.mulesoft.com/accounts/login")
                .post(formBody)
                .build();
        
		//Calling URL to get bearer token
        Response response =  httpClient.newCall(request).execute();
        
        JSONParser parser = new JSONParser();
        try 
        {
			JSONObject json = (JSONObject) parser.parse(response.body().string());
			
			//Extracting token from response
			String bearer = (String) json.get("access_token");
			
			System.out.println("Bearer Token :"+bearer);
					
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.add("Authorization", "bearer "+bearer);
			headers.add("X-ANYPNT-ENV-ID", envID);
			headers.add("X-ANYPNT-ORG-ID", orgID);
			
			//storing the multi part file in a temp file
	        FileOutputStream fos = new FileOutputStream( convFile );
	        fos.write( file.getBytes() );
			
			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", new FileSystemResource(convFile));
			
			
			
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			 
			//Calling Redeployment URL 
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
					
			//Getting response
			String response2 =  restTemplate.patchForObject("https://anypoint.mulesoft.com/hybrid/api/v1/applications/"+appID, requestEntity, String.class);
			//Converting to json - we can remove this as by default rest controller returns json as resp
			JSONObject json2 = (JSONObject) parser.parse(response2);
			
			//Closing stream and deleting temp file
			fos.close();
			if(convFile.delete())
			{
			System.out.println("Temp file deleted");
			}
        	
			//Returning response 
			return json2;
			
		}
        catch (ParseException e)
        {
			e.printStackTrace();
		}
		
		return null;
        		
		
	}
	
	
	
	


}
