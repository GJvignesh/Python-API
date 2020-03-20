# -*- coding: utf-8 -*-
"""
Created on Fri Mar 20 17:01:09 2020

@author: vgopalja
"""

from flask import Flask
from flask import request
import requests # importing the requests library 
import os

app = Flask(__name__)

@app.route("/")
def home():
    return "Home Page!"

def get_bearer():
    
    # Logic to get the bearer
    token_url = "https://anypoint.mulesoft.com/accounts/login"
    # data to be sent to token_url 
    data = {"username":"pams_apiuser_dev", 
            "password":"Muletest1234"} 
    # sending post request and saving response as response object 
    token_response = requests.post(url = token_url, data = data) 

    # extracting response text
    token_response_json = token_response.json()
    #print(token_response_json)
    bearer = token_response_json["access_token"]
    return bearer


@app.route("/newdeploy", methods = ["GET","POST"])
def newdeploy():
    
    print("*"*120)
    print(request.headers)    

    # Getting all the details from the incoming headers    
    Env_Id = request.headers.get("X-Anypnt-Env-Id")
    Org_Id = request.headers["X-Anypnt-Org-Id"]
    artifactName = request.headers["artifactName"]
    targetId = request.headers["targetId"] 
    
    
    # Calling bearer method to get the bearer
    bearer = get_bearer()
        
    # pulling the incoming file
    incoming_file = request.files.get('file')
    # fetching the file name
    file_name = incoming_file.filename

    # Saving the incoming file
    incoming_file.save(file_name)
    #incoming_file.save(os.path.join(file_name))
##############################################################################
     # Deploy URL
    deploy_url = "https://anypoint.mulesoft.com/hybrid/api/v1/applications"

    # data to be sent to token_url 
    deploy_headers = { 
          "x-anypnt-env-id":Env_Id,
          "x-anypnt-org-id":Org_Id,
          "Authorization":"bearer "+bearer
          }
    
    # 'Content-Type': "multipart/form-data",
    payload = {
        'artifactName': artifactName,
        "targetId" : targetId
    }


    file = open(file_name,"rb")
    files = {"file":  file}
    
    # sending post request and saving response as response object 
    deploy_response = requests.post(url = deploy_url, headers = deploy_headers, 
                                        data = payload,
                                        files = files)
    file.close()
    
    # extracting response text
    print(deploy_response.headers)
    print("*"*120)
    print(deploy_response.json())
    print("*"*120)

    # removing the saved file
    os.remove(os.path.join(file_name))
    
    return deploy_response.json()
    
if __name__ == "__main__":
    app.run(debug=False)