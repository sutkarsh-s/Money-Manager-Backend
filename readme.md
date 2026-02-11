To Run the UI(frontend) : 
1. npm install
2. npm run dev

To run the backend: 
1. Ensure u have mysql and rabbitmq as container images on docker, else pull these images
1. docker compose up -d mysql rabbitmq to run my sql and rabbitmq containers
2. docker compose up --build core-service, to build and run core serive
3. docker compose up --build email-service, to build and run email serive