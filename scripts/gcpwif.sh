#!/bin/bash

# Update the variables below
issuer="<Okta App Url>" 
region="<gcp region>"
audience="<Okta Audience>"
srvcacct="<GCP IAM Service Account Name>"
accountid="<GCP Project Account ID>"
wrkldidpoolname="<GCP Workload ID Pool Name>"
projid="<GCP Project ID>"

# Construct service account email id
srvcacctemail="${srvcacct}@${projid}.iam.gserviceaccount.com"

echo "===================================================="
echo " Inputs ..."
echo " Issuer: ${issuer}" 
echo " Region: ${region}" 
echo " Audience: ${audience}" 
echo " SrvAcct: ${srvcacctemail}"
echo " GCPProjAcctId: ${accountid}"
echo " IdPoolName: ${wrkldidpoolname}"

echo "===================================================="


gcloud iam workload-identity-pools create $wrkldidpoolname \
    --location="global" \
    --description="Testing workload identity federation with Okta " \
    --display-name="$wrkldidpoolname"


gcloud iam workload-identity-pools providers create-oidc okta-provider \
    --location="global" \
    --workload-identity-pool="$wrkldidpoolname" \
    --issuer-uri="$issuer" \
    --allowed-audiences="$audience" \
    --attribute-mapping="google.subject=assertion.sub"


# Create IAM service account and assign roles
gcloud iam service-accounts create $srvcacct --display-name="OktaServiceAccount"
gcloud projects add-iam-policy-binding $projid --member=serviceAccount:$srvcacctemail --role=roles/storage.objectViewer

gcloud iam service-accounts add-iam-policy-binding $srvcacctemail \
--member="principalSet://iam.googleapis.com/projects/$accountid/locations/global/workloadIdentityPools/$wrkldidpoolname/*" \
--role="roles/iam.workloadIdentityUser"