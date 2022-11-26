#!/usr/bin/python
# Okta oauth client credentials flow with Workflow identity federation
from google.cloud import storage
import google.auth
import os
import json
from google.auth import identity_pool
import requests
import http.client
from pprint import pprint
import requests
import json
import base64
userpass = '<clientid>:<clientsecret>'
encoded_u = base64.b64encode(userpass.encode()).decode()

def list_buckets(project, scoped_credentials):
    """Lists all buckets."""
    print("Calling Google Cloud Storage APIs")
    storage_client = storage.Client(
        project=project, credentials=scoped_credentials)
    buckets = storage_client.list_buckets()
    print('Printing Storage bucket for project: ', project)
    for iteration, bucket in enumerate(buckets):
        print("bucket ", iteration, " ", bucket.name)


def get_okta_token():
    cookies = {
        'JSESSIONID': 'C4178E6BB95095E2E6652D69CC3B716A',
    }

    headers = {
        'Accept': '*/*',
        'Authorization' : "Basic %s" % encoded_u,
        'Cache-Control': 'no-cache',
        'Content-Type': 'application/x-www-form-urlencoded',
    }

    data = {'grant_type': 'client_credentials'}
    

    response = requests.post('https://dev-77608005.okta.com/oauth2/aus7d77jbioOzfeXe5d7/v1/token',
                             headers=headers, cookies=cookies, data=data)
    response.raise_for_status()
    print("Creating Okta token file")
    data = response.json()
    with open('/tmp/okta-token.json', 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)


if __name__ == '__main__':
    print("Running main")
    get_okta_token()
    # For service account credentials
    file = open('client-config.json')
    json_config_info = json.loads(file.read())
    #print(json_config_info)
    scopes = ['https://www.googleapis.com/auth/devstorage.full_control',
              'https://www.googleapis.com/auth/devstorage.read_only', 'https://www.googleapis.com/auth/devstorage.read_write']
    #credentials  = google.auth.default(scopes=scopes)
    credentials = identity_pool.Credentials.from_info(json_config_info)
    scoped_credentials = credentials.with_scopes(scopes)
    project = 'yadavaja-sandbox'
    list_buckets(project, scoped_credentials)
    #list_buckets(project, credentials)
    print("Removing Okta token file")
    os.remove('/tmp/okta-token.json')