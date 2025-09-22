FROM flyway/flyway:9.22.3

# Copy migration scripts
COPY db/migration /flyway/sql

# Set default command
CMD ["migrate"]