# Ultra-lightweight image using busybox (only ~1-2 MB)
FROM busybox:latest

# Create a simple app
WORKDIR /app
RUN echo '#!/bin/sh' > app.sh && \
    echo 'echo "Hello from gha-test-image"' >> app.sh && \
    echo 'echo "Version: 1.0.0"' >> app.sh && \
    chmod +x app.sh

# Run the app
CMD ["sh", "./app.sh"]