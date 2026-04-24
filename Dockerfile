# Ultra-lightweight image using busybox (only ~1-2 MB)
FROM busybox:latest

# Create a simple mock app
WORKDIR /app
RUN echo '#!/bin/sh' > app.sh && \
    echo 'echo "Marvel Mock API - CI/CD Test Image"' >> app.sh && \
    echo 'echo "Version: 1.0.0"' >> app.sh && \
    echo 'echo "Image built successfully"' >> app.sh && \
    chmod +x app.sh

# Run the app
CMD ["sh", "./app.sh"]