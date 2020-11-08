# TechAid Api

Provides backing services for the dashboard site at https://lambeth-techaid.ju.ma/app/

```bash
# It's best to run with docker 
# To build the codebase and start the service locally on http://localhost:8080
# Copy the sample config file first 
cp .env.sample .env
# Start the app
./gradlew ktlintFormat build -x test && docker-compose up 
```

## Generating Gmail Credentials 
```bash
# Get the code for permission request 
https://accounts.google.com/o/oauth2/v2/auth \
 -d client_id=<client_id> \
 -d response_type=code \
 -d scope=https://www.googleapis.com/auth/gmail.modify \
 -d redirect_uri=http://localhost \
 -d access_type=offline
 
# Exchange code for access token and refresh token 
curl -X POST \
 -d code=<code> \
 -d client_id=<client_id> \
 -d client_secret=<client_secret> \
 -d grant_type=<grant_type> \
 -d redirect_uri=<redirect_uri> \
  https://www.googleapis.com/oauth2/v4/token
```
