The goal of this directory is to provide recommendatations and tools for validating a CloudBees CD installation.

# Platform Functionality

1. Server up
    1. https://cdservername
    2. ectool getServerStatus
    3. curl curl -k -X GET "https://admin:changeme@cdservername/rest/v1.0/server/status?block=true&diagnostics=true&serverStateOnly=true&timeout=10" -H "accept: application/json"
1. Agent access
1. Repo
1. DOIS

# Integrations

# Security
1. Ports, SSLv3 only
1. Trusted agents only
1. Access controls

# Custom Models
1. https://github.com/electric-cloud/ec-specs-tool
