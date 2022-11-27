#!/bin/bash

# Update the variables below
srvcacct="oktasvcacct@ymygcpproj.iam.gserviceaccount.com"
wrkldidpoolname="workload-if-okta-pool"


echo "===================================================="
echo " Inputs ..."
echo " SrvAcct: ${srvcacct}"
echo " IdPoolName: ${wrkldidpoolname}"

echo "===================================================="

gcloud iam service-accounts delete $srvcacct --quiet
gcloud iam workload-identity-pools delete $wrkldidpoolname --location="global" --quiet